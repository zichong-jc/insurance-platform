package com.example.insurance.sync.strategy;

import com.example.insurance.api.entity.InsuranceCompany;
import com.example.insurance.api.entity.InsuranceProduct;
import com.example.insurance.api.enums.CompanyType;
import com.example.insurance.api.enums.ProductType;
import com.example.insurance.api.enums.SyncStatus;
import com.example.insurance.api.repository.InsuranceCompanyRepository;
import com.example.insurance.api.repository.InsuranceProductRepository;
import com.example.insurance.sync.config.CompanyApiConfig;
import com.example.insurance.sync.config.SyncConfig;
import com.example.insurance.sync.dto.pingan.PingAnProductResponse;
import com.example.insurance.sync.entity.SyncLog;
import com.example.insurance.sync.repository.SyncLogRepository;
import com.example.insurance.sync.service.BrowserAutomationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class PingAnSync implements InsuranceSync {

    private static final String COMPANY_CODE = "PA";
    private static final String COMPANY_NAME = "中国平安保险";

    private final SyncLogRepository syncLogRepository;
    private final InsuranceProductRepository productRepository;
    private final InsuranceCompanyRepository companyRepository;
    private final SyncConfig syncConfig;
    private final CompanyApiConfig companyApiConfig;
    private final RestTemplate restTemplate;
    private final BrowserAutomationService browserService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public CompanyType getCompanyType() {
        return CompanyType.PING_AN;
    }

    @Override
    public String getCompanyCode() {
        return COMPANY_CODE;
    }

    @Override
    public String getCompanyName() {
        return COMPANY_NAME;
    }

    @Override
    public SyncLog syncAllProducts() {
        log.info("Starting sync for Ping An insurance products");
        Instant startTime = Instant.now();

        // 获取或创建保险公司
        InsuranceCompany company = getOrCreateCompany();

        SyncLog syncLog = SyncLog.builder()
                .companyId(company.getId())
                .companyName(getCompanyName())
                .status(SyncStatus.RUNNING)
                .syncTime(startTime)
                .build();

        try {
            // 从产品列表接口获取产品
            List<PingAnProductResponse.ProductData> products = fetchProductList();
            log.info("Fetched {} products from Ping An API", products.size());

            int created = 0;
            int updated = 0;
            int skipped = 0;

            for (PingAnProductResponse.ProductData productData : products) {
                try {
                    SyncResult result = syncProductData(company.getId(), productData);
                    switch (result) {
                        case CREATED -> created++;
                        case UPDATED -> updated++;
                        case SKIPPED -> skipped++;
                    }
                } catch (Exception e) {
                    log.error("Failed to sync product: {}", productData.getProductCode(), e);
                    syncLog.setFilesFailed(syncLog.getFilesFailed() + 1);
                }
            }

            syncLog.setStatus(SyncStatus.SUCCESS);
            syncLog.setMessage(String.format("Sync completed. Created: %d, Updated: %d, Skipped: %d", 
                    created, updated, skipped));
            syncLog.setFilesDownloaded(created + updated);
            syncLog.setFilesSkipped(skipped);

        } catch (Exception e) {
            log.error("Sync failed for Ping An", e);
            syncLog.setStatus(SyncStatus.FAILED);
            syncLog.setMessage("Sync failed: " + e.getMessage());
        }

        syncLog.setDurationMs(java.time.Duration.between(startTime, Instant.now()).toMillis());
        return syncLogRepository.save(syncLog);
    }

    @Override
    public SyncLog syncProduct(Long productId) {
        log.info("Syncing specific product: {} for Ping An", productId);
        Instant startTime = Instant.now();

        InsuranceCompany company = getOrCreateCompany();

        SyncLog syncLog = SyncLog.builder()
                .companyId(company.getId())
                .companyName(getCompanyName())
                .productId(productId)
                .status(SyncStatus.RUNNING)
                .syncTime(startTime)
                .build();

        try {
            // 根据产品ID从列表中查找并同步
            List<PingAnProductResponse.ProductData> products = fetchProductList();
            Optional<PingAnProductResponse.ProductData> targetProduct = products.stream()
                    .filter(p -> p.getProductId().equals(String.valueOf(productId)))
                    .findFirst();

            if (targetProduct.isPresent()) {
                SyncResult result = syncProductData(company.getId(), targetProduct.get());
                syncLog.setStatus(SyncStatus.SUCCESS);
                syncLog.setMessage("Product " + productId + " synced: " + result);
                syncLog.setFilesDownloaded(result == SyncResult.SKIPPED ? 0 : 1);
            } else {
                syncLog.setStatus(SyncStatus.FAILED);
                syncLog.setMessage("Product not found: " + productId);
            }

        } catch (Exception e) {
            log.error("Failed to sync product: {}", productId, e);
            syncLog.setStatus(SyncStatus.FAILED);
            syncLog.setMessage("Sync failed: " + e.getMessage());
        }

        syncLog.setDurationMs(java.time.Duration.between(startTime, Instant.now()).toMillis());
        return syncLogRepository.save(syncLog);
    }

    @Override
    public List<SyncLog> syncProducts(List<Long> productIds) {
        List<SyncLog> logs = new ArrayList<>();
        for (Long productId : productIds) {
            logs.add(syncProduct(productId));
        }
        return logs;
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    /**
     * 获取或创建保险公司
     */
    private InsuranceCompany getOrCreateCompany() {
        return companyRepository.findByCode("PAIC")
                .orElseGet(() -> {
                    InsuranceCompany company = InsuranceCompany.builder()
                            .name(COMPANY_NAME)
                            .code("PAIC")
                            .type(CompanyType.PING_AN)
                            .website("https://www.pingan.com")
                            .build();
                    return companyRepository.save(company);
                });
    }

    /**
     * 从产品列表接口获取产品
     */
    private List<PingAnProductResponse.ProductData> fetchProductList() {
        // 从配置获取接口URL
        CompanyApiConfig.CompanyConfig config = companyApiConfig.getConfigs().get("PING_AN");
        if (config == null || config.getProductListUrl() == null) {
            throw new RuntimeException("Ping An API config not found");
        }

        String url = config.getProductListUrl();
        log.info("Fetching product list from: {}", url);

        // 优先使用浏览器自动化（绕过反爬虫）
        try {
            return fetchWithBrowser(url);
        } catch (Exception e) {
            log.warn("Browser automation failed: {}, trying HTTP client...", e.getMessage());
        }

        // 备选：使用HTTP客户端
        try {
            return fetchWithHttpClient(url, config);
        } catch (Exception e) {
            log.warn("HTTP client failed: {}, using local test data...", e.getMessage());
            return fetchFromLocalFile();
        }
    }

    /**
     * 使用浏览器自动化获取数据
     */
    private List<PingAnProductResponse.ProductData> fetchWithBrowser(String url) {
        log.info("Using browser automation to fetch: {}", url);

        // 使用浏览器获取页面内容
        String pageContent = browserService.fetchPageContent("pingan", url);

        // 从页面中提取JSON数据
        // 平安的API返回的是JSON，但可能被包裹在HTML中或通过JS加载
        String json = extractJsonFromPage(pageContent);

        if (json == null || json.isEmpty()) {
            throw new RuntimeException("No JSON data found in page");
        }

        PingAnProductResponse response;
        try {
            response = objectMapper.readValue(json, PingAnProductResponse.class);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON response", e);
        }

        if (!"200".equals(response.getResultCode())) {
            throw new RuntimeException("API returned error: " + response.getResultMsg());
        }

        List<PingAnProductResponse.ProductData> allProducts = new ArrayList<>();
        if (response.getData() != null) {
            for (PingAnProductResponse.CategoryData category : response.getData()) {
                if (category.getCategoryProducts() != null) {
                    allProducts.addAll(category.getCategoryProducts());
                }
            }
        }

        log.info("Browser automation fetched {} products", allProducts.size());
        return allProducts;
    }

    /**
     * 从页面内容中提取JSON
     */
    private String extractJsonFromPage(String pageContent) {
        // 尝试直接找到JSON响应
        // 平安API通常直接返回JSON或在script标签中

        // 方法1：查找script标签中的JSON
        Pattern scriptPattern = Pattern.compile("<script[^>]*>.*?window\\.__INITIAL_STATE__\\s*=\\s*(\\{.*?\\});.*?</script>", Pattern.DOTALL);
        Matcher scriptMatcher = scriptPattern.matcher(pageContent);
        if (scriptMatcher.find()) {
            return scriptMatcher.group(1);
        }

        // 方法2：查找纯JSON响应（API直接返回）
        int jsonStart = pageContent.indexOf("{");
        int jsonEnd = pageContent.lastIndexOf("}");
        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            String json = pageContent.substring(jsonStart, jsonEnd + 1);
            // 验证是否是有效的JSON
            if (json.contains("resultCode") && json.contains("data")) {
                return json;
            }
        }

        // 方法3：查找pre标签中的JSON
        Pattern prePattern = Pattern.compile("<pre[^>]*>(.*?)</pre>", Pattern.DOTALL);
        Matcher preMatcher = prePattern.matcher(pageContent);
        if (preMatcher.find()) {
            String json = preMatcher.group(1);
            if (json.contains("resultCode")) {
                return json;
            }
        }

        return null;
    }

    /**
     * 使用HTTP客户端获取数据
     */
    private List<PingAnProductResponse.ProductData> fetchWithHttpClient(String url, CompanyApiConfig.CompanyConfig config) {
        log.info("Using HTTP client to fetch: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json, text/plain, */*");
        headers.set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        headers.set("Connection", "keep-alive");
        headers.set("Sec-Fetch-Dest", "empty");
        headers.set("Sec-Fetch-Mode", "cors");
        headers.set("Sec-Fetch-Site", "same-origin");
        if (config.getHeaders() != null) {
            config.getHeaders().forEach(headers::set);
        }

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<PingAnProductResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                PingAnProductResponse.class
        );

        PingAnProductResponse body = response.getBody();
        if (body == null || !"200".equals(body.getResultCode())) {
            throw new RuntimeException("Failed to fetch product list: " +
                    (body != null ? body.getResultMsg() : "Empty response"));
        }

        List<PingAnProductResponse.ProductData> allProducts = new ArrayList<>();
        if (body.getData() != null) {
            for (PingAnProductResponse.CategoryData category : body.getData()) {
                if (category.getCategoryProducts() != null) {
                    allProducts.addAll(category.getCategoryProducts());
                }
            }
        }

        return allProducts;
    }

    /**
     * 从本地JSON文件读取测试数据
     */
    private List<PingAnProductResponse.ProductData> fetchFromLocalFile() {
        log.info("Loading product data from local file");
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(
                    "/Users/jiangchao/Dev/fullStack/Insurance/insurance-platform/insurance-sync/src/main/java/com/example/insurance/sync/strategy/ping-an-all-product.json");
            String content = java.nio.file.Files.readString(path);

            int jsonStart = content.indexOf("{");
            if (jsonStart == -1) {
                throw new RuntimeException("No JSON content found in file");
            }
            String json = content.substring(jsonStart);

            PingAnProductResponse response = objectMapper.readValue(json, PingAnProductResponse.class);

            if (!"200".equals(response.getResultCode())) {
                throw new RuntimeException("Invalid response code: " + response.getResultCode());
            }

            List<PingAnProductResponse.ProductData> allProducts = new ArrayList<>();
            if (response.getData() != null) {
                for (PingAnProductResponse.CategoryData category : response.getData()) {
                    if (category.getCategoryProducts() != null) {
                        allProducts.addAll(category.getCategoryProducts());
                    }
                }
            }

            log.info("Loaded {} products from local file", allProducts.size());
            return allProducts;
        } catch (Exception e) {
            log.error("Failed to load local file", e);
            throw new RuntimeException("Failed to fetch product list from all sources", e);
        }
    }

    /**
     * 同步单个产品数据
     */
    private SyncResult syncProductData(Long companyId, PingAnProductResponse.ProductData productData) {
        String productCode = productData.getProductCode();
        String productName = productData.getProductName();

        // 优先使用 productCode 检查产品是否已存在
        Optional<InsuranceProduct> existingProductByCode = productRepository
                .findByProductCode(productCode);

        if (existingProductByCode.isPresent()) {
            InsuranceProduct product = existingProductByCode.get();
            // 检查是否有更新
            boolean hasChanges = checkProductChanges(product, productData);
            if (!hasChanges) {
                log.debug("Product unchanged, skipping: {}", productCode);
                return SyncResult.SKIPPED;
            }

            // 更新产品
            updateProduct(product, productData);
            productRepository.save(product);
            log.info("Product updated: {}", productCode);
            return SyncResult.UPDATED;
        }

        // 如果没有 productCode 匹配，再用名称检查（兼容旧数据）
        Optional<InsuranceProduct> existingProductByName = productRepository
                .findByCompanyIdAndName(companyId, productName);

        if (existingProductByName.isPresent()) {
            InsuranceProduct product = existingProductByName.get();
            // 更新 productCode
            product.setProductCode(productCode);
            updateProduct(product, productData);
            productRepository.save(product);
            log.info("Product updated (by name): {}", productCode);
            return SyncResult.UPDATED;
        }

        // 创建新产品
        InsuranceProduct newProduct = createProduct(companyId, productData);
        productRepository.save(newProduct);
        log.info("Product created: {}", productCode);
        return SyncResult.CREATED;
    }

    /**
     * 检查产品是否有变更
     */
    private boolean checkProductChanges(InsuranceProduct product, PingAnProductResponse.ProductData productData) {
        // 比较关键字段
        if (!product.getName().equals(productData.getProductName())) {
            return true;
        }

        // 比较属性字段
        Map<String, Object> properties = product.getProperties();
        if (properties == null) {
            return true;
        }

        String oldPrice = String.valueOf(properties.get("price"));
        String newPrice = String.valueOf(productData.getProductPrice());
        if (!oldPrice.equals(newPrice)) {
            return true;
        }

        String oldDesc = (String) properties.get("description");
        String newDesc = productData.getProductDesc();
        if (newDesc != null && !newDesc.equals(oldDesc)) {
            return true;
        }

        return false;
    }

    /**
     * 创建新产品
     */
    private InsuranceProduct createProduct(Long companyId, PingAnProductResponse.ProductData productData) {
        // 构建属性
        Map<String, Object> properties = new HashMap<>();
        properties.put("productId", productData.getProductId());
        properties.put("categoryCode", productData.getCategoryCode());
        properties.put("categoryName", productData.getCategoryName());
        properties.put("price", productData.getProductPrice());
        properties.put("priceUnit", productData.getPriceUnit());
        properties.put("description", productData.getProductDesc());
        properties.put("productPic", productData.getProductPic());
        properties.put("productUrl", productData.getProductUrl());
        properties.put("homeProductFlag", productData.getHomeProductFlag());
        properties.put("hotProductFlag", productData.getHotProductFlag());
        properties.put("newProductFlag", productData.getNewProductFlag());
        properties.put("productOrder", productData.getProductOrder());

        // 确定产品类型
        ProductType productType = determineProductType(productData.getCategoryCode());

        return InsuranceProduct.builder()
                .companyId(companyId)
                .name(productData.getProductName())
                .productCode(productData.getProductCode())
                .type(productType)
                .status("ACTIVE")
                .description(productData.getProductDesc())
                .properties(properties)
                .build();
    }

    /**
     * 更新产品
     */
    private void updateProduct(InsuranceProduct product, PingAnProductResponse.ProductData productData) {
        Map<String, Object> properties = product.getProperties();
        if (properties == null) {
            properties = new HashMap<>();
        }

        properties.put("productId", productData.getProductId());
        properties.put("price", productData.getProductPrice());
        properties.put("priceUnit", productData.getPriceUnit());
        properties.put("description", productData.getProductDesc());
        properties.put("productPic", productData.getProductPic());
        properties.put("productUrl", productData.getProductUrl());
        properties.put("homeProductFlag", productData.getHomeProductFlag());
        properties.put("hotProductFlag", productData.getHotProductFlag());
        properties.put("newProductFlag", productData.getNewProductFlag());

        product.setProperties(properties);
        product.setDescription(productData.getProductDesc());
    }

    /**
     * 根据分类代码确定产品类型
     */
    private ProductType determineProductType(String categoryCode) {
        if (categoryCode == null) {
            return ProductType.OTHER;
        }

        return switch (categoryCode) {
            case "PC_HEALTHY" -> ProductType.HEALTH;
            case "PC_LIFE" -> ProductType.LIFE;
            case "PC_CAR" -> ProductType.AUTO;
            case "PC_PROPERTY" -> ProductType.PROPERTY;
            case "PC_TRAVEL" -> ProductType.TRAVEL;
            case "PC_ACCIDENT" -> ProductType.ACCIDENT;
            default -> ProductType.OTHER;
        };
    }

    private enum SyncResult {
        CREATED, UPDATED, SKIPPED
    }
}
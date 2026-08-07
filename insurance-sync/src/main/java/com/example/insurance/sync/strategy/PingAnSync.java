package com.example.insurance.sync.strategy;

import com.example.insurance.api.entity.InsuranceCompany;
import com.example.insurance.api.entity.InsuranceDocument;
import com.example.insurance.api.entity.InsuranceProduct;
import com.example.insurance.api.entity.InsuranceVersion;
import com.example.insurance.api.enums.CompanyType;
import com.example.insurance.api.enums.ProductType;
import com.example.insurance.api.enums.SyncStatus;
import com.example.insurance.api.enums.VersionStatus;
import com.example.insurance.api.repository.InsuranceCompanyRepository;
import com.example.insurance.api.repository.InsuranceDocumentRepository;
import com.example.insurance.api.repository.InsuranceProductRepository;
import com.example.insurance.api.repository.InsuranceVersionRepository;
import com.example.insurance.sync.config.CompanyApiConfig;
import com.example.insurance.sync.config.SyncConfig;
import com.example.insurance.sync.dto.pingan.PingAnProductResponse;
import com.example.insurance.sync.entity.SyncLog;
import com.example.insurance.sync.repository.SyncLogRepository;
import com.example.insurance.sync.service.BrowserAutomationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
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
    private final InsuranceVersionRepository versionRepository;
    private final InsuranceDocumentRepository documentRepository;
    private final com.example.insurance.api.service.MinioStorageService minioStorageService;
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

        InsuranceCompany company = getOrCreateCompany();

        SyncLog syncLog = SyncLog.builder()
                .companyId(company.getId())
                .companyName(getCompanyName())
                .status(SyncStatus.RUNNING)
                .syncTime(startTime)
                .build();

        try {
            List<PingAnProductResponse.ProductData> products = fetchProductList();
            log.info("Fetched {} products from Ping An API", products.size());

            int created = 0;
            int updated = 0;
            int skipped = 0;
            int totalPdfCount = 0;

            for (PingAnProductResponse.ProductData productData : products) {
                try {
                    SyncResult result = syncProductData(company.getId(), productData);
                    switch (result) {
                        case CREATED -> created++;
                        case UPDATED -> updated++;
                        case SKIPPED -> skipped++;
                    }

                    if (result == SyncResult.CREATED || result == SyncResult.UPDATED) {
                        InsuranceProduct product = productRepository.findByProductCode(productData.getProductCode())
                                .orElse(null);
                        if (product != null) {
                            int pdfCount = syncProductDocuments(product, productData);
                            totalPdfCount += pdfCount;
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to sync product: {}", productData.getProductCode(), e);
                    syncLog.setFilesFailed(syncLog.getFilesFailed() + 1);
                }
            }

            syncLog.setStatus(SyncStatus.SUCCESS);
            syncLog.setMessage(String.format("Sync completed. Created: %d, Updated: %d, Skipped: %d, PDFs: %d",
                    created, updated, skipped, totalPdfCount));
            syncLog.setFilesDownloaded(totalPdfCount);
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
            List<PingAnProductResponse.ProductData> products = fetchProductList();
            Optional<PingAnProductResponse.ProductData> targetProduct = products.stream()
                    .filter(p -> p.getProductId().equals(String.valueOf(productId)))
                    .findFirst();

            if (targetProduct.isPresent()) {
                PingAnProductResponse.ProductData productData = targetProduct.get();
                SyncResult result = syncProductData(company.getId(), productData);

                if (result == SyncResult.CREATED || result == SyncResult.UPDATED) {
                    InsuranceProduct product = productRepository.findByProductCode(productData.getProductCode())
                            .orElseThrow(() -> new RuntimeException("Product not found after sync"));

                    int pdfCount = syncProductDocuments(product, productData);
                    syncLog.setMessage("Product " + productId + " synced: " + result + ", PDFs downloaded: " + pdfCount);
                    syncLog.setFilesDownloaded(pdfCount);
                } else {
                    syncLog.setMessage("Product " + productId + " synced: " + result);
                    syncLog.setFilesDownloaded(0);
                }
                syncLog.setStatus(SyncStatus.SUCCESS);
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
     * Get or create insurance company
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
     * Fetch product list from API
     */
    private List<PingAnProductResponse.ProductData> fetchProductList() {
        CompanyApiConfig.CompanyConfig config = companyApiConfig.getConfigs().get("PING_AN");
        if (config == null || config.getProductListUrl() == null) {
            throw new RuntimeException("Ping An API config not found");
        }

        String url = config.getProductListUrl();
        log.info("Fetching product list from: {}", url);

        try {
            return fetchWithBrowser(url);
        } catch (Exception e) {
            log.warn("Browser automation failed: {}, trying HTTP client...", e.getMessage());
        }

        try {
            return fetchWithHttpClient(url, config);
        } catch (Exception e) {
            log.warn("HTTP client failed: {}, using local test data...", e.getMessage());
            return fetchFromLocalFile();
        }
    }

    /**
     * Fetch data using browser automation
     */
    private List<PingAnProductResponse.ProductData> fetchWithBrowser(String url) {
        log.info("Using browser automation to fetch: {}", url);

        String pageContent = browserService.fetchPageContent("pingan", url);
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
     * Extract JSON from page content
     */
    private String extractJsonFromPage(String pageContent) {
        Pattern scriptPattern = Pattern.compile("<script[^>]*>.*?window\\.__INITIAL_STATE__\\s*=\\s*(\\{.*?\\});.*?</script>", Pattern.DOTALL);
        Matcher scriptMatcher = scriptPattern.matcher(pageContent);
        if (scriptMatcher.find()) {
            return scriptMatcher.group(1);
        }

        int jsonStart = pageContent.indexOf("{");
        int jsonEnd = pageContent.lastIndexOf("}");
        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            String json = pageContent.substring(jsonStart, jsonEnd + 1);
            if (json.contains("resultCode") && json.contains("data")) {
                return json;
            }
        }

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
     * Fetch data using HTTP client
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
     * Fetch test data from local JSON file
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
     * Sync single product data
     */
    private SyncResult syncProductData(Long companyId, PingAnProductResponse.ProductData productData) {
        String productCode = productData.getProductCode();
        String productName = productData.getProductName();

        Optional<InsuranceProduct> existingProductByCode = productRepository
                .findByProductCode(productCode);

        if (existingProductByCode.isPresent()) {
            InsuranceProduct product = existingProductByCode.get();
            boolean hasChanges = checkProductChanges(product, productData);
            if (!hasChanges) {
                log.debug("Product unchanged, skipping: {}", productCode);
                return SyncResult.SKIPPED;
            }

            updateProduct(product, productData);
            productRepository.save(product);
            log.info("Product updated: {}", productCode);
            return SyncResult.UPDATED;
        }

        Optional<InsuranceProduct> existingProductByName = productRepository
                .findByCompanyIdAndName(companyId, productName);

        if (existingProductByName.isPresent()) {
            InsuranceProduct product = existingProductByName.get();
            product.setProductCode(productCode);
            updateProduct(product, productData);
            productRepository.save(product);
            log.info("Product updated (by name): {}", productCode);
            return SyncResult.UPDATED;
        }

        InsuranceProduct newProduct = createProduct(companyId, productData);
        productRepository.save(newProduct);
        log.info("Product created: {}", productCode);
        return SyncResult.CREATED;
    }

    /**
     * Check if product has changes
     */
    private boolean checkProductChanges(InsuranceProduct product, PingAnProductResponse.ProductData productData) {
        if (!product.getName().equals(productData.getProductName())) {
            return true;
        }

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
     * Create new product
     */
    private InsuranceProduct createProduct(Long companyId, PingAnProductResponse.ProductData productData) {
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
     * Update existing product
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
     * Determine product type from category code
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

    /**
     * Sync product documents (PDF terms)
     * 1. Visit product detail page
     * 2. Handle agreement popup
     * 3. Find all PDF links after "I have read and agree"
     * 4. Download PDFs and save to MinIO
     */
    private int syncProductDocuments(InsuranceProduct product, PingAnProductResponse.ProductData productData) {
        String productUrl = productData.getProductUrl();
        if (productUrl == null || productUrl.isEmpty()) {
            log.warn("No product URL found for product: {}", productData.getProductCode());
            return 0;
        }

        log.info("Syncing documents for product: {} from URL: {}", product.getId(), productUrl);

        try {
            List<PdfDocumentInfo> pdfLinks = browserService.extractPdfLinks("pingan_product_" + product.getId(), productUrl);

            if (pdfLinks.isEmpty()) {
                log.warn("No PDF links found for product: {}", product.getId());
                return 0;
            }

            InsuranceVersion version = createVersion(product, productData, pdfLinks);

            int downloadedCount = 0;
            for (PdfDocumentInfo pdfInfo : pdfLinks) {
                try {
                    downloadAndSavePdf(product, version, pdfInfo);
                    downloadedCount++;
                } catch (Exception e) {
                    log.error("Failed to download PDF: {} for product: {}", pdfInfo.getName(), product.getId(), e);
                }
            }

            log.info("Downloaded {} PDF documents for product: {}", downloadedCount, product.getId());
            return downloadedCount;

        } catch (Exception e) {
            log.error("Failed to sync documents for product: {}", product.getId(), e);
            return 0;
        }
    }

    /**
     * Create version record
     */
    private InsuranceVersion createVersion(InsuranceProduct product, PingAnProductResponse.ProductData productData, List<PdfDocumentInfo> pdfLinks) {
        String versionNumber = java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd.HHmm")
                .format(java.time.LocalDateTime.now());

        String hash = calculatePdfHash(pdfLinks);

        InsuranceVersion version = InsuranceVersion.builder()
                .productId(product.getId())
                .versionNumber(versionNumber)
                .versionType("SYNC")
                .versionDescription("Auto sync version - " + pdfLinks.size() + " documents")
                .status(VersionStatus.ACTIVE)
                .syncTime(Instant.now())
                .hash(hash)
                .downloadUrl(productData.getProductUrl())
                .build();

        InsuranceVersion savedVersion = versionRepository.save(version);
        log.info("Created version: {} for product: {}", versionNumber, product.getId());
        return savedVersion;
    }

    /**
     * Calculate hash of PDF links
     */
    private String calculatePdfHash(List<PdfDocumentInfo> pdfLinks) {
        StringBuilder sb = new StringBuilder();
        for (PdfDocumentInfo pdf : pdfLinks) {
            sb.append(pdf.getUrl()).append("|");
        }
        return org.springframework.util.DigestUtils.md5DigestAsHex(sb.toString().getBytes());
    }

    /**
     * Download and save PDF to MinIO
     */
    private void downloadAndSavePdf(InsuranceProduct product, InsuranceVersion version, PdfDocumentInfo pdfInfo) {
        log.info("Downloading PDF: {} from: {}", pdfInfo.getName(), pdfInfo.getUrl());

        byte[] pdfContent = downloadPdfContent(pdfInfo.getUrl());
        String fileHash = org.springframework.util.DigestUtils.md5DigestAsHex(pdfContent);
        String fileName = pdfInfo.getName().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5.]", "_") + ".pdf";

        String objectKey = minioStorageService.generateObjectKey(product.getProductCode(), fileName);
        String minioUrl = minioStorageService.uploadFile(objectKey, pdfContent, "application/pdf");

        InsuranceDocument document = InsuranceDocument.builder()
                .versionId(version.getId())
                .productId(product.getId())
                .documentType(determineDocumentType(pdfInfo.getName()))
                .documentName(pdfInfo.getName())
                .fileUrl(pdfInfo.getUrl())
                .localPath(minioUrl)
                .fileHash(fileHash)
                .fileSize((long) pdfContent.length)
                .parseStatus("PENDING")
                .createdTime(Instant.now())
                .build();

        documentRepository.save(document);
        log.info("Saved PDF document to MinIO: {} for product: {}", pdfInfo.getName(), product.getId());
    }

    /**
     * Download PDF content
     */
    private byte[] downloadPdfContent(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36");
        headers.set("Accept", "application/pdf,*/*");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);

        return response.getBody();
    }

    /**
     * Determine document type from name
     */
    private String determineDocumentType(String documentName) {
        String name = documentName.toLowerCase();
        if (name.contains("条款") || name.contains("terms")) {
            return "TERMS";
        } else if (name.contains("免责") || name.contains("exclusion")) {
            return "EXCLUSION";
        } else if (name.contains("投保") || name.contains("underwriting")) {
            return "UNDERWRITING";
        } else if (name.contains("续保") || name.contains("renewal")) {
            return "RENEWAL";
        } else if (name.contains("保障") || name.contains("coverage")) {
            return "COVERAGE";
        } else if (name.contains("须知") || name.contains("notice")) {
            return "NOTICE";
        } else if (name.contains("附加") || name.contains("rider")) {
            return "RIDER";
        }
        return "TERMS";
    }

    /**
     * PDF document info
     */
    @Data
    public static class PdfDocumentInfo {
        private String name;
        private String url;
    }

    private enum SyncResult {
        CREATED, UPDATED, SKIPPED
    }
}
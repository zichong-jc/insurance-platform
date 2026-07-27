
package com.example.insurance.sync.strategy;

import com.example.insurance.api.enums.CompanyType;
import com.example.insurance.api.enums.SyncStatus;
import com.example.insurance.common.util.HashUtils;
import com.example.insurance.sync.config.SyncConfig;
import com.example.insurance.sync.entity.SyncLog;
import com.example.insurance.sync.repository.SyncLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PICCSync implements InsuranceSync {

    private final SyncLogRepository syncLogRepository;
    private final SyncConfig syncConfig;
    private final RestTemplate restTemplate;

    @Override
    public CompanyType getCompanyType() {
        return CompanyType.PICC;
    }

    @Override
    public String getCompanyCode() {
        return CompanyType.PICC.getCode();
    }

    @Override
    public String getCompanyName() {
        return CompanyType.PICC.getDescription();
    }

    @Override
    public SyncLog syncAllProducts() {
        log.info("Starting sync for PICC insurance products");
        Instant startTime = Instant.now();
        
        SyncLog syncLog = SyncLog.builder()
                .companyId(3L)
                .companyName(getCompanyName())
                .status(SyncStatus.RUNNING)
                .syncTime(startTime)
                .build();
        
        try {
            List<String> productUrls = fetchProductUrls();
            int downloaded = 0;
            int skipped = 0;
            
            for (String url : productUrls) {
                try {
                    byte[] content = downloadFile(url);
                    String hash = HashUtils.sha256(content);
                    
                    if (isFileChanged(url, hash)) {
                        saveFile(content, url, hash);
                        downloaded++;
                    } else {
                        skipped++;
                    }
                } catch (Exception e) {
                    log.error("Failed to sync product: {}", url, e);
                }
            }
            
            syncLog.setStatus(SyncStatus.SUCCESS);
            syncLog.setMessage("Synced " + downloaded + " products, skipped " + skipped);
            syncLog.setFilesDownloaded(downloaded);
            syncLog.setFilesSkipped(skipped);
            
        } catch (Exception e) {
            log.error("Sync failed for PICC", e);
            syncLog.setStatus(SyncStatus.FAILED);
            syncLog.setMessage("Sync failed: " + e.getMessage());
        }
        
        syncLog.setDurationMs(java.time.Duration.between(startTime, Instant.now()).toMillis());
        return syncLogRepository.save(syncLog);
    }

    @Override
    public SyncLog syncProduct(Long productId) {
        log.info("Syncing specific product: {} for PICC", productId);
        Instant startTime = Instant.now();
        
        SyncLog syncLog = SyncLog.builder()
                .companyId(3L)
                .companyName(getCompanyName())
                .productId(productId)
                .status(SyncStatus.RUNNING)
                .syncTime(startTime)
                .build();
        
        try {
            String productUrl = getProductUrl(productId);
            byte[] content = downloadFile(productUrl);
            String hash = HashUtils.sha256(content);
            
            if (isFileChanged(productUrl, hash)) {
                saveFile(content, productUrl, hash);
                syncLog.setFilesDownloaded(1);
                syncLog.setMessage("Product " + productId + " synced successfully");
            } else {
                syncLog.setFilesSkipped(1);
                syncLog.setMessage("Product " + productId + " unchanged, skipped");
            }
            
            syncLog.setStatus(SyncStatus.SUCCESS);
            
        } catch (Exception e) {
            log.error("Failed to sync product: {}", productId, e);
            syncLog.setStatus(SyncStatus.FAILED);
            syncLog.setMessage("Sync failed: " + e.getMessage());
            syncLog.setFilesFailed(1);
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

    private List<String> fetchProductUrls() {
        return List.of(
                "https://www.picc.com/product1.pdf",
                "https://www.picc.com/product2.pdf",
                "https://www.picc.com/product3.pdf"
        );
    }

    private String getProductUrl(Long productId) {
        return "https://www.picc.com/product" + productId + ".pdf";
    }

    private byte[] downloadFile(String url) {
        return restTemplate.getForObject(url, byte[].class);
    }

    private boolean isFileChanged(String url, String newHash) {
        return true;
    }

    private void saveFile(byte[] content, String url, String hash) {
        log.info("Saving file: {} with hash: {}", url, hash);
    }
}
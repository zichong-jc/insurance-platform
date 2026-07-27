
package com.example.insurance.sync.service;

import com.example.insurance.api.enums.CompanyType;
import com.example.insurance.common.exception.ResourceNotFoundException;
import com.example.insurance.sync.config.SyncConfig;
import com.example.insurance.sync.entity.SyncLog;
import com.example.insurance.sync.repository.SyncLogRepository;
import com.example.insurance.sync.strategy.InsuranceSync;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final List<InsuranceSync> syncStrategies;
    private final SyncLogRepository syncLogRepository;
    private final SyncConfig syncConfig;

    private final Map<String, InsuranceSync> strategyCache = new ConcurrentHashMap<>();

    public SyncLog syncCompany(String companyCode) {
        log.info("Starting sync for company: {}", companyCode);
        
        InsuranceSync strategy = getSyncStrategy(companyCode);
        return strategy.syncAllProducts();
    }

    public SyncLog syncProduct(String companyCode, Long productId) {
        log.info("Starting sync for company: {}, product: {}", companyCode, productId);
        
        InsuranceSync strategy = getSyncStrategy(companyCode);
        return strategy.syncProduct(productId);
    }

    @Async
    public void syncCompanyAsync(String companyCode) {
        log.info("Starting async sync for company: {}", companyCode);
        try {
            syncCompany(companyCode);
        } catch (Exception e) {
            log.error("Async sync failed for company: {}", companyCode, e);
        }
    }

    public SyncLog getSyncLog(Long logId) {
        return syncLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("SyncLog", logId));
    }

    public Page<SyncLog> getSyncLogs(Pageable pageable) {
        return syncLogRepository.findAll(pageable);
    }

    public Page<SyncLog> getSyncLogsByCompany(Long companyId, Pageable pageable) {
        return syncLogRepository.findByCompanyId(companyId, pageable);
    }

    public List<SyncLog> getLatestSyncLogs(Long companyId, int limit) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        return syncLogRepository.findLatestByCompanyId(companyId, pageable);
    }

    public List<SyncLog> getSyncLogsSince(Instant startTime) {
        return syncLogRepository.findBySyncTimeSince(startTime);
    }

    private InsuranceSync getSyncStrategy(String companyCode) {
        return strategyCache.computeIfAbsent(companyCode, code -> {
            CompanyType companyType = CompanyType.fromCode(code);
            return syncStrategies.stream()
                    .filter(strategy -> strategy.getCompanyType() == companyType)
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Sync strategy", code));
        });
    }

    public List<String> getSupportedCompanies() {
        return syncStrategies.stream()
                .filter(InsuranceSync::isSupported)
                .map(InsuranceSync::getCompanyCode)
                .toList();
    }
}
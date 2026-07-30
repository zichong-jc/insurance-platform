
package com.example.insurance.web.service;

import com.example.insurance.api.dto.request.InsuranceVersionRequest;
import com.example.insurance.api.dto.response.InsuranceVersionResponse;
import com.example.insurance.api.entity.InsuranceVersion;
import com.example.insurance.api.enums.SyncStatus;
import com.example.insurance.api.repository.InsuranceProductRepository;
import com.example.insurance.common.exception.ResourceNotFoundException;
import com.example.insurance.web.repository.InsuranceVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class VersionService {

    private final InsuranceVersionRepository versionRepository;
    private final InsuranceProductRepository productRepository;

    public Page<InsuranceVersionResponse> getVersionsByProduct(Long productId, Pageable pageable) {
        log.debug("Getting versions for product: {}", productId);
        return versionRepository.findByProductId(productId, pageable)
                .map(this::toResponse);
    }

    public InsuranceVersionResponse getVersion(Long productId, Long versionId) {
        log.debug("Getting version: {} for product: {}", versionId, productId);
        InsuranceVersion version = versionRepository.findById(versionId)
                .filter(v -> v.getProductId().equals(productId) && !v.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("InsuranceVersion", versionId));
        return toResponse(version);
    }

    @Transactional
    public InsuranceVersionResponse createVersion(Long productId, InsuranceVersionRequest request) {
        log.info("Creating version for product: {}", productId);
        
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("InsuranceProduct", productId);
        }
        
        InsuranceVersion version = InsuranceVersion.builder()
                .productId(productId)
                .versionNumber(request.getVersionNumber())
                .hash(request.getHash())
                .downloadUrl(request.getDownloadUrl())
                .status(SyncStatus.PENDING)
                .build();
        
        InsuranceVersion saved = versionRepository.save(version);
        log.info("Created version: {} for product: {}", saved.getId(), productId);
        
        return toResponse(saved);
    }

    @Transactional
    public void deleteVersion(Long productId, Long versionId) {
        log.info("Deleting version: {} for product: {}", versionId, productId);
        
        InsuranceVersion version = versionRepository.findById(versionId)
                .filter(v -> v.getProductId().equals(productId) && !v.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("InsuranceVersion", versionId));
        
        version.setDeleted(true);
        versionRepository.save(version);
        
        log.info("Soft deleted version: {}", versionId);
    }

    @Transactional
    public InsuranceVersionResponse updateSyncStatus(Long versionId, SyncStatus status) {
        log.info("Updating sync status for version: {} to {}", versionId, status);
        
        InsuranceVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("InsuranceVersion", versionId));
        
        version.setStatus(status);
        version.setSyncTime(Instant.now());
        
        InsuranceVersion updated = versionRepository.save(version);
        return toResponse(updated);
    }

    private InsuranceVersionResponse toResponse(InsuranceVersion version) {
        InsuranceVersionResponse response = InsuranceVersionResponse.builder()
                .id(version.getId())
                .productId(version.getProductId())
                .versionNumber(version.getVersionNumber())
                .hash(version.getHash())
                .downloadUrl(version.getDownloadUrl())
                .syncTime(version.getSyncTime())
                .status(version.getStatus().name())
                .createdAt(version.getCreatedAt())
                .updatedAt(version.getUpdatedAt())
                .build();
        
        productRepository.findById(version.getProductId())
                .ifPresent(product -> response.setProductName(product.getName()));
        
        return response;
    }
}
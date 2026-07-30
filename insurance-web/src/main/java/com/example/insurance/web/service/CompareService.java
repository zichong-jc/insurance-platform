
package com.example.insurance.web.service;

import com.example.insurance.api.dto.response.ProductCompareResponse;
import com.example.insurance.api.entity.InsuranceProduct;
import com.example.insurance.api.repository.InsuranceProductRepository;
import com.example.insurance.common.exception.ResourceNotFoundException;
import com.example.insurance.web.repository.InsuranceVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompareService {

    private final InsuranceProductRepository productRepository;
    private final InsuranceVersionRepository versionRepository;

    public ProductCompareResponse compareProducts(List<Long> productIds, List<String> compareFields) {
        log.info("Comparing products: {}", productIds);
        
        List<ProductCompareResponse.ProductComparison> comparisons = new ArrayList<>();
        
        for (Long productId : productIds) {
            InsuranceProduct product = productRepository.findById(productId)
                    .filter(p -> !p.getDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("InsuranceProduct", productId));
            
            ProductCompareResponse.ProductComparison comparison = buildProductComparison(product);
            comparisons.add(comparison);
        }
        
        List<String> fields = compareFields != null && !compareFields.isEmpty() 
                ? compareFields 
                : getDefaultCompareFields();
        
        return ProductCompareResponse.builder()
                .products(comparisons)
                .compareFields(fields)
                .build();
    }

    private ProductCompareResponse.ProductComparison buildProductComparison(InsuranceProduct product) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", product.getName());
        attributes.put("type", product.getType().getDescription());
        attributes.put("status", product.getStatus());
        
        if (product.getCompanyId() != null) {
            productRepository.findById(product.getCompanyId())
                    .ifPresent(company -> attributes.put("company", company.getName()));
        }
        
        List<ProductCompareResponse.VersionInfo> versions = versionRepository
                .findByProductIdOrderByCreatedAtDesc(product.getId())
                .stream()
                .limit(5)
                .map(v -> ProductCompareResponse.VersionInfo.builder()
                        .versionNumber(v.getVersionNumber())
                        .hash(v.getHash())
                        .documentCount("N/A")
                        .build())
                .collect(Collectors.toList());
        
        return ProductCompareResponse.ProductComparison.builder()
                .productId(product.getId())
                .productName(product.getName())
                .companyName(product.getCompanyId() != null ? 
                        productRepository.findById(product.getCompanyId())
                                .map(c -> c.getName())
                                .orElse("Unknown") : "Unknown")
                .productType(product.getType().getDescription())
                .attributes(attributes)
                .versions(versions)
                .build();
    }

    private List<String> getDefaultCompareFields() {
        return List.of("name", "type", "company", "status", "versions");
    }
}
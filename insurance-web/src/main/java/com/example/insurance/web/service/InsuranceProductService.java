
package com.example.insurance.web.service;

import com.example.insurance.api.dto.request.InsuranceProductRequest;
import com.example.insurance.api.dto.response.InsuranceProductResponse;
import com.example.insurance.api.enums.ProductType;
import com.example.insurance.common.exception.ConflictException;
import com.example.insurance.common.exception.ResourceNotFoundException;
import com.example.insurance.web.entity.InsuranceProduct;
import com.example.insurance.web.mapper.InsuranceProductMapper;
import com.example.insurance.web.repository.InsuranceCompanyRepository;
import com.example.insurance.web.repository.InsuranceProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceProductService {

    private final InsuranceProductRepository productRepository;
    private final InsuranceCompanyRepository companyRepository;
    private final InsuranceProductMapper mapper;

    public Page<InsuranceProductResponse> getProducts(Pageable pageable) {
        log.debug("Getting products with pagination");
        return productRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public Page<InsuranceProductResponse> getProductsByCompany(Long companyId, Pageable pageable) {
        log.debug("Getting products for company: {}", companyId);
        return productRepository.findByCompanyId(companyId, pageable)
                .map(this::toResponse);
    }

    public InsuranceProductResponse getProductById(Long id) {
        log.debug("Getting product by id: {}", id);
        InsuranceProduct product = productRepository.findById(id)
                .filter(p -> !p.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("InsuranceProduct", id));
        return toResponse(product);
    }

    @Transactional
    public InsuranceProductResponse createProduct(InsuranceProductRequest request) {
        log.info("Creating new product: {}", request.getName());
        
        if (!companyRepository.existsById(request.getCompanyId())) {
            throw new ResourceNotFoundException("InsuranceCompany", request.getCompanyId());
        }
        
        if (productRepository.existsByCompanyIdAndName(request.getCompanyId(), request.getName())) {
            throw new ConflictException("Product name already exists for this company");
        }
        
        InsuranceProduct product = mapper.toEntity(request);
        InsuranceProduct saved = productRepository.save(product);
        log.info("Created product with id: {}", saved.getId());
        
        return toResponse(saved);
    }

    @Transactional
    public InsuranceProductResponse updateProduct(Long id, InsuranceProductRequest request) {
        log.info("Updating product: {}", id);
        
        InsuranceProduct product = productRepository.findById(id)
                .filter(p -> !p.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("InsuranceProduct", id));
        
        if (!product.getCompanyId().equals(request.getCompanyId()) 
                && productRepository.existsByCompanyIdAndName(request.getCompanyId(), request.getName())) {
            throw new ConflictException("Product name already exists for this company");
        }
        
        product.setCompanyId(request.getCompanyId());
        product.setName(request.getName());
        product.setType(ProductType.fromCode(request.getType()));
        product.setStatus(request.getStatus());
        
        InsuranceProduct updated = productRepository.save(product);
        log.info("Updated product: {}", id);
        
        return toResponse(updated);
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product: {}", id);
        
        InsuranceProduct product = productRepository.findById(id)
                .filter(p -> !p.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("InsuranceProduct", id));
        
        product.setDeleted(true);
        productRepository.save(product);
        
        log.info("Soft deleted product: {}", id);
    }

    private InsuranceProductResponse toResponse(InsuranceProduct product) {
        InsuranceProductResponse response = mapper.toResponse(product);
        
        if (product.getCompanyId() != null) {
            companyRepository.findById(product.getCompanyId())
                    .ifPresent(company -> response.setCompanyName(company.getName()));
        }
        
        Long versionCount = productRepository.countVersionsByProductId(product.getId());
        response.setVersionCount(versionCount.intValue());
        
        return response;
    }
}
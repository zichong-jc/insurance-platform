
package com.example.insurance.web.service;

import com.example.insurance.api.dto.request.InsuranceCompanyRequest;
import com.example.insurance.api.dto.response.InsuranceCompanyResponse;
import com.example.insurance.api.entity.InsuranceCompany;
import com.example.insurance.api.enums.CompanyType;
import com.example.insurance.api.repository.InsuranceCompanyRepository;
import com.example.insurance.common.exception.ConflictException;
import com.example.insurance.common.exception.ResourceNotFoundException;
import com.example.insurance.web.mapper.InsuranceCompanyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceCompanyService {

    private final InsuranceCompanyRepository repository;
    private final InsuranceCompanyMapper mapper;

    public List<InsuranceCompanyResponse> getAllCompanies() {
        log.debug("Getting all insurance companies");
        return repository.findAll().stream()
                .filter(c -> !c.getDeleted())
                .map(mapper::toResponse)
                .toList();
    }

    public InsuranceCompanyResponse getCompanyById(Long id) {
        log.debug("Getting company by id: {}", id);
        InsuranceCompany company = repository.findById(id)
                .filter(c -> !c.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("InsuranceCompany", id));
        return mapper.toResponse(company);
    }

    public InsuranceCompanyResponse getCompanyByCode(String code) {
        log.debug("Getting company by code: {}", code);
        InsuranceCompany company = repository.findByCode(code)
                .filter(c -> !c.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("InsuranceCompany", code));
        return mapper.toResponse(company);
    }

    @Transactional
    public InsuranceCompanyResponse createCompany(InsuranceCompanyRequest request) {
        log.info("Creating new insurance company: {}", request.getName());
        
        if (repository.existsByCode(request.getCode())) {
            throw new ConflictException("Company code already exists: " + request.getCode());
        }
        
        InsuranceCompany company = mapper.toEntity(request);
        InsuranceCompany saved = repository.save(company);
        log.info("Created company with id: {}", saved.getId());
        
        return mapper.toResponse(saved);
    }

    @Transactional
    public InsuranceCompanyResponse updateCompany(Long id, InsuranceCompanyRequest request) {
        log.info("Updating company: {}", id);
        
        InsuranceCompany company = repository.findById(id)
                .filter(c -> !c.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("InsuranceCompany", id));
        
        if (!company.getCode().equals(request.getCode()) && repository.existsByCode(request.getCode())) {
            throw new ConflictException("Company code already exists: " + request.getCode());
        }
        
        company.setName(request.getName());
        company.setCode(request.getCode());
        company.setType(CompanyType.fromCode(request.getType()));
        company.setWebsite(request.getWebsite());
        
        InsuranceCompany updated = repository.save(company);
        log.info("Updated company: {}", id);
        
        return mapper.toResponse(updated);
    }

    @Transactional
    public void deleteCompany(Long id) {
        log.info("Deleting company: {}", id);
        
        InsuranceCompany company = repository.findById(id)
                .filter(c -> !c.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("InsuranceCompany", id));
        
        company.setDeleted(true);
        repository.save(company);
        
        log.info("Soft deleted company: {}", id);
    }
}
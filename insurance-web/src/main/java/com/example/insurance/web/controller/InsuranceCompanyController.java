
package com.example.insurance.web.controller;

import com.example.insurance.api.dto.request.InsuranceCompanyRequest;
import com.example.insurance.api.dto.response.InsuranceCompanyResponse;
import com.example.insurance.common.model.Result;
import com.example.insurance.web.service.InsuranceCompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Tag(name = "Insurance Companies", description = "保险公司管理接口")
public class InsuranceCompanyController {

    private final InsuranceCompanyService service;

    @GetMapping
    @Operation(summary = "查询所有保险公司")
    public Result<List<InsuranceCompanyResponse>> getAllCompanies() {
        log.info("GET /api/v1/companies");
        List<InsuranceCompanyResponse> companies = service.getAllCompanies();
        return Result.success(companies);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询保险公司")
    public Result<InsuranceCompanyResponse> getCompanyById(
            @Parameter(description = "保险公司ID") @PathVariable Long id) {
        log.info("GET /api/v1/companies/{}", id);
        InsuranceCompanyResponse company = service.getCompanyById(id);
        return Result.success(company);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "根据代码查询保险公司")
    public Result<InsuranceCompanyResponse> getCompanyByCode(
            @Parameter(description = "保险公司代码") @PathVariable String code) {
        log.info("GET /api/v1/companies/code/{}", code);
        InsuranceCompanyResponse company = service.getCompanyByCode(code);
        return Result.success(company);
    }

    @PostMapping
    @Operation(summary = "创建保险公司")
    public Result<InsuranceCompanyResponse> createCompany(
            @Valid @RequestBody InsuranceCompanyRequest request) {
        log.info("POST /api/v1/companies - {}", request.getName());
        InsuranceCompanyResponse company = service.createCompany(request);
        return Result.created(company);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新保险公司")
    public Result<InsuranceCompanyResponse> updateCompany(
            @Parameter(description = "保险公司ID") @PathVariable Long id,
            @Valid @RequestBody InsuranceCompanyRequest request) {
        log.info("PUT /api/v1/companies/{}", id);
        InsuranceCompanyResponse company = service.updateCompany(id, request);
        return Result.success(company);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除保险公司")
    public Result<String> deleteCompany(
            @Parameter(description = "保险公司ID") @PathVariable Long id) {
        log.info("DELETE /api/v1/companies/{}", id);
        service.deleteCompany(id);
        return Result.success("Company deleted");
    }
}
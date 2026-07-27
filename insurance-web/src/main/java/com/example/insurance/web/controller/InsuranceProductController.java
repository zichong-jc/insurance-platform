
package com.example.insurance.web.controller;

import com.example.insurance.api.dto.request.InsuranceProductRequest;
import com.example.insurance.api.dto.response.InsuranceProductResponse;
import com.example.insurance.common.model.PageResult;
import com.example.insurance.common.model.Result;
import com.example.insurance.web.service.InsuranceProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Insurance Products", description = "保险产品管理接口")
public class InsuranceProductController {

    private final InsuranceProductService service;

    @GetMapping
    @Operation(summary = "查询保险产品列表")
    public PageResult<InsuranceProductResponse> getProducts(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /api/v1/products?page={}&size={}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<InsuranceProductResponse> products = service.getProducts(pageRequest);
        return PageResult.success(products.getContent(), products.getTotalElements(), page, size);
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "根据公司ID查询产品")
    public PageResult<InsuranceProductResponse> getProductsByCompany(
            @Parameter(description = "公司ID") @PathVariable Long companyId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /api/v1/products/company/{}", companyId);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<InsuranceProductResponse> products = service.getProductsByCompany(companyId, pageRequest);
        return PageResult.success(products.getContent(), products.getTotalElements(), page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询产品")
    public Result<InsuranceProductResponse> getProductById(
            @Parameter(description = "产品ID") @PathVariable Long id) {
        log.info("GET /api/v1/products/{}", id);
        InsuranceProductResponse product = service.getProductById(id);
        return Result.success(product);
    }

    @PostMapping
    @Operation(summary = "创建保险产品")
    public Result<InsuranceProductResponse> createProduct(
            @Valid @RequestBody InsuranceProductRequest request) {
        log.info("POST /api/v1/products - {}", request.getName());
        InsuranceProductResponse product = service.createProduct(request);
        return Result.created(product);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新保险产品")
    public Result<InsuranceProductResponse> updateProduct(
            @Parameter(description = "产品ID") @PathVariable Long id,
            @Valid @RequestBody InsuranceProductRequest request) {
        log.info("PUT /api/v1/products/{}", id);
        InsuranceProductResponse product = service.updateProduct(id, request);
        return Result.success(product);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除保险产品")
    public Result<String> deleteProduct(
            @Parameter(description = "产品ID") @PathVariable Long id) {
        log.info("DELETE /api/v1/products/{}", id);
        service.deleteProduct(id);
        return Result.success("Product deleted");
    }
}
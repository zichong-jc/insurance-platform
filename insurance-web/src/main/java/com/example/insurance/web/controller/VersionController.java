
package com.example.insurance.web.controller;

import com.example.insurance.api.dto.request.InsuranceVersionRequest;
import com.example.insurance.api.dto.response.InsuranceVersionResponse;
import com.example.insurance.common.model.PageResult;
import com.example.insurance.common.model.Result;
import com.example.insurance.web.service.VersionService;
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
@RequestMapping("/api/v1/products/{productId}/versions")
@RequiredArgsConstructor
@Tag(name = "Product Versions", description = "产品版本管理接口")
public class VersionController {

    private final VersionService service;

    @GetMapping
    @Operation(summary = "查询产品版本列表")
    public PageResult<InsuranceVersionResponse> getVersions(
            @Parameter(description = "产品ID") @PathVariable Long productId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /api/v1/products/{}/versions", productId);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<InsuranceVersionResponse> versions = service.getVersionsByProduct(productId, pageRequest);
        return PageResult.success(versions.getContent(), versions.getTotalElements(), page, size);
    }

    @GetMapping("/{versionId}")
    @Operation(summary = "查询单个版本")
    public Result<InsuranceVersionResponse> getVersion(
            @Parameter(description = "产品ID") @PathVariable Long productId,
            @Parameter(description = "版本ID") @PathVariable Long versionId) {
        log.info("GET /api/v1/products/{}/versions/{}", productId, versionId);
        InsuranceVersionResponse version = service.getVersion(productId, versionId);
        return Result.success(version);
    }

    @PostMapping
    @Operation(summary = "创建版本")
    public Result<InsuranceVersionResponse> createVersion(
            @Parameter(description = "产品ID") @PathVariable Long productId,
            @Valid @RequestBody InsuranceVersionRequest request) {
        log.info("POST /api/v1/products/{}/versions", productId);
        InsuranceVersionResponse version = service.createVersion(productId, request);
        return Result.created(version);
    }

    @DeleteMapping("/{versionId}")
    @Operation(summary = "删除版本")
    public Result<String> deleteVersion(
            @Parameter(description = "产品ID") @PathVariable Long productId,
            @Parameter(description = "版本ID") @PathVariable Long versionId) {
        log.info("DELETE /api/v1/products/{}/versions/{}", productId, versionId);
        service.deleteVersion(productId, versionId);
        return Result.success("Version deleted");
    }
}
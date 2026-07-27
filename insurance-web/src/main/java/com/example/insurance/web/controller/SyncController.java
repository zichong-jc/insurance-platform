
package com.example.insurance.web.controller;

import com.example.insurance.api.dto.response.SyncLogResponse;
import com.example.insurance.common.model.PageResult;
import com.example.insurance.common.model.Result;
import com.example.insurance.sync.entity.SyncLog;
import com.example.insurance.sync.service.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
@Tag(name = "Sync", description = "数据同步接口")
public class SyncController {

    private final SyncService service;

    @PostMapping("/companies/{companyCode}")
    @Operation(summary = "同步指定保险公司数据")
    public Result<SyncLogResponse> syncCompany(
            @Parameter(description = "公司代码") @PathVariable String companyCode) {
        log.info("POST /api/v1/sync/companies/{}", companyCode);
        SyncLog syncLog = service.syncCompany(companyCode);
        return Result.success(toResponse(syncLog));
    }

    @PostMapping("/companies/{companyCode}/products/{productId}")
    @Operation(summary = "同步指定产品")
    public Result<SyncLogResponse> syncProduct(
            @Parameter(description = "公司代码") @PathVariable String companyCode,
            @Parameter(description = "产品ID") @PathVariable Long productId) {
        log.info("POST /api/v1/sync/companies/{}/products/{}", companyCode, productId);
        SyncLog syncLog = service.syncProduct(companyCode, productId);
        return Result.success(toResponse(syncLog));
    }

    @GetMapping("/logs")
    @Operation(summary = "查询同步日志")
    public PageResult<SyncLogResponse> getSyncLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /api/v1/sync/logs?page={}&size={}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "syncTime"));
        Page<SyncLog> logs = service.getSyncLogs(pageRequest);
        return PageResult.success(logs.getContent().stream().map(this::toResponse).toList(), 
                logs.getTotalElements(), page, size);
    }

    @GetMapping("/logs/{id}")
    @Operation(summary = "查询同步日志详情")
    public Result<SyncLogResponse> getSyncLog(
            @Parameter(description = "日志ID") @PathVariable Long id) {
        log.info("GET /api/v1/sync/logs/{}", id);
        SyncLog syncLog = service.getSyncLog(id);
        return Result.success(toResponse(syncLog));
    }

    @GetMapping("/logs/company/{companyId}")
    @Operation(summary = "查询指定公司的同步日志")
    public PageResult<SyncLogResponse> getSyncLogsByCompany(
            @Parameter(description = "公司ID") @PathVariable Long companyId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /api/v1/sync/logs/company/{}", companyId);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "syncTime"));
        Page<SyncLog> logs = service.getSyncLogsByCompany(companyId, pageRequest);
        return PageResult.success(logs.getContent().stream().map(this::toResponse).toList(),
                logs.getTotalElements(), page, size);
    }

    @GetMapping("/companies")
    @Operation(summary = "获取支持的保险公司列表")
    public Result<List<String>> getSupportedCompanies() {
        log.info("GET /api/v1/sync/companies");
        List<String> companies = service.getSupportedCompanies();
        return Result.success(companies);
    }

    private SyncLogResponse toResponse(SyncLog log) {
        return SyncLogResponse.builder()
                .id(log.getId())
                .companyId(log.getCompanyId())
                .companyName(log.getCompanyName())
                .productId(log.getProductId())
                .productName(log.getProductName())
                .status(log.getStatus().name())
                .message(log.getMessage())
                .syncTime(log.getSyncTime())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
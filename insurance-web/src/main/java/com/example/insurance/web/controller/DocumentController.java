
package com.example.insurance.web.controller;

import com.example.insurance.api.dto.response.DocumentFileResponse;
import com.example.insurance.api.dto.response.DocumentParseResultResponse;
import com.example.insurance.common.model.PageResult;
import com.example.insurance.common.model.Result;
import com.example.insurance.web.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "文档管理接口")
public class DocumentController {

    private final DocumentService service;

    @GetMapping
    @Operation(summary = "查询文档列表")
    public PageResult<DocumentFileResponse> getDocuments(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /api/v1/documents?page={}&size={}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DocumentFileResponse> documents = service.getDocuments(pageRequest);
        return PageResult.success(documents.getContent(), documents.getTotalElements(), page, size);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "根据产品ID查询文档")
    public PageResult<DocumentFileResponse> getDocumentsByProduct(
            @Parameter(description = "产品ID") @PathVariable Long productId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /api/v1/documents/product/{}", productId);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DocumentFileResponse> documents = service.getDocumentsByProduct(productId, pageRequest);
        return PageResult.success(documents.getContent(), documents.getTotalElements(), page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询文档详情")
    public Result<DocumentFileResponse> getDocument(
            @Parameter(description = "文档ID") @PathVariable Long id) {
        log.info("GET /api/v1/documents/{}", id);
        DocumentFileResponse document = service.getDocument(id);
        return Result.success(document);
    }

    @PostMapping("/upload")
    @Operation(summary = "上传文档")
    public Result<DocumentFileResponse> uploadDocument(
            @Parameter(description = "产品ID") @RequestParam Long productId,
            @Parameter(description = "版本ID") @RequestParam(required = false) Long versionId,
            @Parameter(description = "文档文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "文档类型") @RequestParam(required = false, defaultValue = "IP") String documentType) {
        log.info("POST /api/v1/documents/upload for product: {}", productId);
        DocumentFileResponse document = service.uploadDocument(productId, versionId, file, documentType);
        return Result.created(document);
    }

    @PostMapping("/{id}/parse")
    @Operation(summary = "解析文档")
    public Result<DocumentParseResultResponse> parseDocument(
            @Parameter(description = "文档ID") @PathVariable Long id) {
        log.info("POST /api/v1/documents/{}/parse", id);
        DocumentParseResultResponse result = service.parseDocument(id);
        return Result.success(result);
    }

    @GetMapping("/{id}/parse-result")
    @Operation(summary = "查询解析结果")
    public Result<DocumentParseResultResponse> getParseResult(
            @Parameter(description = "文档ID") @PathVariable Long id) {
        log.info("GET /api/v1/documents/{}/parse-result", id);
        DocumentParseResultResponse result = service.getParseResult(id);
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文档")
    public Result<String> deleteDocument(
            @Parameter(description = "文档ID") @PathVariable Long id) {
        log.info("DELETE /api/v1/documents/{}", id);
        service.deleteDocument(id);
        return Result.success("Document deleted");
    }
}
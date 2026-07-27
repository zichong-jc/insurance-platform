
package com.example.insurance.web.controller;

import com.example.insurance.api.dto.request.ProductCompareRequest;
import com.example.insurance.api.dto.response.ProductCompareResponse;
import com.example.insurance.common.model.Result;
import com.example.insurance.web.service.CompareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/compare")
@RequiredArgsConstructor
@Tag(name = "Product Compare", description = "产品比较接口")
public class CompareController {

    private final CompareService service;

    @PostMapping("/products")
    @Operation(summary = "比较多个保险产品")
    public Result<ProductCompareResponse> compareProducts(@Valid @RequestBody ProductCompareRequest request) {
        log.info("POST /api/v1/compare/products - productIds: {}", request.getProductIds());
        ProductCompareResponse response = service.compareProducts(request.getProductIds(), request.getCompareFields());
        return Result.success(response);
    }
}

package com.example.insurance.web.controller;

import com.example.insurance.api.dto.request.AiChatRequest;
import com.example.insurance.api.dto.response.AiChatResponse;
import com.example.insurance.common.model.Result;
import com.example.insurance.web.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Services", description = "AI智能服务接口")
public class AiController {

    private final AiChatService service;

    @PostMapping("/chat")
    @Operation(summary = "AI智能问答")
    public Result<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        log.info("POST /api/v1/ai/chat - question: {}", request.getQuestion());
        AiChatResponse response = service.chat(request);
        return Result.success(response);
    }
}
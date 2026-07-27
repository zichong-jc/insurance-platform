
package com.example.insurance.web.service;

import com.example.insurance.api.dto.request.AiChatRequest;
import com.example.insurance.api.dto.response.AiChatResponse;
import com.example.insurance.ai.service.AiService;
import com.example.insurance.ai.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final AiService aiService;
    private final RagService ragService;

    public AiChatResponse chat(AiChatRequest request) {
        log.info("Processing AI chat request: {}", request.getQuestion());
        
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        
        String answer;
        if (Boolean.TRUE.equals(request.getEnableRag())) {
            answer = ragService.queryWithRag(request.getQuestion(), 5);
        } else {
            answer = aiService.directQuery(request.getQuestion());
        }
        
        return AiChatResponse.builder()
                .answer(answer)
                .sessionId(sessionId)
                .timestamp(Instant.now())
                .build();
    }
}
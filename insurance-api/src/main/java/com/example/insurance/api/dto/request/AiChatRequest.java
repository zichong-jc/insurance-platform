
package com.example.insurance.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {

    @NotBlank(message = "Question is required")
    private String question;

    private String sessionId;

    private Integer maxTokens;

    private Double temperature;

    private Boolean enableRag;
}
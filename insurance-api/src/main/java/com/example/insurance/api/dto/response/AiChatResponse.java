
package com.example.insurance.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {

    private String answer;
    private String sessionId;
    private List<DocumentReference> references;
    private Instant timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentReference {
        private Long documentId;
        private String fileName;
        private String productName;
        private String snippet;
    }
}
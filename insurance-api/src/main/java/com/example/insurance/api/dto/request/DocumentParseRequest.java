
package com.example.insurance.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentParseRequest {

    @NotNull(message = "Document ID is required")
    private Long documentId;

    private String parseType;

    private Boolean enableAiSummary;
}
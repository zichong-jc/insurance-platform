
package com.example.insurance.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentParseResultResponse {

    private Long id;
    private Long fileId;
    private String fileName;
    private Long productId;
    private String productName;
    private Map<String, Object> parseData;
    private String aiSummary;
    private Instant parseTime;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}

package com.example.insurance.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentFileResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Long versionId;
    private String versionNumber;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String hash;
    private String documentType;
    private String documentTypeDescription;
    private String minioBucket;
    private String minioObjectKey;
    private Instant createdAt;
    private Instant updatedAt;
}

package com.example.insurance.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    private Long versionId;

    @NotBlank(message = "File name is required")
    private String fileName;

    private String documentType;

    private String hash;
}
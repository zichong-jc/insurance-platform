
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
public class InsuranceVersionRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Version number is required")
    private String versionNumber;

    @NotBlank(message = "Download URL is required")
    private String downloadUrl;

    private String hash;
}
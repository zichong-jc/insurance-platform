
package com.example.insurance.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceProductRequest {

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name cannot exceed 200 characters")
    private String name;

    @NotBlank(message = "Product type is required")
    private String type;

    private String status;
}
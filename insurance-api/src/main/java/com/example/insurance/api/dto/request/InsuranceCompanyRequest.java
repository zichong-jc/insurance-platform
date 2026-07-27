
package com.example.insurance.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceCompanyRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 100, message = "Company name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Company code is required")
    @Size(max = 20, message = "Company code cannot exceed 20 characters")
    private String code;

    private String type;

    @Size(max = 255, message = "Website cannot exceed 255 characters")
    private String website;
}
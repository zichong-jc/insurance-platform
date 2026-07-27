
package com.example.insurance.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCompareRequest {

    @NotEmpty(message = "Product IDs cannot be empty")
    private List<Long> productIds;

    private List<String> compareFields;
}
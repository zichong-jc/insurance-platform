
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
public class InsuranceVersionResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String versionNumber;
    private String hash;
    private String downloadUrl;
    private Instant syncTime;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
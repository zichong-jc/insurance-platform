
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
public class SyncLogResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long productId;
    private String productName;
    private String status;
    private String message;
    private Instant syncTime;
    private Instant createdAt;
}
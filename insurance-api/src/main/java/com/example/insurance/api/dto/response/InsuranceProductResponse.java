
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
public class InsuranceProductResponse {

    private Long id;
    private String name;
    private String type;
    private String typeDescription;
    private String status;
    private Long companyId;
    private String companyName;
    private Integer versionCount;
    private Instant createdAt;
    private Instant updatedAt;
}
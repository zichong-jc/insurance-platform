
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
public class InsuranceCompanyResponse {

    private Long id;
    private String name;
    private String code;
    private String type;
    private String typeDescription;
    private String website;
    private Instant createdAt;
    private Instant updatedAt;
}
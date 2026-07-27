
package com.example.insurance.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncRequest {

    private String companyCode;
    
    private Long productId;
    
    private Boolean forceRefresh;
}

package com.example.insurance.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCompareResponse {

    private List<ProductComparison> products;
    private List<String> compareFields;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductComparison {
        private Long productId;
        private String productName;
        private String companyName;
        private String productType;
        private Map<String, Object> attributes;
        private List<VersionInfo> versions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VersionInfo {
        private String versionNumber;
        private String hash;
        private String documentCount;
    }
}
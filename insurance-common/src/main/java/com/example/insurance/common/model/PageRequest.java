
package com.example.insurance.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 10;

    private String sortBy;

    @Builder.Default
    private String sortDirection = "ASC";

    public org.springframework.data.domain.PageRequest toSpringPageRequest() {
        return org.springframework.data.domain.PageRequest.of(
                page,
                size,
                org.springframework.data.domain.Sort.by(
                        sortDirection.equalsIgnoreCase("DESC") 
                                ? org.springframework.data.domain.Sort.Direction.DESC 
                                : org.springframework.data.domain.Sort.Direction.ASC,
                        sortBy != null ? sortBy : "createdAt"
                )
        );
    }
}
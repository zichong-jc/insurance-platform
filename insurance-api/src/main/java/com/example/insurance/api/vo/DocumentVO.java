
package com.example.insurance.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVO {

    private Long id;
    private String fileName;
    private Long fileSize;
    private String documentType;
    private String hash;
    private Instant uploadTime;
    private String status;
}
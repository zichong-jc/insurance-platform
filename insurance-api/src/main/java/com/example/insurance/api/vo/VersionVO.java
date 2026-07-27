
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
public class VersionVO {

    private String versionNumber;
    private String hash;
    private Instant syncTime;
    private String status;
    private String downloadUrl;
}
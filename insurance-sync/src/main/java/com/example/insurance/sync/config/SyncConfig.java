
package com.example.insurance.sync.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "sync")
public class SyncConfig {

    private Boolean enabled = true;
    private String cron = "0 0 2 * * ?";
    private Integer timeoutMinutes = 30;
    private Integer retryCount = 3;
    private Long retryDelaySeconds = 60L;
}

package com.example.insurance.parser.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "parser")
public class ParserConfig {

    private Integer maxFileSizeMb = 50;
    private Integer maxPages = 500;
    private Boolean enableAiParse = true;
    private Boolean enableSummary = true;

    public long getMaxFileSizeBytes() {
        return (long) maxFileSizeMb * 1024 * 1024;
    }
}
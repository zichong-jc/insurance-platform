
package com.example.insurance.web;

import com.example.insurance.ai.AiModuleConfiguration;
import com.example.insurance.parser.ParserModuleConfiguration;
import com.example.insurance.sync.SyncModuleConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties
@EnableAsync
@Import({
    AiModuleConfiguration.class,
    ParserModuleConfiguration.class,
    SyncModuleConfiguration.class
})
public class InsuranceApplication {

    public static void main(String[] args) {
        log.info("Starting Insurance Platform Application");
        SpringApplication.run(InsuranceApplication.class, args);
        log.info("Insurance Platform Application started successfully");
    }
}
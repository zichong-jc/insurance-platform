
package com.example.insurance.sync;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(basePackages = "com.example.insurance.sync")
public class SyncModuleConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
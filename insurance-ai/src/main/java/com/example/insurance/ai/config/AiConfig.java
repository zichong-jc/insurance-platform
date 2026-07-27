
package com.example.insurance.ai.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiConfig {

    private String provider = "openai";
    private String apiKey;
    private String model = "gpt-4o-mini";
    private String embeddingModel = "text-embedding-3-small";
    private Double temperature = 0.7;
    private Integer maxTokens = 4096;

    @Bean
    public ChatModel chatModel() {
        log.info("Initializing AI chat model: {} with provider: {}", model, provider);
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
    }
}
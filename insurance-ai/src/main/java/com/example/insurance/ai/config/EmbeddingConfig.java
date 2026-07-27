
package com.example.insurance.ai.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class EmbeddingConfig {

    private String apiKey;
    private String embeddingModel = "text-embedding-3-small";
    private Integer dimensions = 1536;

    @Bean
    public EmbeddingModel embeddingModel() {
        log.info("Initializing embedding model: {}", embeddingModel);
        return OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(embeddingModel)
                .dimensions(dimensions)
                .build();
    }
}
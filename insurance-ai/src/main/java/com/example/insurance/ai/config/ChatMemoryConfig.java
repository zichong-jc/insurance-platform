
package com.example.insurance.ai.config;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "ai.memory")
public class ChatMemoryConfig {

    private Integer maxMessages = 20;
    private Long ttlMinutes = 60L;

    @Bean
    public ChatMemory chatMemory() {
        log.info("Initializing chat memory with max messages: {}", maxMessages);
        return MessageWindowChatMemory.withMaxMessages(maxMessages);
    }
}
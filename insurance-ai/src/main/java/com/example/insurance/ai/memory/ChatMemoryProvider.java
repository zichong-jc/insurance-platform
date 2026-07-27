
package com.example.insurance.ai.memory;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMemoryProvider {

    private final Map<String, ChatMemory> memoryCache = new ConcurrentHashMap<>();

    @Value("${ai.memory.max-messages:20}")
    private int maxMessages;

    public ChatMemory getMemory(String sessionId) {
        return memoryCache.computeIfAbsent(sessionId, id -> {
            log.debug("Creating new chat memory for session: {}", id);
            return MessageWindowChatMemory.withMaxMessages(maxMessages);
        });
    }

    public void clearMemory(String sessionId) {
        memoryCache.remove(sessionId);
        log.debug("Cleared chat memory for session: {}", sessionId);
    }

    public void updateMemory(String sessionId, ChatMemory memory) {
        memoryCache.put(sessionId, memory);
    }
}
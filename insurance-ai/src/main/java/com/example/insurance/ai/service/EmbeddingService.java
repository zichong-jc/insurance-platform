
package com.example.insurance.ai.service;

import com.example.insurance.ai.embedding.EmbeddingProvider;
import dev.langchain4j.data.embedding.Embedding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingProvider embeddingProvider;

    public Embedding createEmbedding(String text) {
        log.debug("Creating embedding for text");
        return embeddingProvider.embed(text);
    }

    public List<Embedding> createEmbeddings(List<String> texts) {
        log.debug("Creating embeddings for {} texts", texts.size());
        return embeddingProvider.embedAll(texts);
    }

    public float[] getEmbeddingVector(String text) {
        Embedding embedding = embeddingProvider.embed(text);
        return embedding.vector();
    }

    public int getEmbeddingDimensions() {
        return embeddingProvider.getEmbeddingDimensions();
    }
}
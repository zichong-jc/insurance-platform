
package com.example.insurance.ai.embedding;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmbeddingProvider {

    private final EmbeddingModel embeddingModel;

    public Embedding embed(String text) {
        log.debug("Generating embedding for text of length: {}", text.length());
        Response<Embedding> response = embeddingModel.embed(text);
        return response.content();
    }

    public List<Embedding> embedAll(List<String> texts) {
        log.debug("Generating embeddings for {} texts", texts.size());
        List<TextSegment> segments = texts.stream()
                .map(TextSegment::from)
                .collect(Collectors.toList());
        Response<List<Embedding>> response = embeddingModel.embedAll(segments);
        return response.content();
    }

    public int getEmbeddingDimensions() {
        return 1536;
    }
}
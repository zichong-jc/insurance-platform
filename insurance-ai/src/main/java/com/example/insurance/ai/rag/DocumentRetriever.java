
package com.example.insurance.ai.rag;

import com.example.insurance.ai.embedding.EmbeddingProvider;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentRetriever {

    private final KnowledgeBase knowledgeBase;
    private final EmbeddingProvider embeddingProvider;

    public List<TextSegment> retrieve(String query, int topK) {
        log.debug("Retrieving documents for query: {}", query);
        
        Embedding queryEmbedding = embeddingProvider.embed(query);
        List<TextSegment> allSegments = knowledgeBase.getAllSegments();
        
        if (allSegments.isEmpty()) {
            log.warn("No documents found in knowledge base");
            return List.of();
        }

        return allSegments.stream()
                .map(segment -> new ScoredSegment(segment, calculateSimilarity(queryEmbedding, segment)))
                .sorted(Comparator.comparingDouble(ScoredSegment::similarity).reversed())
                .limit(topK)
                .map(ScoredSegment::segment)
                .collect(Collectors.toList());
    }

    private double calculateSimilarity(Embedding queryEmbedding, TextSegment segment) {
        Embedding segmentEmbedding = embeddingProvider.embed(segment.text());
        return cosineSimilarity(queryEmbedding.vector(), segmentEmbedding.vector());
    }

    private double cosineSimilarity(float[] vectorA, float[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < vectorA.length && i < vectorB.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }
        
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private record ScoredSegment(TextSegment segment, double similarity) {}
}
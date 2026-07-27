
package com.example.insurance.ai.service;

import com.example.insurance.ai.embedding.EmbeddingProvider;
import com.example.insurance.ai.rag.DocumentRetriever;
import com.example.insurance.ai.rag.KnowledgeBase;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final AiService aiService;
    private final DocumentRetriever documentRetriever;
    private final KnowledgeBase knowledgeBase;
    private final EmbeddingProvider embeddingProvider;

    public String queryWithRag(String question, int topK) {
        log.info("Executing RAG query: {}", question);
        
        List<TextSegment> relevantSegments = documentRetriever.retrieve(question, topK);
        
        if (relevantSegments.isEmpty()) {
            log.warn("No relevant documents found, using direct query");
            return aiService.directQuery(question);
        }

        String context = relevantSegments.stream()
                .map(segment -> segment.text())
                .reduce("", (a, b) -> a + "\n\n" + b);

        return aiService.generateAnswer(context, question);
    }

    public void addDocumentToKnowledgeBase(String documentId, String content, String productName) {
        log.info("Adding document to knowledge base: {}", documentId);
        
        Metadata metadata = Metadata.from("documentId", documentId);
        metadata.put("productName", productName);
        
        Document document = Document.from(content, metadata);
        knowledgeBase.addDocument(documentId, document);
        
        log.info("Document added successfully: {}", documentId);
    }

    public void removeDocumentFromKnowledgeBase(String documentId) {
        log.info("Removing document from knowledge base: {}", documentId);
        knowledgeBase.removeDocument(documentId);
    }

    public void clearKnowledgeBase() {
        log.info("Clearing knowledge base");
        knowledgeBase.clear();
    }

    public int getKnowledgeBaseStats() {
        return knowledgeBase.getDocumentCount();
    }

    public List<TextSegment> getRelevantSegments(String query, int topK) {
        return documentRetriever.retrieve(query, topK);
    }
}
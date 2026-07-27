
package com.example.insurance.ai.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class KnowledgeBase {

    private final ConcurrentHashMap<String, List<TextSegment>> documentSegments = new ConcurrentHashMap<>();
    private final DocumentSplitter documentSplitter = DocumentSplitters.recursive(1000, 200);

    public void addDocument(String documentId, Document document) {
        log.info("Adding document to knowledge base: {}", documentId);
        List<TextSegment> segments = documentSplitter.split(document);
        documentSegments.put(documentId, segments);
        log.info("Document {} split into {} segments", documentId, segments.size());
    }

    public void removeDocument(String documentId) {
        documentSegments.remove(documentId);
        log.info("Removed document from knowledge base: {}", documentId);
    }

    public List<TextSegment> getAllSegments() {
        List<TextSegment> allSegments = new ArrayList<>();
        documentSegments.values().forEach(allSegments::addAll);
        return allSegments;
    }

    public List<TextSegment> getSegmentsByDocument(String documentId) {
        return documentSegments.getOrDefault(documentId, List.of());
    }

    public int getDocumentCount() {
        return documentSegments.size();
    }

    public int getTotalSegmentCount() {
        return documentSegments.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    public void clear() {
        documentSegments.clear();
        log.info("Knowledge base cleared");
    }
}
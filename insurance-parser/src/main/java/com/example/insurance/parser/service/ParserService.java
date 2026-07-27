
package com.example.insurance.parser.service;

import com.example.insurance.api.enums.DocumentType;
import com.example.insurance.common.exception.ValidationException;
import com.example.insurance.parser.config.ParserConfig;
import com.example.insurance.parser.factory.ParserFactory;
import com.example.insurance.parser.parser.DocumentParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParserService {

    private final ParserFactory parserFactory;
    private final ParserConfig parserConfig;

    public String parseText(byte[] content, String documentType) {
        validateContent(content);
        
        log.info("Parsing text for document type: {}", documentType);
        DocumentParser parser = parserFactory.getParser(documentType);
        return parser.parseText(content);
    }

    public Map<String, Object> parseStructured(byte[] content, String documentType) {
        validateContent(content);
        
        log.info("Parsing structured data for document type: {}", documentType);
        DocumentParser parser = parserFactory.getParser(documentType);
        return parser.parseStructured(content);
    }

    public String generateSummary(byte[] content, String documentType) {
        validateContent(content);
        
        if (!parserConfig.getEnableSummary()) {
            log.warn("Summary generation is disabled");
            return "";
        }

        log.info("Generating summary for document type: {}", documentType);
        DocumentParser parser = parserFactory.getParser(documentType);
        return parser.generateSummary(content);
    }

    public Map<String, Object> parseWithSummary(byte[] content, String documentType) {
        validateContent(content);
        
        log.info("Parsing document with summary: {}", documentType);
        DocumentParser parser = parserFactory.getParser(documentType);
        
        Map<String, Object> result = parser.parseStructured(content);
        
        if (parserConfig.getEnableSummary()) {
            String summary = parser.generateSummary(content);
            result.put("summary", summary);
        }
        
        return result;
    }

    private void validateContent(byte[] content) {
        if (content == null || content.length == 0) {
            throw new ValidationException("Document content cannot be empty");
        }
        
        if (content.length > parserConfig.getMaxFileSizeBytes()) {
            throw new ValidationException("File size exceeds maximum allowed size of " 
                    + parserConfig.getMaxFileSizeMb() + "MB");
        }
    }

    public boolean supportsDocumentType(String documentType) {
        return parserFactory.hasParser(documentType);
    }
}
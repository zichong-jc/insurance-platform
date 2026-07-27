
package com.example.insurance.parser.parser;

import com.example.insurance.api.enums.DocumentType;

import java.util.Map;

public interface DocumentParser {

    String parseText(byte[] content);

    Map<String, Object> parseStructured(byte[] content);

    String generateSummary(byte[] content);

    DocumentType getSupportedType();

    boolean supports(String documentType);
}
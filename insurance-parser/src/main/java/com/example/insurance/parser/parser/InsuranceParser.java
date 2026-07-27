
package com.example.insurance.parser.parser;

import com.example.insurance.ai.service.AiService;
import com.example.insurance.api.enums.DocumentType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class InsuranceParser implements DocumentParser {

    private final AiService aiService;
    private final ObjectMapper objectMapper;

    @Override
    public String parseText(byte[] content) {
        log.info("Parsing insurance document text");
        try (PDDocument document = Loader.loadPDF(content)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            log.error("Failed to parse PDF text", e);
            throw new RuntimeException("Failed to parse PDF text", e);
        }
    }

    @Override
    public Map<String, Object> parseStructured(byte[] content) {
        log.info("Parsing insurance document structured data");
        String text = parseText(content);
        Map<String, Object> result = new HashMap<>();

        result.put("rawText", text);
        result.put("basicInfo", extractBasicInfo(text));
        result.put("coverage", extractCoverage(text));
        result.put("exclusions", extractExclusions(text));
        result.put("claimConditions", extractClaimConditions(text));
        result.put("premium", extractPremium(text));

        return result;
    }

    @Override
    public String generateSummary(byte[] content) {
        log.info("Generating insurance document summary");
        String text = parseText(content);
        return aiService.generateSummary(text);
    }

    @Override
    public DocumentType getSupportedType() {
        return DocumentType.INSURANCE_POLICY;
    }

    @Override
    public boolean supports(String documentType) {
        return DocumentType.INSURANCE_POLICY.name().equalsIgnoreCase(documentType)
                || DocumentType.PRODUCT_BROCHURE.name().equalsIgnoreCase(documentType)
                || DocumentType.CLAIM_GUIDE.name().equalsIgnoreCase(documentType);
    }

    private Map<String, Object> extractBasicInfo(String text) {
        Map<String, Object> info = new HashMap<>();
        
        Pattern namePattern = Pattern.compile("产品名称[：:]([\\u4e00-\\u9fa5a-zA-Z0-9\\s]+)");
        Matcher matcher = namePattern.matcher(text);
        if (matcher.find()) {
            info.put("productName", matcher.group(1).trim());
        }

        Pattern companyPattern = Pattern.compile("保险公司[：:]([\\u4e00-\\u9fa5a-zA-Z\\s]+)");
        matcher = companyPattern.matcher(text);
        if (matcher.find()) {
            info.put("insuranceCompany", matcher.group(1).trim());
        }

        Pattern periodPattern = Pattern.compile("保障期限[：:]([\\u4e00-\\u9fa50-9\\s]+)");
        matcher = periodPattern.matcher(text);
        if (matcher.find()) {
            info.put("coveragePeriod", matcher.group(1).trim());
        }

        Pattern agePattern = Pattern.compile("投保年龄[：:]([0-9]+)至([0-9]+)周岁");
        matcher = agePattern.matcher(text);
        if (matcher.find()) {
            info.put("minAge", Integer.parseInt(matcher.group(1)));
            info.put("maxAge", Integer.parseInt(matcher.group(2)));
        }

        return info;
    }

    private Map<String, Object> extractCoverage(String text) {
        Map<String, Object> coverage = new HashMap<>();
        
        Pattern amountPattern = Pattern.compile("基本保额[：:]([0-9,]+)");
        Matcher matcher = amountPattern.matcher(text);
        if (matcher.find()) {
            coverage.put("basicAmount", matcher.group(1).replace(",", ""));
        }

        Pattern itemsPattern = Pattern.compile("保障范围[：:]([\\s\\S]*?)(?:\\n\\n|免责|$)");
        matcher = itemsPattern.matcher(text);
        if (matcher.find()) {
            coverage.put("items", matcher.group(1).trim());
        }

        return coverage;
    }

    private Map<String, Object> extractExclusions(String text) {
        Map<String, Object> exclusions = new HashMap<>();
        
        Pattern exclusionPattern = Pattern.compile("免责条款[：:]([\\s\\S]*?)(?:\\n\\n|理赔|保障|$)");
        Matcher matcher = exclusionPattern.matcher(text);
        if (matcher.find()) {
            exclusions.put("content", matcher.group(1).trim());
        }

        return exclusions;
    }

    private Map<String, Object> extractClaimConditions(String text) {
        Map<String, Object> conditions = new HashMap<>();
        
        Pattern claimPattern = Pattern.compile("理赔条件[：:]([\\s\\S]*?)(?:\\n\\n|免责|保障|$)");
        Matcher matcher = claimPattern.matcher(text);
        if (matcher.find()) {
            conditions.put("requirements", matcher.group(1).trim());
        }

        return conditions;
    }

    private Map<String, Object> extractPremium(String text) {
        Map<String, Object> premium = new HashMap<>();
        
        Pattern pricePattern = Pattern.compile("保费[：:]([0-9,]+)");
        Matcher matcher = pricePattern.matcher(text);
        if (matcher.find()) {
            premium.put("amount", matcher.group(1).replace(",", ""));
        }

        Pattern paymentPattern = Pattern.compile("缴费方式[：:]([\\u4e00-\\u9fa5\\s]+)");
        matcher = paymentPattern.matcher(text);
        if (matcher.find()) {
            premium.put("paymentMethod", matcher.group(1).trim());
        }

        return premium;
    }
}
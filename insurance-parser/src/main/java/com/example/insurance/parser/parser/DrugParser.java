
package com.example.insurance.parser.parser;

import com.example.insurance.ai.service.AiService;
import com.example.insurance.api.enums.DocumentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class DrugParser implements DocumentParser {

    private final AiService aiService;

    @Override
    public String parseText(byte[] content) {
        log.info("Parsing drug document text");
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
        log.info("Parsing drug document structured data");
        String text = parseText(content);
        Map<String, Object> result = new HashMap<>();

        result.put("rawText", text);
        result.put("drugInfo", extractDrugInfo(text));
        result.put("indications", extractIndications(text));
        result.put("dosage", extractDosage(text));
        result.put("adverseReactions", extractAdverseReactions(text));
        result.put("contraindications", extractContraindications(text));

        return result;
    }

    @Override
    public String generateSummary(byte[] content) {
        log.info("Generating drug document summary");
        String text = parseText(content);
        return aiService.generateSummary(text);
    }

    @Override
    public DocumentType getSupportedType() {
        return DocumentType.DRUG_MANUAL;
    }

    @Override
    public boolean supports(String documentType) {
        return DocumentType.DRUG_MANUAL.name().equalsIgnoreCase(documentType);
    }

    private Map<String, Object> extractDrugInfo(String text) {
        Map<String, Object> info = new HashMap<>();

        Pattern namePattern = Pattern.compile("药品名称[：:]([\\u4e00-\\u9fa5a-zA-Z\\s]+)");
        Matcher matcher = namePattern.matcher(text);
        if (matcher.find()) {
            info.put("drugName", matcher.group(1).trim());
        }

        Pattern genericPattern = Pattern.compile("通用名称[：:]([\\u4e00-\\u9fa5a-zA-Z\\s]+)");
        matcher = genericPattern.matcher(text);
        if (matcher.find()) {
            info.put("genericName", matcher.group(1).trim());
        }

        Pattern brandPattern = Pattern.compile("商品名称[：:]([\\u4e00-\\u9fa5a-zA-Z\\s]+)");
        matcher = brandPattern.matcher(text);
        if (matcher.find()) {
            info.put("brandName", matcher.group(1).trim());
        }

        Pattern specPattern = Pattern.compile("规格[：:]([0-9.]+[a-zA-Z]+)");
        matcher = specPattern.matcher(text);
        if (matcher.find()) {
            info.put("specification", matcher.group(1).trim());
        }

        return info;
    }

    private Map<String, Object> extractIndications(String text) {
        Map<String, Object> indications = new HashMap<>();

        Pattern indicationPattern = Pattern.compile("适应症[：:]([\\s\\S]*?)(?:\\n\\n|用法|用量|$)");
        Matcher matcher = indicationPattern.matcher(text);
        if (matcher.find()) {
            indications.put("content", matcher.group(1).trim());
        }

        return indications;
    }

    private Map<String, Object> extractDosage(String text) {
        Map<String, Object> dosage = new HashMap<>();

        Pattern usagePattern = Pattern.compile("用法用量[：:]([\\s\\S]*?)(?:\\n\\n|不良反应|禁忌|$)");
        Matcher matcher = usagePattern.matcher(text);
        if (matcher.find()) {
            dosage.put("content", matcher.group(1).trim());
        }

        return dosage;
    }

    private Map<String, Object> extractAdverseReactions(String text) {
        Map<String, Object> reactions = new HashMap<>();

        Pattern reactionPattern = Pattern.compile("不良反应[：:]([\\s\\S]*?)(?:\\n\\n|禁忌|注意|$)");
        Matcher matcher = reactionPattern.matcher(text);
        if (matcher.find()) {
            reactions.put("content", matcher.group(1).trim());
        }

        return reactions;
    }

    private Map<String, Object> extractContraindications(String text) {
        Map<String, Object> contraindications = new HashMap<>();

        Pattern contraPattern = Pattern.compile("禁忌[：:]([\\s\\S]*?)(?:\\n\\n|注意|药物|$)");
        Matcher matcher = contraPattern.matcher(text);
        if (matcher.find()) {
            contraindications.put("content", matcher.group(1).trim());
        }

        return contraindications;
    }
}
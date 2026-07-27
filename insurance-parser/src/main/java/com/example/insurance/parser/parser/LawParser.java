
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
public class LawParser implements DocumentParser {

    private final AiService aiService;

    @Override
    public String parseText(byte[] content) {
        log.info("Parsing law document text");
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
        log.info("Parsing law document structured data");
        String text = parseText(content);
        Map<String, Object> result = new HashMap<>();

        result.put("rawText", text);
        result.put("lawInfo", extractLawInfo(text));
        result.put("chapters", extractChapters(text));
        result.put("articles", extractArticles(text));

        return result;
    }

    @Override
    public String generateSummary(byte[] content) {
        log.info("Generating law document summary");
        String text = parseText(content);
        return aiService.generateSummary(text);
    }

    @Override
    public DocumentType getSupportedType() {
        return DocumentType.LAW_REGULATION;
    }

    @Override
    public boolean supports(String documentType) {
        return DocumentType.LAW_REGULATION.name().equalsIgnoreCase(documentType);
    }

    private Map<String, Object> extractLawInfo(String text) {
        Map<String, Object> info = new HashMap<>();

        Pattern namePattern = Pattern.compile("(中华人民共和国[\\u4e00-\\u9fa5]+法)");
        Matcher matcher = namePattern.matcher(text);
        if (matcher.find()) {
            info.put("lawName", matcher.group(1).trim());
        }

        Pattern datePattern = Pattern.compile("颁布日期[：:]([0-9]{4}年[0-9]{1,2}月[0-9]{1,2}日)");
        matcher = datePattern.matcher(text);
        if (matcher.find()) {
            info.put("promulgationDate", matcher.group(1).trim());
        }

        Pattern effectivePattern = Pattern.compile("实施日期[：:]([0-9]{4}年[0-9]{1,2}月[0-9]{1,2}日)");
        matcher = effectivePattern.matcher(text);
        if (matcher.find()) {
            info.put("effectiveDate", matcher.group(1).trim());
        }

        return info;
    }

    private Map<String, Object> extractChapters(String text) {
        Map<String, Object> chapters = new HashMap<>();

        Pattern chapterPattern = Pattern.compile("第([一二三四五六七八九十]+)章\\s+([\\u4e00-\\u9fa5]+)");
        Matcher matcher = chapterPattern.matcher(text);
        while (matcher.find()) {
            chapters.put("chapter" + matcher.group(1), matcher.group(2).trim());
        }

        return chapters;
    }

    private Map<String, Object> extractArticles(String text) {
        Map<String, Object> articles = new HashMap<>();

        Pattern articlePattern = Pattern.compile("第([0-9]+)条[：:]([\\s\\S]*?)(?:第[0-9]+条|$)");
        Matcher matcher = articlePattern.matcher(text);
        int count = 0;
        while (matcher.find() && count < 100) {
            articles.put("article" + matcher.group(1), matcher.group(2).trim());
            count++;
        }

        return articles;
    }
}

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
public class ChipParser implements DocumentParser {

    private final AiService aiService;

    @Override
    public String parseText(byte[] content) {
        log.info("Parsing chip document text");
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
        log.info("Parsing chip document structured data");
        String text = parseText(content);
        Map<String, Object> result = new HashMap<>();

        result.put("rawText", text);
        result.put("chipInfo", extractChipInfo(text));
        result.put("specifications", extractSpecifications(text));
        result.put("electrical", extractElectrical(text));
        result.put("mechanical", extractMechanical(text));
        result.put("package", extractPackage(text));

        return result;
    }

    @Override
    public String generateSummary(byte[] content) {
        log.info("Generating chip document summary");
        String text = parseText(content);
        return aiService.generateSummary(text);
    }

    @Override
    public DocumentType getSupportedType() {
        return DocumentType.CHIP_DATASHEET;
    }

    @Override
    public boolean supports(String documentType) {
        return DocumentType.CHIP_DATASHEET.name().equalsIgnoreCase(documentType);
    }

    private Map<String, Object> extractChipInfo(String text) {
        Map<String, Object> info = new HashMap<>();

        Pattern partPattern = Pattern.compile("Part Number[：:]([a-zA-Z0-9\\-]+)");
        Matcher matcher = partPattern.matcher(text);
        if (matcher.find()) {
            info.put("partNumber", matcher.group(1).trim());
        }

        Pattern descPattern = Pattern.compile("Description[：:]([\\u4e00-\\u9fa5a-zA-Z0-9\\s]+)");
        matcher = descPattern.matcher(text);
        if (matcher.find()) {
            info.put("description", matcher.group(1).trim());
        }

        Pattern mfgPattern = Pattern.compile("Manufacturer[：:]([a-zA-Z\\s]+)");
        matcher = mfgPattern.matcher(text);
        if (matcher.find()) {
            info.put("manufacturer", matcher.group(1).trim());
        }

        return info;
    }

    private Map<String, Object> extractSpecifications(String text) {
        Map<String, Object> specs = new HashMap<>();

        Pattern familyPattern = Pattern.compile("Product Family[：:]([a-zA-Z\\s]+)");
        Matcher matcher = familyPattern.matcher(text);
        if (matcher.find()) {
            specs.put("productFamily", matcher.group(1).trim());
        }

        Pattern corePattern = Pattern.compile("Core[：:]([a-zA-Z0-9\\s]+)");
        matcher = corePattern.matcher(text);
        if (matcher.find()) {
            specs.put("core", matcher.group(1).trim());
        }

        Pattern speedPattern = Pattern.compile("Speed[：:]([0-9]+)MHz");
        matcher = speedPattern.matcher(text);
        if (matcher.find()) {
            specs.put("speedMHz", Integer.parseInt(matcher.group(1)));
        }

        return specs;
    }

    private Map<String, Object> extractElectrical(String text) {
        Map<String, Object> electrical = new HashMap<>();

        Pattern voltagePattern = Pattern.compile("Voltage[：:]([0-9.]+)V");
        Matcher matcher = voltagePattern.matcher(text);
        if (matcher.find()) {
            electrical.put("voltage", Double.parseDouble(matcher.group(1)));
        }

        Pattern currentPattern = Pattern.compile("Current[：:]([0-9.]+)mA");
        matcher = currentPattern.matcher(text);
        if (matcher.find()) {
            electrical.put("current", Double.parseDouble(matcher.group(1)));
        }

        return electrical;
    }

    private Map<String, Object> extractMechanical(String text) {
        Map<String, Object> mechanical = new HashMap<>();

        Pattern tempPattern = Pattern.compile("Temperature Range[：:]([0-9\\-]+)to([0-9]+)");
        Matcher matcher = tempPattern.matcher(text);
        if (matcher.find()) {
            mechanical.put("minTemp", Integer.parseInt(matcher.group(1)));
            mechanical.put("maxTemp", Integer.parseInt(matcher.group(2)));
        }

        return mechanical;
    }

    private Map<String, Object> extractPackage(String text) {
        Map<String, Object> pkg = new HashMap<>();

        Pattern typePattern = Pattern.compile("Package[：:]([a-zA-Z0-9\\-]+)");
        Matcher matcher = typePattern.matcher(text);
        if (matcher.find()) {
            pkg.put("type", matcher.group(1).trim());
        }

        Pattern pinsPattern = Pattern.compile("Pin Count[：:]([0-9]+)");
        matcher = pinsPattern.matcher(text);
        if (matcher.find()) {
            pkg.put("pinCount", Integer.parseInt(matcher.group(1)));
        }

        return pkg;
    }
}
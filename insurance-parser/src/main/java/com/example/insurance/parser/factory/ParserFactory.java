
package com.example.insurance.parser.factory;

import com.example.insurance.api.enums.DocumentType;
import com.example.insurance.parser.parser.DocumentParser;
import com.example.insurance.parser.parser.InsuranceParser;
import com.example.insurance.parser.parser.DrugParser;
import com.example.insurance.parser.parser.ChipParser;
import com.example.insurance.parser.parser.LawParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ParserFactory {

    private final InsuranceParser insuranceParser;
    private final DrugParser drugParser;
    private final ChipParser chipParser;
    private final LawParser lawParser;

    public ParserFactory(InsuranceParser insuranceParser, DrugParser drugParser, 
                        ChipParser chipParser, LawParser lawParser) {
        this.insuranceParser = insuranceParser;
        this.drugParser = drugParser;
        this.chipParser = chipParser;
        this.lawParser = lawParser;
        log.info("ParserFactory initialized with {} parsers", 4);
    }

    public DocumentParser getParser(String documentType) {
        log.debug("Getting parser for document type: {}", documentType);
        
        if (insuranceParser.supports(documentType)) {
            return insuranceParser;
        } else if (drugParser.supports(documentType)) {
            return drugParser;
        } else if (chipParser.supports(documentType)) {
            return chipParser;
        } else if (lawParser.supports(documentType)) {
            return lawParser;
        }

        log.warn("No parser found for document type: {}, using default insurance parser", documentType);
        return insuranceParser;
    }

    public DocumentParser getParser(DocumentType documentType) {
        return getParser(documentType.name());
    }

    public List<DocumentParser> getAllParsers() {
        return List.of(insuranceParser, drugParser, chipParser, lawParser);
    }

    public boolean hasParser(String documentType) {
        return insuranceParser.supports(documentType)
                || drugParser.supports(documentType)
                || chipParser.supports(documentType)
                || lawParser.supports(documentType);
    }
}
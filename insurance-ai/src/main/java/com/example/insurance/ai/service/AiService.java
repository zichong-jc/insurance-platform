
package com.example.insurance.ai.service;

import com.example.insurance.ai.prompt.PromptTemplate;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatModel chatModel;

    public String generateSummary(String documentContent) {
        log.info("Generating summary for document");
        String prompt = PromptTemplate.DOCUMENT_SUMMARIZATION_TEMPLATE
                .replace("{document_content}", documentContent);
        ChatResponse response = chatModel.chat(List.of(UserMessage.from(prompt)));
        AiMessage aiMessage = response.aiMessage();
        return aiMessage != null ? aiMessage.text() : "";
    }

    public String analyzeInsurance(String documentContent, String question) {
        log.info("Analyzing insurance document for question: {}", question);
        String prompt = PromptTemplate.INSURANCE_ANALYSIS_TEMPLATE
                .replace("{document_content}", documentContent)
                .replace("{question}", question);
        ChatResponse response = chatModel.chat(List.of(UserMessage.from(prompt)));
        AiMessage aiMessage = response.aiMessage();
        return aiMessage != null ? aiMessage.text() : "";
    }

    public String compareProducts(String productA, String productB) {
        log.info("Comparing two insurance products");
        String prompt = PromptTemplate.PRODUCT_COMPARISON_TEMPLATE
                .replace("{product_a}", productA)
                .replace("{product_b}", productB);
        ChatResponse response = chatModel.chat(List.of(UserMessage.from(prompt)));
        AiMessage aiMessage = response.aiMessage();
        return aiMessage != null ? aiMessage.text() : "";
    }

    public String analyzeClaim(String policyContent, String claimInfo) {
        log.info("Analyzing claim application");
        String prompt = PromptTemplate.CLAIM_ANALYSIS_TEMPLATE
                .replace("{policy_content}", policyContent)
                .replace("{claim_info}", claimInfo);
        ChatResponse response = chatModel.chat(List.of(UserMessage.from(prompt)));
        AiMessage aiMessage = response.aiMessage();
        return aiMessage != null ? aiMessage.text() : "";
    }

    public String generateAnswer(String context, String question) {
        log.info("Generating answer with context");
        String prompt = PromptTemplate.RAG_ANSWER_TEMPLATE
                .replace("{context}", context)
                .replace("{question}", question);
        ChatResponse response = chatModel.chat(List.of(UserMessage.from(prompt)));
        AiMessage aiMessage = response.aiMessage();
        return aiMessage != null ? aiMessage.text() : "";
    }

    public String directQuery(String question) {
        log.info("Direct AI query: {}", question);
        ChatResponse response = chatModel.chat(List.of(UserMessage.from(question)));
        AiMessage aiMessage = response.aiMessage();
        return aiMessage != null ? aiMessage.text() : "";
    }
}
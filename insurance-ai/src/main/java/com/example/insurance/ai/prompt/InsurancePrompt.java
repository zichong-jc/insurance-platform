
package com.example.insurance.ai.prompt;

import dev.langchain4j.model.input.structured.StructuredPrompt;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@StructuredPrompt("""
        你是一位专业的保险知识助手。请根据用户的问题提供准确、专业的回答。
        
        用户问题：{{question}}
        
        如果需要参考文档，请使用提供的工具进行查询。
        
        回答要求：
        1. 准确引用保险条款内容
        2. 使用通俗易懂的语言
        3. 提供具体的条款依据
        4. 如果不确定，说明情况
        """)
public class InsurancePrompt {

    private String question;

    public static InsurancePrompt of(String question) {
        InsurancePrompt prompt = new InsurancePrompt();
        prompt.setQuestion(question);
        return prompt;
    }
}
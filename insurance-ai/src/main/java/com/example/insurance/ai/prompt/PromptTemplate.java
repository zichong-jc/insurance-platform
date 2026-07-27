
package com.example.insurance.ai.prompt;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PromptTemplate {

    public static final String INSURANCE_ANALYSIS_TEMPLATE = """
            你是一位专业的保险分析师。请根据提供的保险条款内容，分析并回答以下问题：
            
            保险条款内容：
            {document_content}
            
            用户问题：
            {question}
            
            请提供详细、准确的回答，并引用相关条款内容作为依据。
            """;

    public static final String DOCUMENT_SUMMARIZATION_TEMPLATE = """
            请对以下保险产品文档进行总结：
            
            文档内容：
            {document_content}
            
            请按照以下结构进行总结：
            1. 产品名称
            2. 保障范围
            3. 免责条款
            4. 理赔条件
            5. 重要说明
            
            总结应简洁明了，突出重点。
            """;

    public static final String RAG_ANSWER_TEMPLATE = """
            你是一个专业的保险知识问答助手。请根据提供的参考文档回答用户问题。
            
            参考文档：
            {context}
            
            用户问题：
            {question}
            
            请基于参考文档内容进行回答。如果参考文档中没有相关信息，请明确说明。
            回答要准确、简洁，并注明信息来源。
            """;

    public static final String CLAIM_ANALYSIS_TEMPLATE = """
            请根据提供的保险条款分析理赔申请是否符合条件：
            
            保险条款：
            {policy_content}
            
            理赔申请信息：
            {claim_info}
            
            请分析：
            1. 是否符合理赔条件
            2. 可能的拒赔原因
            3. 建议的处理方式
            """;

    public static final String PRODUCT_COMPARISON_TEMPLATE = """
            请对以下保险产品进行比较分析：
            
            产品A：
            {product_a}
            
            产品B：
            {product_b}
            
            请从以下方面进行比较：
            1. 保障范围
            2. 保费价格
            3. 理赔条件
            4. 免责条款
            5. 综合评价
            
            给出专业的比较分析结果。
            """;
}
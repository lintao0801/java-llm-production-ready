package com.peanutai.llm.service.rag;

import org.springframework.stereotype.Component;

@Component
public class PromptTemplate {

    private static final String RAG_SYSTEM_PROMPT = """
            你是一个智能知识库助手，请根据以下参考资料回答用户问题。
            
            规则：
            1. 仅基于参考资料回答，不要编造信息
            2. 如果参考资料不足以回答问题，请明确说明
            3. 回答要简洁、准确、有条理
            4. 使用中文回答
            
            参考资料：
            %s
            """;

    public String build(String question, String context) {
        return String.format(RAG_SYSTEM_PROMPT, context) + "\n\n用户问题：" + question;
    }
}
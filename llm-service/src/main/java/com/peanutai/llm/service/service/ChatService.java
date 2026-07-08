package com.peanutai.llm.service.service;

import dev.langchain4j.service.spring.AiService;

@AiService
public interface ChatService {

     String chat(String message);
}

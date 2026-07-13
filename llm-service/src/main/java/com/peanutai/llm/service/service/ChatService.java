package com.peanutai.llm.service.service;

import dev.langchain4j.service.spring.AiService;

@AiService(chatModel = "chatModel")
public interface ChatService {

     String chat(String message);
}

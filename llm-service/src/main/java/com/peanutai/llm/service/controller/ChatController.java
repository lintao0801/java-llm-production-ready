package com.peanutai.llm.service.controller;

import com.peanutai.llm.common.response.Result;
import com.peanutai.llm.service.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "普通AI助手")
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    @Resource
    private ChatService chatService;

    @Operation(summary = "简单问答")
    @GetMapping("/chat")
    public Result<String> chat(@RequestParam(value = "message",defaultValue = "一句话回答你是谁") String message) {
        String chat = chatService.chat(message);
        return Result.success(chat);
    }



}

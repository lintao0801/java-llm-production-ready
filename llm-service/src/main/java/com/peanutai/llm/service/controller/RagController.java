package com.peanutai.llm.service.controller;

import com.peanutai.llm.common.response.Result;
import com.peanutai.llm.service.model.dto.RagRequest;
import com.peanutai.llm.service.model.dto.RagResponse;
import com.peanutai.llm.service.service.RagService;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "RAG智能问答")
@RestController
@RequestMapping("/api/v1/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;

    @Operation(summary = "同步问答")
    @PostMapping("/query")
    public Result<RagResponse> query(@RequestBody RagRequest request) {
        RagResponse response = ragService.query(request.getQuestion(), request.getKnowledgeBaseId());
        return Result.success(response);
    }

    @Operation(summary = "流式问答（SSE）")
    @PostMapping("/query/stream")
    public SseEmitter queryStream(@RequestBody RagRequest request) {
        return ragService.queryStream(request.getQuestion(), request.getKnowledgeBaseId());
    }

}
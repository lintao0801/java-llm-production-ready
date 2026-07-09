package com.peanutai.llm.service.controller;

import com.peanutai.llm.common.response.Result;
import com.peanutai.llm.service.model.dto.RagRequest;
import com.peanutai.llm.service.model.dto.RagResponse;
import com.peanutai.llm.service.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "RAG智能问答")
@RestController
@RequestMapping("/api/v1/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;

    @Operation(summary = "同步问答（Naive RAG）")
    @PostMapping("/query")
    public Result<RagResponse> query(@RequestBody RagRequest request) {
        // 如果请求中带有 Advanced RAG 参数，使用 Advanced RAG Pipeline
        if (hasAdvancedRagParams(request)) {
            RagResponse response = ragService.query(request);
            return Result.success(response);
        }
        // 否则使用 Naive RAG（向后兼容）
        RagResponse response = ragService.query(request.getQuestion(), request.getKnowledgeBaseId());
        return Result.success(response);
    }

    @Operation(summary = "流式问答（SSE）")
    @PostMapping("/query/stream")
    public SseEmitter queryStream(@RequestBody RagRequest request) {
        // 如果请求中带有 Advanced RAG 参数，使用 Advanced RAG Pipeline
        if (hasAdvancedRagParams(request)) {
            return ragService.queryStream(request);
        }
        // 否则使用 Naive RAG（向后兼容）
        return ragService.queryStream(request.getQuestion(), request.getKnowledgeBaseId());
    }

    /**
     * 判断是否使用了 Advanced RAG 参数
     */
    private boolean hasAdvancedRagParams(RagRequest request) {
        return (request.getEnableQueryExpansion() != null && request.getEnableQueryExpansion())
                || (request.getEnableRerank() != null && request.getEnableRerank())
                || (request.getHistoryMessages() != null && !request.getHistoryMessages().isEmpty())
                || (request.getMetadataFilters() != null && !request.getMetadataFilters().isEmpty())
                || (request.getConversationId() != null && !request.getConversationId().isEmpty());
    }
}
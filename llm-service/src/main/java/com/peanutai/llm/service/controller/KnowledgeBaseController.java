package com.peanutai.llm.service.controller;

import com.peanutai.llm.common.response.Result;
import com.peanutai.llm.service.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "知识库管理")
@RestController
@RequestMapping("/api/v1/knowledge-bases")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final RagService ragService;

    @Operation(summary = "删除知识库")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        ragService.deleteKnowledgeBase(id);
        return Result.success("知识库已删除");
    }
}
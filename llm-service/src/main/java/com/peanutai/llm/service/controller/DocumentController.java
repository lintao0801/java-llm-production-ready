package com.peanutai.llm.service.controller;

import com.peanutai.llm.common.response.Result;
import com.peanutai.llm.service.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "文档管理")
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final RagService ragService;

    @Operation(summary = "上传文档")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file,
                                  @RequestParam("knowledgeBaseId") String knowledgeBaseId) {
        ragService.ingestDocument(file, knowledgeBaseId);
        return Result.success("文档上传成功");
    }

    @Operation(summary = "批量上传文档")
    @PostMapping("/upload/batch")
    public Result<String> uploadBatch(@RequestParam("files") List<MultipartFile> files,
                                       @RequestParam("knowledgeBaseId") String knowledgeBaseId) {
        ragService.ingestDocuments(files, knowledgeBaseId);
        return Result.success("批量上传成功");
    }
}
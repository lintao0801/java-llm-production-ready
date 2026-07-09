package com.peanutai.llm.service.service;

import com.peanutai.llm.service.model.dto.RagRequest;
import com.peanutai.llm.service.model.dto.RagResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface RagService {

    void ingestDocument(MultipartFile file, String knowledgeBaseId);

    void ingestDocuments(List<MultipartFile> files, String knowledgeBaseId);

    RagResponse query(String question, String knowledgeBaseId);

    SseEmitter queryStream(String question, String knowledgeBaseId);

    /**
     * Advanced RAG 同步问答 - 支持查询压缩、查询扩展、重排序、元数据过滤等
     */
    RagResponse query(RagRequest request);

    /**
     * Advanced RAG 流式问答（SSE）- 支持查询压缩、查询扩展、重排序、元数据过滤等
     */
    SseEmitter queryStream(RagRequest request);

    void deleteKnowledgeBase(String knowledgeBaseId);
}
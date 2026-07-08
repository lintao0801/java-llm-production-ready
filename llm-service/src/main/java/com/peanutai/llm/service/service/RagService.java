package com.peanutai.llm.service.service;

import com.peanutai.llm.service.model.dto.RagResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface RagService {

    void ingestDocument(MultipartFile file, String knowledgeBaseId);

    void ingestDocuments(List<MultipartFile> files, String knowledgeBaseId);

    RagResponse query(String question, String knowledgeBaseId);

    SseEmitter queryStream(String question, String knowledgeBaseId);

    void deleteKnowledgeBase(String knowledgeBaseId);
}
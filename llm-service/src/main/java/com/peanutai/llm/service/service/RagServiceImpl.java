package com.peanutai.llm.service.service;

import com.peanutai.llm.common.exception.BusinessException;
import com.peanutai.llm.common.exception.ErrorCode;
import com.peanutai.llm.service.model.dto.DocumentMatch;
import com.peanutai.llm.service.model.dto.DocumentSource;
import com.peanutai.llm.service.model.dto.RagResponse;
import com.peanutai.llm.service.protection.ContentSafetyService;
import com.peanutai.llm.service.rag.DocumentProcessor;
import com.peanutai.llm.service.rag.PromptTemplate;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.TokenUsage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RagServiceImpl implements RagService {

    private final DocumentProcessor documentProcessor;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;
    private final PromptTemplate promptTemplate;
    private final ContentSafetyService contentSafetyService;

    public RagServiceImpl(DocumentProcessor documentProcessor,
                          EmbeddingService embeddingService,
                          VectorStoreService vectorStoreService,
                          ChatModel chatModel,
                          StreamingChatModel streamingChatModel,
                          PromptTemplate promptTemplate,
                          ContentSafetyService contentSafetyService) {
        this.documentProcessor = documentProcessor;
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
        this.promptTemplate = promptTemplate;
        this.contentSafetyService = contentSafetyService;
    }

    @Override
    public void ingestDocument(MultipartFile file, String knowledgeBaseId) {
        log.info("开始处理文档入库: file={}, kbId={}", file.getOriginalFilename(), knowledgeBaseId);

        try {
            String content = documentProcessor.parse(file);
            List<TextSegment> chunks = documentProcessor.chunk(content);
            log.info("文档分块完成: 共{}个块", chunks.size());

            List<Embedding> embeddings = embeddingService.embedAll(
                    chunks.stream().map(TextSegment::text).collect(Collectors.toList()));
            vectorStoreService.addAll(knowledgeBaseId, chunks, embeddings);

            log.info("文档入库完成: file={}", file.getOriginalFilename());
        } catch (IOException e) {
            log.error("文档解析失败", e);
            throw new BusinessException(ErrorCode.DOCUMENT_PARSE_ERROR);
        }
    }

    @Override
    public void ingestDocuments(List<MultipartFile> files, String knowledgeBaseId) {
        for (MultipartFile file : files) {
            ingestDocument(file, knowledgeBaseId);
        }
    }

    @Override
    public RagResponse query(String question, String knowledgeBaseId) {
        long startTime = System.currentTimeMillis();
        // [1] 输入安全校验
        contentSafetyService.validateInput(question);
        // [2] 问题向量化
        Embedding questionEmbedding = embeddingService.embed(question);
        // [3] 向量检索Top-5
        List<DocumentMatch> matches = vectorStoreService.search(knowledgeBaseId, questionEmbedding, 5);
        // [4] 拼接上下文
        String context = matches.stream()
                .map(DocumentMatch::getContent)
                .collect(Collectors.joining("\n\n"));
        // [5] 构造Prompt
        String prompt = promptTemplate.build(question, context);
        // [6] 调用大模型
        ChatRequest chatRequest = ChatRequest.builder()
                .messages(UserMessage.from(prompt))
                .build();
        ChatResponse response = chatModel.chat(chatRequest);

        // [7] 输出审核
        String answer = response.aiMessage().text();
        contentSafetyService.auditOutput(answer);

        // [8] 构造响应（含Token用量、延迟、来源文档）
        long latency = System.currentTimeMillis() - startTime;
        TokenUsage tokenUsage = response.tokenUsage();

        return RagResponse.builder()
                .answer(answer)
                .sources(convertToSources(matches))
                .tokenUsage(com.peanutai.llm.service.model.dto.TokenUsage.builder()
                        .inputTokens(tokenUsage.inputTokenCount())
                        .outputTokens(tokenUsage.outputTokenCount())
                        .totalTokens(tokenUsage.totalTokenCount())
                        .build())
                .latencyMs(latency)
                .model("gpt-4o-mini")
                .build();
    }

    @Override
    public SseEmitter queryStream(String question, String knowledgeBaseId) {
        SseEmitter emitter = new SseEmitter(30000L);

        CompletableFuture.runAsync(() -> {
            try {
                contentSafetyService.validateInput(question);
                Embedding questionEmbedding = embeddingService.embed(question);
                List<DocumentMatch> matches = vectorStoreService.search(knowledgeBaseId, questionEmbedding, 5);

                String context = matches.stream()
                        .map(DocumentMatch::getContent)
                        .collect(Collectors.joining("\n\n"));

                String prompt = promptTemplate.build(question, context);

                streamingChatModel.chat(prompt, new StreamingChatResponseHandler() {
                    @Override
                    public void onPartialResponse(String partialResponse) {
                        try {
                            emitter.send(SseEmitter.event().name("message").data(partialResponse));
                        } catch (IOException e) {
                            log.error("SSE发送失败", e);
                        }
                    }

                    @Override
                    public void onCompleteResponse(ChatResponse completeResponse) {
                        try {
                            emitter.send(SseEmitter.event().name("sources").data(convertToSources(matches)));
                            emitter.complete();
                        } catch (IOException e) {
                            log.error("SSE完成发送失败", e);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        log.error("流式查询失败", error);
                        emitter.completeWithError(error);
                    }
                });
            } catch (Exception e) {
                log.error("流式查询异常", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @Override
    public void deleteKnowledgeBase(String knowledgeBaseId) {
        vectorStoreService.deleteCollection(knowledgeBaseId);
        log.info("知识库已删除: kbId={}", knowledgeBaseId);
    }

    private List<DocumentSource> convertToSources(List<DocumentMatch> matches) {
        return matches.stream()
                .map(m -> DocumentSource.builder()
                        .documentId(m.getDocumentId())
                        .fileName(m.getFileName())
                        .content(m.getContent())
                        .similarityScore(m.getScore())
                        .metadata(new HashMap<>())
                        .build())
                .collect(Collectors.toList());
    }
}
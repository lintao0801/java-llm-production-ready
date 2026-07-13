package com.peanutai.llm.service.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    private static final int EMBEDDING_BATCH_SIZE = 10;//阿里云 DashScope 单次 embedding 请求的文本数量有限制，最大为 10 条。
    public Embedding embed(String text) {
        return embeddingModel.embed(text).content();
    }

    public List<Embedding> embedAll(List<String> texts) {
        List<TextSegment> segments = texts.stream()
                .map(TextSegment::from)
                .collect(Collectors.toList());

        List<Embedding> allEmbeddings = new ArrayList<>();
        for (int i = 0; i < segments.size(); i += EMBEDDING_BATCH_SIZE) {
            List<TextSegment> batch = segments.subList(i, Math.min(i + EMBEDDING_BATCH_SIZE, segments.size()));
            log.debug("Embedding batch: {}-{} / {}", i, i + batch.size(), segments.size());
            List<Embedding> batchResult = embeddingModel.embedAll(batch).content();
            allEmbeddings.addAll(batchResult);
        }
        return allEmbeddings;
    }
}
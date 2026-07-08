package com.peanutai.llm.service.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public Embedding embed(String text) {
        return embeddingModel.embed(text).content();
    }

    public List<Embedding> embedAll(List<String> texts) {
        return embeddingModel.embedAll(
                texts.stream().map(TextSegment::from).collect(Collectors.toList())
        ).content();
    }
}
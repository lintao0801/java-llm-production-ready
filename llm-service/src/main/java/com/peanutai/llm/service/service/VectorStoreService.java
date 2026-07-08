package com.peanutai.llm.service.service;

import com.peanutai.llm.service.model.dto.DocumentMatch;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VectorStoreService {

    /**
     * 多知识库隔离字段：所有写入和查询都按 knowledgeBaseId 过滤，
     * 避免不同知识库的数据互相污染（修复原 InMemory 全局存储 + removeAll 误删问题）。
     */
    private static final String KB_ID_FIELD = "knowledgeBaseId";

    private final EmbeddingStore<TextSegment> embeddingStore;

    public VectorStoreService(EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingStore = embeddingStore;
    }

    public void addAll(String knowledgeBaseId, List<TextSegment> chunks, List<Embedding> embeddings) {
        if (chunks.size() != embeddings.size()) {
            throw new IllegalArgumentException("chunks 和 embeddings 数量不一致");
        }
        for (int i = 0; i < chunks.size(); i++) {
            TextSegment segment = chunks.get(i);
            // 强制写入 knowledgeBaseId 元数据，便于后续按知识库过滤检索
            segment.metadata().put(KB_ID_FIELD, knowledgeBaseId);
            embeddingStore.add(embeddings.get(i), segment);
        }
        log.info("向量存储完成: kbId={}, count={}", knowledgeBaseId, chunks.size());
    }



    /**
     * 在指定知识库中搜索与查询向量最相似的文档。
     *
     * <p>该方法使用向量相似度搜索算法，在给定的知识库中查找与查询向量最匹配的文档片段。
     * 搜索结果会按照相似度得分排序，并过滤掉相似度低于 0.6 的结果。</p>
     *
     * @param knowledgeBaseId 知识库ID，用于隔离不同知识库的数据，确保搜索范围限定在指定知识库内
     * @param queryEmbedding 查询文本的向量表示，用于与知识库中的文档向量进行相似度匹配
     * @param maxResults 最大返回结果数量，控制返回的匹配文档上限
     * @return 匹配的文档列表，每个元素包含文档内容和相似度得分；如果没有匹配结果则返回空列表
     */
    public List<DocumentMatch> search(String knowledgeBaseId, Embedding queryEmbedding, int maxResults) {
        // 构建知识库过滤条件，确保只搜索指定知识库的数据
        Filter kbFilter = new IsEqualTo(KB_ID_FIELD, knowledgeBaseId);

        return embeddingStore.search(
                        EmbeddingSearchRequest.builder()
                                .queryEmbedding(queryEmbedding)
                                .maxResults(maxResults)
                                .minScore(0.6)
                                .filter(kbFilter)
                                .build())
                .matches().stream()
                .map(m -> DocumentMatch.builder()
                        .content(m.embedded().text())
                        .score(m.score())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 仅删除指定知识库的向量，而非全量清空。
     * 注意：LangChain4j EmbeddingStore 的 removeAllByFilter 在部分实现中支持有限，
     * 如果 Milvus 版本不支持，建议改用 collection 级别隔离（每知识库一个 collection）。
     */
    public void deleteCollection(String knowledgeBaseId) {
        Filter kbFilter = new IsEqualTo(KB_ID_FIELD, knowledgeBaseId);
        embeddingStore.removeAll(kbFilter);
        log.info("向量集合已按知识库删除: kbId={}", knowledgeBaseId);
    }
}

package com.peanutai.llm.service.rag;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.aggregator.ContentAggregator;
import dev.langchain4j.rag.content.aggregator.DefaultContentAggregator;
import dev.langchain4j.rag.content.aggregator.ReRankingContentAggregator;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Metadata;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.rag.query.transformer.ExpandingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

/**
 * Advanced RAG Pipeline - 基于 LangChain4j 的模块化 RAG 框架
 * <p>
 * 实现以下 Advanced RAG 能力：
 * <ul>
 *   <li><b>查询转换 (Query Transformation)</b>：
 *     <ul>
 *       <li>查询压缩 (CompressingQueryTransformer) - 多轮对话上下文压缩</li>
 *       <li>查询扩展 (ExpandingQueryTransformer) - 生成多个查询变体提升召回率</li>
 *     </ul>
 *   </li>
 *   <li><b>内容检索 (Content Retrieval)</b>：
 *     <ul>
 *       <li>向量检索 (EmbeddingStoreContentRetriever) - 语义搜索</li>
 *       <li>动态元数据过滤 - 按 knowledgeBaseId 等条件过滤</li>
 *     </ul>
 *   </li>
 *   <li><b>内容聚合 (Content Aggregation)</b>：
 *     <ul>
 *       <li>RRF 融合排序 (DefaultContentAggregator) - 多查询结果融合</li>
 *       <li>重排序 (ReRankingContentAggregator) - 基于 ScoringModel 的精确重排</li>
 *     </ul>
 *   </li>
 *   <li><b>内容注入 (Content Injection)</b>：
 *     <ul>
 *       <li>元数据注入 (DefaultContentInjector) - 将文档来源等信息注入 Prompt</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * @see <a href="https://docs.langchain4j.dev/tutorials/rag/#advanced-rag">LangChain4j Advanced RAG</a>
 */
@Slf4j
@Component
public class AdvancedRagPipeline {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final ChatModel chatModel;
    private final ScoringModel scoringModel;

    @Value("${llm.rag.top-k:5}")
    private int defaultTopK;

    @Value("${llm.rag.min-score:0.6}")
    private double defaultMinScore;

    public AdvancedRagPipeline(EmbeddingStore<TextSegment> embeddingStore,
                               EmbeddingModel embeddingModel,
                               ChatModel chatModel,
                               @Autowired(required = false) ScoringModel scoringModel) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
        this.chatModel = chatModel;
        this.scoringModel = scoringModel;
    }

    /**
     * RAG 增强结果
     */
    public record AugmentationResult(
            /** 增强后的用户消息（已注入检索内容） */
            ChatMessage augmentedMessage,
            /** 检索到的内容源列表 */
            List<Content> sources,
            /** 原始查询 */
            Query originalQuery,
            /** 转换后的查询列表 */
            Collection<Query> transformedQueries
    ) {}

    /**
     * 执行 Advanced RAG 增强流程
     *
     * @param question         用户原始问题
     * @param knowledgeBaseId  知识库ID
     * @param topK             检索返回的最大结果数
     * @param enableQueryExpansion 是否启用查询扩展
     * @param enableRerank     是否启用重排序
     * @param historyMessages  历史对话消息（用于查询压缩）
     * @param metadataFilters  元数据过滤条件
     * @return 增强结果
     */
    public AugmentationResult augment(String question,
                                      String knowledgeBaseId,
                                      Integer topK,
                                      Boolean enableQueryExpansion,
                                      Boolean enableRerank,
                                      List<ChatMessage> historyMessages,
                                      Map<String, String> metadataFilters) {

        int maxResults = (topK != null && topK > 0) ? topK : defaultTopK;

        // [1] 构建 ContentRetriever（带动态元数据过滤）
        ContentRetriever contentRetriever = buildContentRetriever(maxResults, knowledgeBaseId, metadataFilters);

        // [2] 构建 QueryTransformer（查询压缩 + 查询扩展）
        QueryTransformer queryTransformer = buildQueryTransformer(
                enableQueryExpansion != null && enableQueryExpansion,
                historyMessages);

        // [3] 构建 ContentAggregator（RRF 或 Re-Ranking）
        ContentAggregator contentAggregator = buildContentAggregator(
                enableRerank != null && enableRerank);

        // [4] 构建 ContentInjector（注入检索内容到 Prompt）
        ContentInjector contentInjector = buildContentInjector();

        // [5] 创建 Query 对象（带聊天记忆用于查询压缩）
        Query query;
        if (historyMessages != null && !historyMessages.isEmpty()) {
            Metadata metadata = Metadata.builder()
                    .chatMessage(UserMessage.from(question))
                    .chatMemory(historyMessages)
                    .build();
            query = Query.from(question, metadata);
        } else {
            query = Query.from(question);
        }

        // [6] 查询转换（可能扩展为多个查询）
        Collection<Query> queries = queryTransformer.transform(query);
        log.debug("查询转换完成: 原始查询=[{}], 转换后查询数={}", question, queries.size());

        // [7] 对每个查询执行检索
        Map<Query, Collection<List<Content>>> queryToContents = new LinkedHashMap<>();
        List<Content> allContents = new ArrayList<>();

        for (Query q : queries) {
            List<Content> contents = contentRetriever.retrieve(q);
            queryToContents.put(q, Collections.singletonList(contents));
            allContents.addAll(contents);
        }
        log.debug("内容检索完成: 共检索到 {} 条内容", allContents.size());

        // [8] 内容聚合（排序/重排）
        List<Content> aggregatedContents = contentAggregator.aggregate(queryToContents);
        log.debug("内容聚合完成: 聚合后 {} 条内容", aggregatedContents.size());

        // [9] 内容注入（将检索内容注入到用户消息中）
        UserMessage userMessage = UserMessage.from(question);
        ChatMessage augmentedMessage = contentInjector.inject(aggregatedContents, userMessage);

        log.info("Advanced RAG Pipeline 完成: question=[{}], kbId=[{}], sources={}",
                question, knowledgeBaseId, aggregatedContents.size());

        return new AugmentationResult(augmentedMessage, aggregatedContents, query, queries);
    }

    /**
     * 构建 ContentRetriever，支持动态元数据过滤
     */
    private ContentRetriever buildContentRetriever(int maxResults,
                                                   String knowledgeBaseId,
                                                   Map<String, String> metadataFilters) {
        var builder = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(maxResults)
                .minScore(defaultMinScore);

        // 动态过滤：按 knowledgeBaseId + 自定义过滤条件
        final String kbId = knowledgeBaseId;
        builder.dynamicFilter(query -> {
            Filter filter = metadataKey("knowledgeBaseId").isEqualTo(kbId);

            // 叠加自定义元数据过滤条件
            if (metadataFilters != null && !metadataFilters.isEmpty()) {
                for (Map.Entry<String, String> entry : metadataFilters.entrySet()) {
                    filter = filter.and(metadataKey(entry.getKey()).isEqualTo(entry.getValue()));
                }
            }
            return filter;
        });

        return builder.build();
    }

    /**
     * 构建 QueryTransformer
     * <p>
     * 链式组合：
     * 1. CompressingQueryTransformer - 将多轮对话压缩为独立查询
     * 2. ExpandingQueryTransformer - 将查询扩展为多个变体
     */
    private QueryTransformer buildQueryTransformer(boolean enableQueryExpansion,
                                                   List<ChatMessage> historyMessages) {
        List<QueryTransformer> transformers = new ArrayList<>();

        // 查询压缩：当有历史对话时，将上下文压缩为独立查询
        if (historyMessages != null && !historyMessages.isEmpty()) {
            log.debug("启用查询压缩: 历史消息数={}", historyMessages.size());
            transformers.add(new CompressingQueryTransformer(chatModel));
        }

        // 查询扩展：生成多个查询变体提升召回率
        if (enableQueryExpansion) {
            log.debug("启用查询扩展");
            transformers.add(new ExpandingQueryTransformer(chatModel));
        }

        if (transformers.isEmpty()) {
            // 默认：不做任何转换，直接透传
            return query -> Collections.singletonList(query);
        }

        // 链式组合多个转换器
        return query -> {
            Collection<Query> result = Collections.singletonList(query);
            for (QueryTransformer transformer : transformers) {
                Collection<Query> nextResult = new ArrayList<>();
                for (Query q : result) {
                    nextResult.addAll(transformer.transform(q));
                }
                result = nextResult;
            }
            return result;
        };
    }

    /**
     * 构建 ContentAggregator
     * <p>
     * - 默认使用 DefaultContentAggregator（RRF 融合排序）
     * - 启用重排序时使用 ReRankingContentAggregator
     */
    private ContentAggregator buildContentAggregator(boolean enableRerank) {
        if (enableRerank && scoringModel != null) {
            log.debug("启用重排序 (Re-Ranking)");
            return new ReRankingContentAggregator(scoringModel);
        }

        log.debug("使用默认 RRF 融合排序");
        return new DefaultContentAggregator();
    }

    /**
     * 构建 ContentInjector，注入检索内容及元数据到 Prompt
     */
    private ContentInjector buildContentInjector() {
        return DefaultContentInjector.builder()
                // 注入文档来源、文件名等元数据
                .metadataKeysToInclude(List.of("file_name", "source", "absolute_directory_path"))
                .build();
    }

    /**
     * 获取检索到的内容文本（用于兼容旧接口）
     */
    public static String getContextText(List<Content> contents) {
        return contents.stream()
                .map(Content::textSegment)
                .filter(Objects::nonNull)
                .map(TextSegment::text)
                .collect(Collectors.joining("\n\n"));
    }
}
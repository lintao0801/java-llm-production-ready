package com.peanutai.llm.service.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.milvus.param.MetricType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LangChain4jConfig {

    @Value("${llm.openai.api-key}")
    private String openaiApiKey;

    @Value("${llm.openai.model:gpt-4o-mini}")
    private String modelName;

    @Value("${llm.openai.base-url:https://api.openai.com}")
    private String baseUrl;

    @Value("${llm.embedding.model:text-embedding-3-small}")
    private String embeddingModelName;

    @Value("${llm.embedding.dimensions:1536}")
    private int embeddingDimensions;

    @Value("${llm.milvus.host:localhost}")
    private String milvusHost;

    @Value("${llm.milvus.port:19530}")
    private int milvusPort;

    @Value("${llm.milvus.collection-name:llm_knowledge_base}")
    private String milvusCollectionName;

    /**
     * local profile 下为 true，使用 InMemoryEmbeddingStore 代替 Milvus，避免本地开发依赖 Docker。
     */
    @Value("${llm.use-in-memory-embedding:false}")
    private boolean useInMemoryEmbedding;

    @Bean
    public ChatModel chatModel() {
        return OpenAiChatModel.builder()
                .apiKey(openaiApiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .timeout(Duration.ofSeconds(30))
                .temperature(0.7)
                .maxTokens(2000)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    public StreamingChatModel streamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(openaiApiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .timeout(Duration.ofSeconds(60))
                .temperature(0.7)
                .maxTokens(2000)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(openaiApiKey)
                .modelName(embeddingModelName)
                .baseUrl(baseUrl)
                .timeout(Duration.ofSeconds(30))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    /**
     * 向量存储：生产环境用 Milvus，本地开发用 InMemoryEmbeddingStore。
     * 通过 llm.use-in-memory-embedding 开关切换。
     * 注意：InMemoryEmbeddingStore 不支持按 metadata 过滤删除，local profile 下 deleteCollection 会清空全部。
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        if (useInMemoryEmbedding) {
            return new InMemoryEmbeddingStore<>();
        }
        return MilvusEmbeddingStore.builder()
                .host(milvusHost)
                .port(milvusPort)
                .collectionName(milvusCollectionName)
                .dimension(embeddingDimensions)
                .metricType(MetricType.COSINE)
                .autoFlushOnInsert(true)
                .build();
    }

    /**
     * 重排序模型（可选，用于 Advanced RAG 的 Re-Ranking 能力）
     * <p>
     * 当配置了 llm.rerank.provider=cohere 且提供 COHERE_API_KEY 时启用。
     * 支持 Cohere 等主流重排序模型。
     * <p>
     * 若不配置，ReRankingContentAggregator 不会启用，将回退到 RRF 融合排序。
     *
     * @see <a href="https://docs.langchain4j.dev/category/scoring-reranking-models">LangChain4j Scoring Models</a>
     */
    @Bean
    @ConditionalOnProperty(name = "llm.rerank.provider", havingValue = "cohere")
    public ScoringModel scoringModel(
            @Value("${llm.rerank.cohere.api-key:}") String cohereApiKey,
            @Value("${llm.rerank.cohere.model:rerank-english-v3.0}") String cohereModel) {
        return dev.langchain4j.model.cohere.CohereScoringModel.builder()
                .apiKey(cohereApiKey)
                .modelName(cohereModel)
                .timeout(Duration.ofSeconds(30))
                .build();
    }
}
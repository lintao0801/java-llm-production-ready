package com.peanutai.llm.service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * RAG 查询请求 DTO
 * <p>
 * 支持 Advanced RAG 的扩展参数：
 * <ul>
 *   <li>topK：向量检索返回的最大结果数</li>
 *   <li>temperature：LLM 生成温度</li>
 *   <li>stream：是否流式返回</li>
 *   <li>conversationId：会话ID（多轮对话场景，用于查询压缩）</li>
 *   <li>historyMessages：历史消息列表（多轮对话上下文）</li>
 *   <li>enableQueryExpansion：是否启用查询扩展</li>
 *   <li>enableRerank：是否启用重排序</li>
 *   <li>metadataFilters：元数据过滤条件</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagRequest {

    /** 用户问题（必填） */
    private String question;

    /** 知识库ID（必填，用于数据隔离） */
    private String knowledgeBaseId;

    /** 向量检索返回的最大结果数（默认5） */
    private Integer topK;

    /** LLM 生成温度，控制随机性（默认0.7） */
    private Double temperature;

    /** 是否使用流式返回（默认false） */
    private Boolean stream;

    /** 会话ID，用于多轮对话上下文关联 */
    private String conversationId;

    /** 历史消息列表，用于多轮对话的查询压缩 */
    private List<ChatMessage> historyMessages;

    /** 是否启用查询扩展（默认true，会生成多个查询变体提升召回率） */
    private Boolean enableQueryExpansion;

    /** 是否启用重排序（默认false，需要配置 ScoringModel） */
    private Boolean enableRerank;

    /** 元数据过滤条件，如 {"userId": "12345", "category": "技术文档"} */
    private Map<String, String> metadataFilters;

    /**
     * 聊天消息（用于多轮对话历史）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        /** 角色：user / assistant */
        private String role;
        /** 消息内容 */
        private String content;
    }
}
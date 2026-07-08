package com.peanutai.llm.service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagResponse {
    private String answer;
    private List<DocumentSource> sources;
    private TokenUsage tokenUsage;
    private long latencyMs;
    private String model;
    private String conversationId;
}
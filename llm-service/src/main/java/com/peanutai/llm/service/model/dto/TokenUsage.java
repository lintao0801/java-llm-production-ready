package com.peanutai.llm.service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenUsage {
    private int inputTokens;
    private int outputTokens;
    private int totalTokens;
}
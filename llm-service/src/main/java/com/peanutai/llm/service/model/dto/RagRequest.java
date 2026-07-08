package com.peanutai.llm.service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagRequest {
    private String question;
    private String knowledgeBaseId;
    private Integer topK = 5;
    private Double temperature = 0.7;
    private Boolean stream = false;
}
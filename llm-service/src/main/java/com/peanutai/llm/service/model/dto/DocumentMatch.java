package com.peanutai.llm.service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMatch {
    private String documentId;
    private String fileName;
    private String content;
    private double score;
}
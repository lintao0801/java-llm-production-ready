package com.peanutai.llm.service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSource {
    private String documentId;
    private String fileName;
    private String content;
    private double similarityScore;
    private int pageNumber;
    private Map<String, Object> metadata;
}
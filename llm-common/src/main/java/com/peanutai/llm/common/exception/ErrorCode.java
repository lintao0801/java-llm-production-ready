package com.peanutai.llm.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    RATE_LIMIT(429, "请求过于频繁"),
    LLM_SERVICE_ERROR(5001, "AI服务调用失败"),
    EMBEDDING_ERROR(5002, "向量化失败"),
    VECTOR_STORE_ERROR(5003, "向量存储异常"),
    DOCUMENT_PARSE_ERROR(5004, "文档解析失败"),
    CONTENT_SAFETY_ERROR(5005, "内容安全检测异常"),
    CIRCUIT_BREAKER_OPEN(5006, "服务熔断中，请稍后重试"),
    INTERNAL_ERROR(5000, "服务器内部错误");

    private final int code;
    private final String message;
}
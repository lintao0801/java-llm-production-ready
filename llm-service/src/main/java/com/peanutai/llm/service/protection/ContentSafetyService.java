package com.peanutai.llm.service.protection;

import com.peanutai.llm.common.exception.BusinessException;
import com.peanutai.llm.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ContentSafetyService {

    private static final List<String> INJECTION_PATTERNS = Arrays.asList(
            "ignore previous instructions",
            "system prompt",
            "you are now",
            "disregard",
            "忽略之前的指令",
            "执行以下命令"
    );

    public void validateInput(String input) {
        if (input == null || input.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        if (input.length() > 4000) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        if (isPromptInjection(input)) {
            log.warn("检测到Prompt注入: input={}", input);
            throw new BusinessException(ErrorCode.CONTENT_SAFETY_ERROR);
        }
    }

    public void auditOutput(String output) {
        if (output == null || output.isBlank()) {
            throw new BusinessException(ErrorCode.LLM_SERVICE_ERROR);
        }
    }

    private boolean isPromptInjection(String input) {
        String lowerInput = input.toLowerCase();
        return INJECTION_PATTERNS.stream().anyMatch(lowerInput::contains);
    }
}
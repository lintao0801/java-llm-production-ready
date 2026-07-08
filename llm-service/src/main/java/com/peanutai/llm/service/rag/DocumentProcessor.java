package com.peanutai.llm.service.rag;

import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class DocumentProcessor {

    private static final int CHUNK_SIZE = 1000;// 每块1000字符
    private static final int CHUNK_OVERLAP = 200;// 块间重叠200字符
    private final Tika tika = new Tika();

    public String parse(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            return tika.parseToString(is);
        } catch (TikaException e) {
            throw new IOException("文档解析失败: " + file.getOriginalFilename(), e);
        }
    }

    public List<TextSegment> chunk(String content) {
        List<TextSegment> chunks = new ArrayList<>();
        int length = content.length();
        // 滑动窗口分块，重叠避免语义被切断
        for (int start = 0; start < length; start += (CHUNK_SIZE - CHUNK_OVERLAP)) {
            int end = Math.min(start + CHUNK_SIZE, length);
            String chunk = content.substring(start, end);
            chunks.add(TextSegment.from(chunk));
        }

        return chunks;
    }
}
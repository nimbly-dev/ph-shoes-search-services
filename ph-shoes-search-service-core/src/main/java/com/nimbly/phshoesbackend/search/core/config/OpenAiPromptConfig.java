package com.nimbly.phshoesbackend.search.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Component
public class OpenAiPromptConfig {

    private final String promptTemplate;

    public OpenAiPromptConfig(
            @Value("classpath:ai/prompt-intent-parser.txt")
            Resource resource
    ) throws IOException {
        try (var in = resource.getInputStream()) {
            this.promptTemplate = StreamUtils.copyToString(in, StandardCharsets.UTF_8);
        }
    }

    public String getPromptFor(String userQuery) {
        return promptTemplate.replace("{QUERY}", userQuery.trim());
    }
}

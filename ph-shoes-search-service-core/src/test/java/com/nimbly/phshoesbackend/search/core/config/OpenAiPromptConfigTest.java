package com.nimbly.phshoesbackend.search.core.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiPromptConfigTest {

    @Test
    void getPromptFor_replacesQueryToken() throws Exception {
        // Arrange
        OpenAiPromptConfig config = new OpenAiPromptConfig(new ClassPathResource("ai/prompt-intent-parser.txt"));

        // Act
        String prompt = config.getPromptFor("nike shoes");

        // Assert
        assertThat(prompt).contains("nike shoes");
        assertThat(prompt).doesNotContain("{QUERY}");
    }
}

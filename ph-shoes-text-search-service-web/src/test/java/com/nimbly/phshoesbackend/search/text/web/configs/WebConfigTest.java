package com.nimbly.phshoesbackend.search.text.web.configs;

import com.nimbly.phshoesbackend.search.text.web.configs.props.CorsProperties;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WebConfigTest {

    @Test
    void corsConfigurer_registersMappings() throws Exception {
        // Arrange
        CorsProperties corsProperties = new CorsProperties();
        corsProperties.setAllowedOrigins(List.of("https://example.com"));
        corsProperties.setAllowedMethods(List.of("GET"));
        corsProperties.setAllowedHeaders(List.of("X-Test"));
        corsProperties.setAllowCredentials(false);
        corsProperties.setMaxAge(1800);

        WebConfig config = new WebConfig(corsProperties);
        TestCorsRegistry registry = new TestCorsRegistry();

        // Act
        WebMvcConfigurer configurer = config.corsConfigurer();
        configurer.addCorsMappings(registry);

        // Assert
        Map<String, CorsConfiguration> configurations = registry.getConfigurations();
        CorsConfiguration configuration = configurations.get("/**");
        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins()).containsExactly("https://example.com");
        assertThat(configuration.getAllowedMethods()).containsExactly("GET");
        assertThat(configuration.getAllowedHeaders()).containsExactly("X-Test");
        assertThat(configuration.getAllowCredentials()).isFalse();
        assertThat(configuration.getMaxAge()).isEqualTo(1800);
    }

    private static final class TestCorsRegistry extends CorsRegistry {
        Map<String, CorsConfiguration> getConfigurations() {
            return super.getCorsConfigurations();
        }
    }
}

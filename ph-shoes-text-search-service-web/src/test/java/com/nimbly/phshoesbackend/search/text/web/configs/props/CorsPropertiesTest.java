package com.nimbly.phshoesbackend.search.text.web.configs.props;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CorsPropertiesTest {

    @Test
    void defaults_matchExpectedValues() {
        // Arrange
        CorsProperties properties = new CorsProperties();

        // Act + Assert
        assertThat(properties.getAllowedOrigins()).containsExactly("http://localhost:5173");
        assertThat(properties.getAllowedMethods()).containsExactly("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(properties.getAllowedHeaders()).containsExactly("*");
        assertThat(properties.getExposedHeaders()).isEmpty();
        assertThat(properties.isAllowCredentials()).isTrue();
        assertThat(properties.getMaxAge()).isEqualTo(3600);
    }
}

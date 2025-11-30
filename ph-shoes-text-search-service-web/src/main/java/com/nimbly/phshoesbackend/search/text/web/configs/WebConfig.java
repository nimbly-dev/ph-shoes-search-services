package com.nimbly.phshoesbackend.search.text.web.configs;

import com.nimbly.phshoesbackend.search.text.web.configs.props.CorsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    private final CorsProperties corsProperties;

    public WebConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                CorsRegistration registration = registry.addMapping("/**")
                        .allowedOrigins(corsProperties.getAllowedOrigins().toArray(new String[0]))
                        .allowedMethods(corsProperties.getAllowedMethods().toArray(new String[0]))
                        .allowedHeaders(corsProperties.getAllowedHeaders().toArray(new String[0]))
                        .allowCredentials(corsProperties.isAllowCredentials())
                        .maxAge(corsProperties.getMaxAge());

                if (!corsProperties.getExposedHeaders().isEmpty()) {
                    registration.exposedHeaders(corsProperties.getExposedHeaders().toArray(new String[0]));
                }
            }
        };
    }
}


package com.nimbly.phshoesbackend.search.text.web.configs.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    private List<String> allowedOrigins = new ArrayList<>(Collections.singletonList("http://localhost:5173"));
    private List<String> allowedMethods = new ArrayList<>(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    private List<String> allowedHeaders = new ArrayList<>(Collections.singletonList("*"));
    private List<String> exposedHeaders = new ArrayList<>();
    private boolean allowCredentials = true;
    private long maxAge = 3600;
}


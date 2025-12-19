package com.sketchnotes.identityservice.config;


import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.PredictionServiceSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class VertexAIConfig {

    private final GeminiProperties   geminiProperties;

    public VertexAIConfig(GeminiProperties geminiProperties) {
        this.geminiProperties = geminiProperties;
    }

    @Bean
    public PredictionServiceClient predictionServiceClient() throws IOException {
        String endpoint = String.format("%s-aiplatform.googleapis.com:443", geminiProperties.getLocation());

        PredictionServiceSettings settings = PredictionServiceSettings.newBuilder()
                .setEndpoint(endpoint)
                .build();

        return PredictionServiceClient.create(settings);
    }
}
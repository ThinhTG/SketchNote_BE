package com.sketchnotes.identityservice.config;


import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.PredictionServiceSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@Slf4j
public class VertexAIConfig {

    private final GeminiProperties geminiProperties;

    public VertexAIConfig(GeminiProperties geminiProperties) {
        this.geminiProperties = geminiProperties;
    }

    /**
     * Creates PredictionServiceClient bean only if Google Cloud credentials are properly configured.
     * This bean is optional - ContentModerationService uses VertexAI SDK directly which handles
     * credentials differently.
     */
    @Bean
    @ConditionalOnProperty(name = "spring.cloud.gcp.credentials.location")
    public PredictionServiceClient predictionServiceClient() throws IOException {
        try {
            String endpoint = String.format("%s-aiplatform.googleapis.com:443", geminiProperties.getLocation());

            PredictionServiceSettings settings = PredictionServiceSettings.newBuilder()
                    .setEndpoint(endpoint)
                    .build();

            log.info("Creating PredictionServiceClient with endpoint: {}", endpoint);
            return PredictionServiceClient.create(settings);
        } catch (Exception e) {
            log.warn("Failed to create PredictionServiceClient. This is optional if you're only using Vertex AI Generative AI: {}", e.getMessage());
            throw e;
        }
    }
}
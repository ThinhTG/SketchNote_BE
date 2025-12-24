package com.sketchnotes.identityservice.config;


import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.PredictionServiceSettings;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
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
    @Bean
    @ConditionalOnProperty(name = "spring.cloud.gcp.credentials.location")
    public PredictionServiceClient predictionServiceClient() throws IOException {
        try {
            String endpoint = String.format("%s-aiplatform.googleapis.com:443", geminiProperties.getLocation());

            PredictionServiceSettings settings = PredictionServiceSettings.newBuilder()
                    .setEndpoint(endpoint)
                    .build();
            return PredictionServiceClient.create(settings);
        } catch (Exception e) {
            throw e;
        }
    }
    @Bean(destroyMethod = "close")
    public ImageAnnotatorClient imageAnnotatorClient() throws IOException {
        try {
            return ImageAnnotatorClient.create();
        } catch (IOException e) {
            throw e;
        }
    }
}
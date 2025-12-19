package com.sketchnotes.identityservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.gemini")
public class GeminiProperties {
    private String projectId;
    private String location;
    private String model;
}
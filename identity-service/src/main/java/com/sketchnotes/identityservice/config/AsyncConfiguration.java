package com.sketchnotes.identityservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration để enable async processing cho event listeners
 * Cho phép UserCreatedEventListener xử lý event không đồng bộ
 */
@Configuration
@EnableAsync
public class AsyncConfiguration {
    // Async processing enabled for event listeners
}

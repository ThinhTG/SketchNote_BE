package com.sketchnotes.identityservice.config;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign client configuration for service-to-service communication
 * Registers the FeignClientInterceptor to propagate JWT tokens
 */
@Configuration
@RequiredArgsConstructor
public class FeignConfig {
    
    private final FeignClientInterceptor feignClientInterceptor;
    
    @Bean
    public RequestInterceptor requestInterceptor() {
        return feignClientInterceptor;
    }
}

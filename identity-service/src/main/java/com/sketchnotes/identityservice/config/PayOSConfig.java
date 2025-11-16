package com.sketchnotes.identityservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.ưpayos.PayOS;
import org.springframework.beans.factory.annotation.Value;


@Configuration
public class PayOSConfig {

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    @Value("${payos.api-url:}")
    private String apiUrl;

    @Bean
    public PayOS payOS() {
        // If an apiUrl is provided in configuration, use the constructor that accepts it.
        // This ensures the SDK talks to the correct PayOS endpoint (sandbox/production) and
        // prevents signature mismatches caused by environment differences.
        if (apiUrl != null && !apiUrl.isBlank()) {
            try {
                return new PayOS(clientId, apiKey, checksumKey, apiUrl);
            } catch (NoSuchMethodError | IllegalArgumentException ex) {
                // Fallback to older constructor if SDK version doesn't support apiUrl param
                // log a warning and continue with the 3-arg constructor
                System.err.println("PayOS SDK does not support apiUrl constructor or invalid apiUrl provided. Falling back to default constructor. " + ex.getMessage());
                return new PayOS(clientId, apiKey, checksumKey);
            }
        }

        return new PayOS(clientId, apiKey, checksumKey);
    }
}
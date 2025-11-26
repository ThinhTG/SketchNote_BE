package com.sketchnotes.identityservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    
    @Value("${gateway.url:http://localhost:8888}")
    private String gatewayUrl;
    
    @Value("${gateway.account-path:}")
    private String accountPath;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Account Service API")
                        .version("v1")
                        .description("Account Service API - All requests go through API Gateway"))
                .servers(List.of(
                        // Local Gateway (default cho development)
                        new Server()
                                .url(gatewayUrl + accountPath)
                                .description("API Gateway (Local/Production)"),
                        // Direct to service (for debugging)
                        new Server()
                                .url("http://localhost:8089")
                                .description("Direct to Account Service (Debug only)")
                ))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}

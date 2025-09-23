package com.sketchnotes.learning.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Learning Service API")
                        .version("3.0.0")   // ⚡ cần có version hợp lệ ở đây
                        .description("API documentation for Learning Service"));
    }
}

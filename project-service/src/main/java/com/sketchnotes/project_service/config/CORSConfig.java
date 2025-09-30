package com.sketchnotes.project_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CORSConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")   // Cho phép tất cả domain (nên đổi thành FE domain thực tế khi deploy)
                .allowedHeaders("*")
                .exposedHeaders("Access-Control-Allow-Origin",
                        "Access-Control-Allow-Methods",
                        "Access-Control-Allow-Headers")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .maxAge(3600); // Cache preflight trong 1h
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cho Swagger UI và static files
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
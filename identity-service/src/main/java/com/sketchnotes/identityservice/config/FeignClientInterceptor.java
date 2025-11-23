package com.sketchnotes.identityservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Feign client interceptor that propagates JWT token from incoming requests
 * to outgoing Feign client requests for service-to-service authentication
 */
@Component
@Slf4j
public class FeignClientInterceptor implements RequestInterceptor {
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
            
            if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
                log.debug("Propagating Authorization header to Feign client request: {}", template.url());
                template.header(AUTHORIZATION_HEADER, authorizationHeader);
            } else {
                log.debug("No valid Authorization header found in request context for: {}", template.url());
            }
        } else {
            log.debug("No request context available for Feign client request: {}", template.url());
        }
    }
}

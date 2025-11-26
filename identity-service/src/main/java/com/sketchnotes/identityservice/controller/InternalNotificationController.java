package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dtos.request.CreateNotificationRequest;
import com.sketchnotes.identityservice.dtos.response.NotificationDto;
import com.sketchnotes.identityservice.service.interfaces.INotificationService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal controller for notification operations.
 * Used by other microservices (order-service, learning-service) to create notifications.
 * These endpoints are not secured to allow internal service-to-service communication.
 */
@RestController
@RequestMapping("/internal/notifications")
@RequiredArgsConstructor
@Slf4j
@Hidden // Hide from Swagger UI as these are internal endpoints
public class InternalNotificationController {
    
    private final INotificationService notificationService;
    
    /**
     * Create a notification (internal use only).
     * Called by other microservices via Feign clients.
     */
    @PostMapping
    public ResponseEntity<NotificationDto> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        
        log.info("Internal request to create notification for user {}: {}", 
                request.getUserId(), request.getTitle());
        
        NotificationDto created = notificationService.create(request);
        
        return ResponseEntity.ok(created);
    }
}

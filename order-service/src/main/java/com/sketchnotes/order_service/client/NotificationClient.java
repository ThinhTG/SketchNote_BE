package com.sketchnotes.order_service.client;

import com.sketchnotes.order_service.dtos.CreateNotificationRequest;
import com.sketchnotes.order_service.dtos.NotificationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for communicating with the identity-service notification API.
 * Used to create notifications for users when events occur (purchases, enrollments, etc.).
 */
@FeignClient(name = "account-service")
public interface NotificationClient {
    
    /**
     * Create a notification via the identity-service internal API.
     *
     * @param request the notification creation request
     * @return the created notification DTO
     */
    @PostMapping("/internal/notifications")
    NotificationDto createNotification(@RequestBody CreateNotificationRequest request);
}

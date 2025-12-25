package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dtos.request.CreateNotificationRequest;
import com.sketchnotes.identityservice.dtos.response.NotificationDto;
import com.sketchnotes.identityservice.dtos.response.UserResponse;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.service.interfaces.INotificationService;
import com.sketchnotes.identityservice.service.interfaces.IUserService;
import com.sketchnotes.identityservice.ultils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for notification management.
 * Provides endpoints for users to view and manage their notifications.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {
    
    private final INotificationService notificationService;
    private final IUserService userService;
    
    /**
     * Get paginated notifications for the current authenticated user.
     */
    @GetMapping
    @Operation(summary = "Get user notifications", 
               description = "Retrieve paginated list of notifications for the authenticated user")
    public ResponseEntity<Page<NotificationDto>> getMyNotifications(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        
        Long userId = getCurrentUserId();
        log.debug("Fetching notifications for user ID: {}", userId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<NotificationDto> notifications = notificationService.getNotifications(userId, pageable);
        
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Mark a specific notification as read.
     */
    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", 
               description = "Mark a specific notification as read for the authenticated user")
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "Notification ID")
            @PathVariable Long id) {
        
        Long userId = getCurrentUserId();
        log.info("User {} marking notification {} as read", userId, id);
        
        notificationService.markAsRead(userId, id);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Mark all notifications as read for the current user.
     */
    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", 
               description = "Mark all notifications as read for the authenticated user")
    public ResponseEntity<Void> markAllAsRead() {
        Long userId = getCurrentUserId();
        log.info("User {} marking all notifications as read", userId);
        
        notificationService.markAllAsRead(userId);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get count of unread notifications for the current user.
     */
    @GetMapping("/count-unread")
    @Operation(summary = "Count unread notifications", 
               description = "Get the count of unread notifications for the authenticated user")
    public ResponseEntity<Map<String, Long>> countUnread() {
        Long userId = getCurrentUserId();
        
        long count = notificationService.countUnread(userId);
        
        return ResponseEntity.ok(Map.of("unread", count));
    }
    
    /**
     * Helper method to get current user's database ID from keycloak ID.
     * Uses IUserService instead of direct repository access.
     */
    private Long getCurrentUserId() {
        String keycloakId = SecurityUtils.getCurrentUserId();
        UserResponse user = userService.getUserByKeycloakId(keycloakId);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        return user.getId();
    }
}


package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.dtos.request.CreateNotificationRequest;
import com.sketchnotes.identityservice.dtos.response.NotificationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for notification management.
 * Handles notification creation, retrieval, and status updates.
 */
public interface INotificationService {
    
    /**
     * Create a new notification and push it to the user via WebSocket.
     *
     * @param request the notification creation request
     * @return the created notification DTO
     */
    NotificationDto create(CreateNotificationRequest request);
    
    /**
     * Get paginated notifications for a specific user.
     *
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of notification DTOs
     */
    Page<NotificationDto> getNotifications(Long userId, Pageable pageable);
    
    /**
     * Mark a specific notification as read.
     *
     * @param userId the user ID (for authorization)
     * @param notificationId the notification ID
     */
    void markAsRead(Long userId, Long notificationId);
    
    /**
     * Mark all notifications for a user as read.
     *
     * @param userId the user ID
     */
    void markAllAsRead(Long userId);
    
    /**
     * Count unread notifications for a user.
     *
     * @param userId the user ID
     * @return count of unread notifications
     */
    long countUnread(Long userId);
}

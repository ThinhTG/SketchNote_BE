package com.sketchnotes.identityservice.mapper;

import com.sketchnotes.identityservice.dtos.request.CreateNotificationRequest;
import com.sketchnotes.identityservice.dtos.response.NotificationDto;
import com.sketchnotes.identityservice.model.Notification;

/**
 * Mapper utility class for converting between Notification entities and DTOs.
 * Uses static methods for simple, clean conversions.
 */
public class NotificationMapper {
    
    private NotificationMapper() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Converts a Notification entity to a NotificationDto.
     *
     * @param notification the entity to convert
     * @return the corresponding DTO
     */
    public static NotificationDto toDto(Notification notification) {
        if (notification == null) {
            return null;
        }
        
        return NotificationDto.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .resourceItemId(notification.getResourceItemId())
                .orderId(notification.getOrderId())
                .projectId(notification.getProjectId())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
    
    /**
     * Converts a CreateNotificationRequest to a Notification entity.
     * Note: ID and createdAt will be set by JPA.
     *
     * @param request the request DTO
     * @return the corresponding entity
     */
    public static Notification toEntity(CreateNotificationRequest request) {
        if (request == null) {
            return null;
        }
        
        return Notification.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .resourceItemId(request.getResourceItemId())
                .orderId(request.getOrderId())
                .projectId(request.getProjectId())
                .isRead(false)
                .build();
    }
}

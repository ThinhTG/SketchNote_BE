package com.sketchnotes.identityservice.service;

import com.sketchnotes.identityservice.dtos.request.CreateNotificationRequest;
import com.sketchnotes.identityservice.dtos.response.NotificationDto;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.mapper.NotificationMapper;
import com.sketchnotes.identityservice.model.Notification;
import com.sketchnotes.identityservice.repository.NotificationRepository;
import com.sketchnotes.identityservice.service.interfaces.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for notification management.
 * Handles notification CRUD operations and real-time WebSocket push.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService implements INotificationService {
    
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Override
    @Transactional
    public NotificationDto create(CreateNotificationRequest request) {
        log.info("Creating notification for user {}: {}", request.getUserId(), request.getTitle());
        
        // Convert request to entity
        Notification notification = NotificationMapper.toEntity(request);
        
        // Save to database
        Notification saved = notificationRepository.save(notification);
        log.debug("Notification saved with ID: {}", saved.getId());
        
        // Convert to DTO
        NotificationDto dto = NotificationMapper.toDto(saved);
        
        // Push real-time notification via WebSocket
        try {
            String destination = "/topic/notifications." + request.getUserId();
            messagingTemplate.convertAndSend(destination, dto);
            log.debug("Notification pushed to WebSocket destination: {}", destination);
        } catch (Exception e) {
            log.error("Failed to push notification via WebSocket for user {}: {}", 
                    request.getUserId(), e.getMessage(), e);
            // Don't fail the request if WebSocket push fails
        }
        
        return dto;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotifications(Long userId, Pageable pageable) {
        log.debug("Fetching notifications for user {} with pagination: {}", userId, pageable);
        
        Page<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        return notifications.map(NotificationMapper::toDto);
    }
    
    @Override
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        log.debug("Marking notification {} as read for user {}", notificationId, userId);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    log.warn("Notification not found: {}", notificationId);
                    return new AppException(ErrorCode.NOTIFICATION_NOT_FOUND);
                });
        
        // Verify the notification belongs to the user
        if (!notification.getUserId().equals(userId)) {
            log.warn("User {} attempted to mark notification {} belonging to user {}", 
                    userId, notificationId, notification.getUserId());
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
            log.info("Notification {} marked as read for user {}", notificationId, userId);
        } else {
            log.debug("Notification {} was already read", notificationId);
        }
    }
    
    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user {}", userId);
        
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndIsRead(userId, false);
        
        if (unreadNotifications.isEmpty()) {
            log.debug("No unread notifications found for user {}", userId);
            return;
        }
        
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
        
        log.info("Marked {} notifications as read for user {}", 
                unreadNotifications.size(), userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        long count = notificationRepository.countByUserIdAndIsRead(userId, false);
        log.debug("User {} has {} unread notifications", userId, count);
        return count;
    }
}

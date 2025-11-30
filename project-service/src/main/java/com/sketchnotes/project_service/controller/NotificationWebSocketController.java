package com.sketchnotes.project_service.controller;

import com.sketchnotes.project_service.dtos.socket.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * WebSocket Controller for real-time notifications
 * Handles notification delivery to specific users or groups
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class NotificationWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send notification to a specific user
     * 
     * Client sends to: /app/notify/{userId}
     * Server sends to: /queue/notify/{userId} (private queue)
     */
    @MessageMapping("/notify/{userId}")
    public void sendNotificationToUser(
            @DestinationVariable Long userId,
            @Payload NotificationMessage notification) {
        
        notification.setTimestamp(LocalDateTime.now());
        notification.setRecipientId(userId);
        
        log.info("📢 [Notification] Sending to user {}: type={}, title={}", 
                userId, notification.getType(), notification.getTitle());
        
        // Send to specific user's private queue
        messagingTemplate.convertAndSend(
                "/queue/notify/" + userId,
                notification
        );
    }

    /**
     * Broadcast notification to all users in a project
     * 
     * Client sends to: /app/notify-project/{projectId}
     * Server broadcasts to: /topic/notify/{projectId}
     */
    @MessageMapping("/notify-project/{projectId}")
    public void broadcastNotificationToProject(
            @DestinationVariable Long projectId,
            @Payload NotificationMessage notification) {
        
        notification.setTimestamp(LocalDateTime.now());
        notification.setProjectId(projectId);
        
        log.info("📣 [Notification] Broadcasting to project {}: type={}, content={}", 
                projectId, notification.getType(), notification.getContent());
        
        // Broadcast to all users in the project
        messagingTemplate.convertAndSend(
                "/topic/notify/" + projectId,
                notification
        );
    }

    /**
     * Send collaboration invitation via WebSocket
     * User B receives realtime notification when User A invites them
     * 
     * Client sends to: /app/notify-collab-invite/{recipientUserId}
     */
    @MessageMapping("/notify-collab-invite/{recipientUserId}")
    public void sendCollaborationInvite(
            @DestinationVariable Long recipientUserId,
            @Payload NotificationMessage invitation) {
        
        invitation.setType("PROJECT_INVITE");
        invitation.setTimestamp(LocalDateTime.now());
        invitation.setRecipientId(recipientUserId);
        invitation.setPriority("HIGH");
        
        log.info("👥 [Notification] Collaboration invite from user {} to user {}: project={}", 
                invitation.getSenderId(), recipientUserId, invitation.getProjectId());
        
        // Send private invitation to the recipient
        messagingTemplate.convertAndSend(
                "/queue/notify/" + recipientUserId,
                invitation
        );
    }

    /**
     * Notify users when someone joins a collaborative session
     * 
     * Client sends to: /app/notify-collab-join/{projectId}
     */
    @MessageMapping("/notify-collab-join/{projectId}")
    public void notifyCollaborationJoin(
            @DestinationVariable Long projectId,
            @Payload NotificationMessage joinNotification) {
        
        joinNotification.setType("COLLAB_USER_JOINED");
        joinNotification.setTimestamp(LocalDateTime.now());
        joinNotification.setProjectId(projectId);
        
        log.info("🟢 [Notification] User {} joined project {}", 
                joinNotification.getSenderId(), projectId);
        
        // Notify all users in the project
        messagingTemplate.convertAndSend(
                "/topic/notify/" + projectId,
                joinNotification
        );
    }

    /**
     * Notify users when someone leaves a collaborative session
     * 
     * Client sends to: /app/notify-collab-leave/{projectId}
     */
    @MessageMapping("/notify-collab-leave/{projectId}")
    public void notifyCollaborationLeave(
            @DestinationVariable Long projectId,
            @Payload NotificationMessage leaveNotification) {
        
        leaveNotification.setType("COLLAB_USER_LEFT");
        leaveNotification.setTimestamp(LocalDateTime.now());
        leaveNotification.setProjectId(projectId);
        
        log.info("🔴 [Notification] User {} left project {}", 
                leaveNotification.getSenderId(), projectId);
        
        // Notify all users in the project
        messagingTemplate.convertAndSend(
                "/topic/notify/" + projectId,
                leaveNotification
        );
    }

    /**
     * Acknowledge that a notification was read by user
     * 
     * Client sends to: /app/notify-read/{userId}
     */
    @MessageMapping("/notify-read/{userId}")
    public void markNotificationAsRead(
            @DestinationVariable Long userId,
            @Payload NotificationMessage readNotification) {
        
        readNotification.setRead(true);
        readNotification.setReadAt(LocalDateTime.now());
        
        log.debug("✅ [Notification] User {} marked notification {} as read", 
                userId, readNotification.getNotificationId());
        
        // Could trigger event to update database
        // For now, just log the action
    }
}

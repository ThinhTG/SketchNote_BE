package com.sketchnotes.interaction_service.service;

import com.sketchnotes.interaction_service.entity.Notification;

import java.util.List;

public interface NotificationService {
    List<Notification> getUserNotifications(Long userId);
    Notification addNotification(Notification notification);
    Notification markAsRead(Long id);
}
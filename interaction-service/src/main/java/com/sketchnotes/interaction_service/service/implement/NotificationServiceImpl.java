package com.sketchnotes.interaction_service.service.implement;

import com.sketchnotes.interaction_service.entity.Notification;
import com.sketchnotes.interaction_service.repository.NotificationRepository;
import com.sketchnotes.interaction_service.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;

    public NotificationServiceImpl(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Notification> getUserNotifications(Long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public Notification addNotification(Notification notification) {
        return repository.save(notification);
    }

    @Override
    public Notification markAsRead(Long id) {
        Notification noti = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        noti.setIsRead(true);
        return repository.save(noti);
    }
}
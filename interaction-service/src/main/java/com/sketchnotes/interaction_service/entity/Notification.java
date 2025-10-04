package com.sketchnotes.interaction_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    private Long userId;
    private String message;
    private Boolean isRead = false;

    private LocalDateTime createdAt = LocalDateTime.now();


}
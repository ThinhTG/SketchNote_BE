package com.sketchnotes.project_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "image_prompt")
@Getter
@Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ImagePrompt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imagePromptId;


    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Long ownerId;
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

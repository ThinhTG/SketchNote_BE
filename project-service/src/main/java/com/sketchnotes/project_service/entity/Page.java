package com.sketchnotes.project_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "page")
@Getter
@Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false)
    private Integer pageNumber;

    // URL tới file strokes đã upload cloud
    @Column(nullable = false)
    private String strokeUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

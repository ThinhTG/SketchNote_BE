package com.sketchnotes.project_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "page_version")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pageVersionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_version_id")
    private ProjectVersion projectVersion;
    @Column(nullable = false)
    private Integer pageNumber;

    @Column(nullable = false)
    private String strokeUrl;


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
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

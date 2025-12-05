package com.sketchnotes.project_service.entity;

import com.sketchnotes.project_service.enums.PaperType;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project")
@Getter
@Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 1000)
    private String description;
    private String imageUrl;
    private Long ownerId;
    @Enumerated(EnumType.STRING)
    private PaperType paperType;
    @OneToMany(mappedBy = "project")
    private List<Page> pages = new ArrayList<>();
    @OneToMany(mappedBy = "project")
    private List<ProjectCollaboration> projectCollaborations = new ArrayList<>();
    @OneToMany(mappedBy = "project")
    private List<ProjectVersion> projectVersions = new ArrayList<>();

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


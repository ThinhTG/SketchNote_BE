package com.sketchnotes.order_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "resource_template")
public class ResourceTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long templateId;

    @Column(name = "designer_id", nullable = false)
    private Long designerId;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TemplateType type;

    @Column(name = "price", precision = 15, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "expired_time")
    private LocalDate expiredTime;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "resourceTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourcesTemplateImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "resourceTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceTemplateItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (releaseDate == null) {
            releaseDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum TemplateType {
        PRESENTATION,
        DOCUMENT,
        INFOGRAPHIC,
        POSTER,
        BROCHURE,
        CERTIFICATE,
        OTHER
    }

    public enum TemplateStatus {
        PENDING_REVIEW,
        PUBLISHED,
        REJECTED
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TemplateStatus status = TemplateStatus.PENDING_REVIEW;
}

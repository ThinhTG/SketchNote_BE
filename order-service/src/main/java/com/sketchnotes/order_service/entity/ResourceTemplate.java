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

    @Column(name = "current_published_version_id")
    private Long currentPublishedVersionId; // ID of the currently published version

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
       ICONS, TEMPLATES, FONT, ILLUSTRATIONS, MOCKUPS, PHOTOS, TITLES, OTHER
    }

    public enum TemplateStatus {
        PENDING_REVIEW,  // Initial state - waiting for Staff approval
        PUBLISHED,       // Staff approved - visible on marketplace
        REJECTED,        // Staff rejected - end state
        ARCHIVED,        // Designer archived - hidden from marketplace
        DELETED          // Soft deleted - can be restored
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TemplateStatus status = TemplateStatus.PENDING_REVIEW;

    // ============ HELPER METHODS for orphanRemoval ============
    // Use these methods to properly manage bidirectional relationships
    // and avoid "orphan deletion was no longer referenced" errors

    public void addImage(ResourcesTemplateImage image) {
        images.add(image);
        image.setResourceTemplate(this);
    }

    public void removeImage(ResourcesTemplateImage image) {
        images.remove(image);
        image.setResourceTemplate(null);
    }

    public void clearAndSetImages(List<ResourcesTemplateImage> newImages) {
        this.images.clear();
        if (newImages != null) {
            newImages.forEach(img -> {
                img.setResourceTemplate(this);
                this.images.add(img);
            });
        }
    }

    public void addItem(ResourceTemplateItem item) {
        items.add(item);
        item.setResourceTemplate(this);
    }

    public void removeItem(ResourceTemplateItem item) {
        items.remove(item);
        item.setResourceTemplate(null);
    }

    public void clearAndSetItems(List<ResourceTemplateItem> newItems) {
        this.items.clear();
        if (newItems != null) {
            newItems.forEach(item -> {
                item.setResourceTemplate(this);
                this.items.add(item);
            });
        }
    }
}

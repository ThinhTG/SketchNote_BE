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
@Table(name = "resource_template_version")
public class ResourceTemplateVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long versionId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "version_number", nullable = false)
    private String versionNumber; // e.g., "1.0", "2.0", "3.0"

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ResourceTemplate.TemplateType type;

    @Column(name = "price", precision = 15, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ResourceTemplate.TemplateStatus status = ResourceTemplate.TemplateStatus.PENDING_REVIEW;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false)
    private Long createdBy; // designer ID

    @Column(name = "reviewed_by")
    private Long reviewedBy; // staff ID who reviewed

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_comment")
    private String reviewComment; // Lý do từ chối hoặc comment khi approve

    @OneToMany(mappedBy = "version", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceTemplateVersionImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "version", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceTemplateVersionItem> items = new ArrayList<>();

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

    // ============ HELPER METHODS for orphanRemoval ============
    // Use these methods to properly manage bidirectional relationships
    // and avoid "orphan deletion was no longer referenced" errors

    public void addImage(ResourceTemplateVersionImage image) {
        images.add(image);
        image.setVersion(this);
    }

    public void removeImage(ResourceTemplateVersionImage image) {
        images.remove(image);
        image.setVersion(null);
    }

    public void clearAndSetImages(List<ResourceTemplateVersionImage> newImages) {
        this.images.clear();
        if (newImages != null) {
            newImages.forEach(img -> {
                img.setVersion(this);
                this.images.add(img);
            });
        }
    }

    public void addItem(ResourceTemplateVersionItem item) {
        items.add(item);
        item.setVersion(this);
    }

    public void removeItem(ResourceTemplateVersionItem item) {
        items.remove(item);
        item.setVersion(null);
    }

    public void clearAndSetItems(List<ResourceTemplateVersionItem> newItems) {
        this.items.clear();
        if (newItems != null) {
            newItems.forEach(item -> {
                item.setVersion(this);
                this.items.add(item);
            });
        }
    }
}

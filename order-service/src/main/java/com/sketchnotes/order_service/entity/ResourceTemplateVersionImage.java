package com.sketchnotes.order_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "resource_template_version_image")
public class ResourceTemplateVersionImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @ManyToOne
    @JoinColumn(name = "version_id", nullable = false)
    private ResourceTemplateVersion version;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "is_thumbnail")
    private Boolean isThumbnail;

    @PrePersist
    protected void onCreate() {
        if (isThumbnail == null) {
            isThumbnail = false;
        }
    }
}

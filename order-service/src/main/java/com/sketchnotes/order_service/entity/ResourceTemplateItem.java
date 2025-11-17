package com.sketchnotes.order_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "resource_item")
public class ResourceTemplateItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resourceItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_template_id", nullable = false)
    private ResourceTemplate resourceTemplate;

    @Column(name = "item_index")
    private Integer itemIndex;

    @Column(name = "image_url")
    private String imageUrl;    // ảnh của item show ở library để customer biết item đó trông như thế nào khi kéo vào canvas sử dụng

    @Column(name = "item_url", length = 255)
    private String itemUrl;    // link của item khi kéo vào canvas sử dụng

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

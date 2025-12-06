package com.sketchnotes.order_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "resource_template_version_item")
public class ResourceTemplateVersionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @ManyToOne
    @JoinColumn(name = "version_id", nullable = false)
    private ResourceTemplateVersion version;

    @Column(name = "item_index")
    private Integer itemIndex;

    @Column(name = "item_url")
    private String itemUrl;

    @Column(name = "image_url")
    private String imageUrl;
}

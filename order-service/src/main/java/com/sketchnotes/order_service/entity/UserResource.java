package com.sketchnotes.order_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_resources")
public class UserResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private Long resourceTemplateId;

    @Column(name = "purchased_version_id")
    private Long purchasedVersionId; // Version ID that user purchased - they can access this version + all newer versions

    private boolean active = true;   // có đang được sử dụng hay không, trường hợp hết hạn hoặc báo cáo hoàn tiền gì đó, maybe sử dụng

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

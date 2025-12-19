package com.sketchnotes.identityservice.model;

import com.sketchnotes.identityservice.enums.BlogStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "blog_moderation_history")
public class BlogModerationHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlogStatus previousStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlogStatus newStatus;
    
    @Column(nullable = false)
    private Boolean isSafe;
    
    @Column(nullable = false)
    private Integer safetyScore;
    
    @Column(columnDefinition = "TEXT")
    private String reason;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime checkedAt;
}


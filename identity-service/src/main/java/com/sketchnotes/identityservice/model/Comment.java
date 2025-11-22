package com.sketchnotes.identityservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_blog_id", columnList = "blog_id"),
    @Index(name = "idx_parent_comment_id", columnList = "parentCommentId"),
    @Index(name = "idx_deleted_at", columnList = "deletedAt")
})
@EntityListeners(AuditingEntityListener.class)
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    // parent comment id -> nếu null thì là comment gốc, nếu khác null thì reply
    private Long parentCommentId;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    private LocalDateTime deletedAt;
}
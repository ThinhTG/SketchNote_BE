package com.sketchnotes.identityservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks", 
    indexes = {
        @Index(name = "idx_course_id", columnList = "courseId"),
        @Index(name = "idx_resource_id", columnList = "resourceId"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_created_at", columnList = "createdAt")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_course", columnNames = {"user_id", "courseId"}),
        @UniqueConstraint(name = "uk_user_resource", columnNames = {"user_id", "resourceId"})
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "courseId")
    private Long courseId;  // Reference to Course in learning-service

    @Column(name = "resourceId")
    private Long resourceId;  // Reference to ResourceTemplate in order-service

    @Column(nullable = false)
    private Integer rating;  // 1-5 stars

    @Column(columnDefinition = "TEXT")
    private String comment;  // Optional comment

    @Column(name = "progress_when_submitted")
    private Integer progressWhenSubmitted;  // 0-100%, for courses only

    @Column(nullable = false)
    @Builder.Default
    private Boolean isEdited = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isVerified = true;  // User has purchased/enrolled

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Validation: Either courseId OR resourceId must be set (not both, not neither)
    @PrePersist
    @PreUpdate
    private void validateFeedbackTarget() {
        if ((courseId == null && resourceId == null) || (courseId != null && resourceId != null)) {
            throw new IllegalStateException("Feedback must have exactly one target: either courseId or resourceId");
        }
        
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalStateException("Rating must be between 1 and 5");
        }
        
        if (progressWhenSubmitted != null && (progressWhenSubmitted < 0 || progressWhenSubmitted > 100)) {
            throw new IllegalStateException("Progress must be between 0 and 100");
        }
    }

    // Helper method to check if this is course feedback
    public boolean isCourseFeedback() {
        return courseId != null;
    }

    // Helper method to check if this is resource feedback
    public boolean isResourceFeedback() {
        return resourceId != null;
    }
}

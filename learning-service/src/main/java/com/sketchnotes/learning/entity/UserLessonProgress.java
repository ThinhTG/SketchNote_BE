package com.sketchnotes.learning.entity;

import com.sketchnotes.learning.dto.enums.ProgressStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(
    name = "user_lesson_progress",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "lesson_id"})
)
public class UserLessonProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long progressId;

    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    private ProgressStatus status = ProgressStatus.NOT_STARTED;

    private Integer lastPosition = 0;   // Giây
    private Integer timeSpent = 0;      // Giây

    private LocalDateTime completedAt;
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
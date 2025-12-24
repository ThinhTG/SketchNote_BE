package com.sketchnotes.learning.repository;

import com.sketchnotes.learning.entity.UserLessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserLessonProgressRepository extends JpaRepository<UserLessonProgress, Long> {
    Optional<UserLessonProgress> findByUserIdAndLesson_LessonId(Long userId, Long lessonId);

    @Query("SELECT COUNT(p) FROM UserLessonProgress p WHERE p.userId = :userId AND p.course.courseId = :courseId AND p.status = 'COMPLETED'")
    long countCompletedLessons(Long userId, Long courseId);

    /**
     * Upsert progress - INSERT if not exists, do nothing if exists (atomic operation)
     * Returns the number of rows affected (1 if inserted, 0 if already exists)
     */
    @Modifying
    @Query(value = """
        INSERT INTO user_lesson_progress (user_id, course_id, lesson_id, status, last_position, time_spent, updated_at)
        VALUES (:userId, :courseId, :lessonId, 'NOT_STARTED', 0, 0, NOW())
        ON CONFLICT (user_id, lesson_id) DO NOTHING
        """, nativeQuery = true)
    int upsertProgress(@Param("userId") Long userId, 
                       @Param("courseId") Long courseId, 
                       @Param("lessonId") Long lessonId);
}
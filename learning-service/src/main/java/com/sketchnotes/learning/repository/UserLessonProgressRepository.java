package com.sketchnotes.learning.repository;

import com.sketchnotes.learning.entity.UserLessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserLessonProgressRepository extends JpaRepository<UserLessonProgress, Long> {
    Optional<UserLessonProgress> findByUserIdAndLesson_LessonId(Long userId, Long lessonId);

    @Query("SELECT COUNT(p) FROM UserLessonProgress p WHERE p.userId = :userId AND p.course.courseId = :courseId AND p.status = 'COMPLETED'")
    long countCompletedLessons(Long userId, Long courseId);
}
package com.sketchnotes.learning.repository;

import com.sketchnotes.learning.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourse_CourseId(Long courseId);
}
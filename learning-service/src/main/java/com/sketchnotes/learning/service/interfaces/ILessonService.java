package com.sketchnotes.learning.service.interfaces;

import com.sketchnotes.learning.dto.LessonDTO;

import java.util.List;

/**
 * Service interface for lesson operations.
 * Provides methods for managing lessons within courses.
 */
public interface ILessonService {
    
    /**
     * Create lessons for a specific course.
     * @param courseId Course ID
     * @param lessonDtos List of lesson data
     * @return List of created lessons
     */
    List<LessonDTO> createLessonsForCourse(Long courseId, List<LessonDTO> lessonDtos);
    
    /**
     * Get all lessons for a specific course.
     * @param courseId Course ID
     * @return List of lessons
     */
    List<LessonDTO> getLessonsByCourse(Long courseId);
    
    /**
     * Get a lesson by its ID.
     * @param lessonId Lesson ID
     * @return Lesson data
     */
    LessonDTO getLessonById(Long lessonId);
    
    /**
     * Update an existing lesson.
     * @param lessonId Lesson ID
     * @param dto Updated lesson data
     * @return Updated lesson
     */
    LessonDTO updateLesson(Long lessonId, LessonDTO dto);
    
    /**
     * Delete a lesson by ID.
     * @param lessonId Lesson ID
     */
    void deleteLesson(Long lessonId);
}

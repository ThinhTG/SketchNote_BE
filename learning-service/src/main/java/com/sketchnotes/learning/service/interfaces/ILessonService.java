package com.sketchnotes.learning.service.interfaces;

import com.sketchnotes.learning.dto.LessonDTO;

import java.util.List;

/**
 * Service interface for Lesson operations.
 * Controllers MUST depend on this interface, not the implementation.
 */
public interface ILessonService {
    
    List<LessonDTO> createLessonsForCourse(Long courseId, List<LessonDTO> lessonDtos);
    
    List<LessonDTO> getLessonsByCourse(Long courseId);
    
    LessonDTO getLessonById(Long lessonId);
    
    LessonDTO updateLesson(Long lessonId, LessonDTO dto);
    
    void deleteLesson(Long lessonId);
}

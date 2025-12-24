package com.sketchnotes.learning.service.interfaces;

import com.sketchnotes.learning.dto.UpdateProgressRequest;

/**
 * Service interface for Lesson Progress operations.
 * Controllers MUST depend on this interface, not the implementation.
 */
public interface ILessonProgressService {
    
    void updateLessonProgress(Long userId, Long courseId, Long lessonId, UpdateProgressRequest request);
}

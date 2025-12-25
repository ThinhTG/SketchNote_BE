package com.sketchnotes.learning.service.interfaces;

import com.sketchnotes.learning.dto.UpdateProgressRequest;

/**
 * Service interface for lesson progress operations.
 * Provides methods for tracking and updating user progress on lessons.
 */
public interface ILessonProgressService {
    
    /**
     * Update lesson progress for a user.
     * @param userId User ID
     * @param courseId Course ID
     * @param lessonId Lesson ID
     * @param request Progress update request containing completion status and position
     */
    void updateLessonProgress(Long userId, Long courseId, Long lessonId, UpdateProgressRequest request);
}

package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.dtos.request.FeedbackRequest;
import com.sketchnotes.identityservice.dtos.response.FeedbackResponse;
import com.sketchnotes.identityservice.dtos.response.FeedbackStatsResponse;

public interface FeedbackService {
    
    /**
     * Create or update course feedback
     * Validates enrollment and progress requirements
     */
    FeedbackResponse createOrUpdateCourseFeedback(Long userId, FeedbackRequest request);
    
    /**
     * Create or update resource feedback
     * Validates purchase status
     */
    FeedbackResponse createOrUpdateResourceFeedback(Long userId, FeedbackRequest request);
    
    /**
     * Get all feedbacks and statistics for a course
     */
    FeedbackStatsResponse getCourseFeedbackStats(Long courseId);
    
    /**
     * Get all feedbacks and statistics for a resource
     */
    FeedbackStatsResponse getResourceFeedbackStats(Long resourceId);
    
    /**
     * Get current user's feedback for a specific course
     */
    FeedbackResponse getUserCourseFeedback(Long userId, Long courseId);
    
    /**
     * Get current user's feedback for a specific resource
     */
    FeedbackResponse getUserResourceFeedback(Long userId, Long resourceId);
}

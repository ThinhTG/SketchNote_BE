package com.sketchnotes.interaction_service.service;

import com.sketchnotes.interaction_service.entity.Feedback;

import java.util.List;

public interface FeedbackService {
    List<Feedback> getFeedbacksByCourse(Long courseId);
    Feedback addFeedback(Feedback feedback);
    Feedback updateFeedback(Long id, Feedback feedback);
    void deleteFeedback(Long id);
}
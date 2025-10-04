package com.sketchnotes.interaction_service.service.implement;

import com.sketchnotes.interaction_service.entity.Feedback;
import com.sketchnotes.interaction_service.repository.FeedbackRepository;
import com.sketchnotes.interaction_service.service.FeedbackService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository repository;

    public FeedbackServiceImpl(FeedbackRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Feedback> getFeedbacksByCourse(Long courseId) {
        return repository.findByCourseId(courseId);
    }

    @Override
    public Feedback addFeedback(Feedback feedback) {
        return repository.save(feedback);
    }

    @Override
    public Feedback updateFeedback(Long id, Feedback feedback) {
        feedback.setFeedbackId(id);
        return repository.save(feedback);
    }

    @Override
    public void deleteFeedback(Long id) {
        repository.deleteById(id);
    }
}
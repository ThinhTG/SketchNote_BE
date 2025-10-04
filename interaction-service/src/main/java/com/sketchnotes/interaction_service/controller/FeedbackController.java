package com.sketchnotes.interaction_service.controller;

import com.sketchnotes.interaction_service.entity.Feedback;
import com.sketchnotes.interaction_service.service.FeedbackService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interactions/feedback")
public class FeedbackController {
    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    @GetMapping("/course/{courseId}")
    public List<Feedback> getCourseFeedbacks(@PathVariable Long courseId) {
        return service.getFeedbacksByCourse(courseId);
    }

    @PostMapping
    public Feedback addFeedback(@RequestBody Feedback feedback) {
        return service.addFeedback(feedback);
    }

    @PutMapping("/{id}")
    public Feedback updateFeedback(@PathVariable Long id, @RequestBody Feedback feedback) {
        return service.updateFeedback(id, feedback);
    }

    @DeleteMapping("/{id}")
    public void deleteFeedback(@PathVariable Long id) {
        service.deleteFeedback(id);
    }
}

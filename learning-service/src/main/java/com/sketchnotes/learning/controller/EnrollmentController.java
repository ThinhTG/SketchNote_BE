package com.sketchnotes.learning.controller;

import com.sketchnotes.learning.entity.CourseEnrollment;
import com.sketchnotes.learning.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/learning/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @PostMapping("/{courseId}/users/{userId}")
    public CourseEnrollment enroll(
            @PathVariable long courseId,
            @PathVariable long userId) {
        return enrollmentService.enroll(courseId, userId);
    }
}
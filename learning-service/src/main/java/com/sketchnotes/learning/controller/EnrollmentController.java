package com.sketchnotes.learning.controller;

import com.sketchnotes.learning.entity.CourseEnrollment;
import com.sketchnotes.learning.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/users/{userId}/status")
    public Object getUserCourseStatus(@PathVariable long userId) {
        return enrollmentService.getUserCourseStatus(userId);
    }
}
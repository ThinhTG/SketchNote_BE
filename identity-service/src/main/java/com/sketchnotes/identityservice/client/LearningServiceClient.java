package com.sketchnotes.identityservice.client;

import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.dtos.response.CourseEnrollmentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "learning-service")
public interface LearningServiceClient {
    
    /**
     * Get course enrollment details for a specific user and course
     * Used to validate if user is enrolled and get their progress
     */
    @GetMapping("/api/enrollments/user/{userId}/course/{courseId}")
    ApiResponse<CourseEnrollmentResponse> getEnrollment(
            @PathVariable("userId") Long userId,
            @PathVariable("courseId") Long courseId
    );
}

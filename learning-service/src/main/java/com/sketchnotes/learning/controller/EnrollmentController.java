package com.sketchnotes.learning.controller;

import com.sketchnotes.learning.client.IdentityClient;
import com.sketchnotes.learning.dto.ApiResponse;
import com.sketchnotes.learning.dto.CourseDTO;
import com.sketchnotes.learning.dto.EnrollmentDTO;
import com.sketchnotes.learning.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learning/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
    private final EnrollmentService enrollmentService;
    private final IdentityClient identityClient;

    @PostMapping("/{courseId}")
    public ResponseEntity<ApiResponse<EnrollmentDTO>> enroll(
            @PathVariable long courseId) {
        var user = identityClient.getCurrentUser().getResult();
        EnrollmentDTO enrollment = enrollmentService.enroll(courseId, user.getId());
        return new ResponseEntity<>(
                ApiResponse.success(enrollment, "Enrolled successfully"),
                HttpStatus.CREATED
        );
    }


    @GetMapping("/users/status")
    public ResponseEntity<ApiResponse<Map<String, List<CourseDTO>>>> getUserCourseStatus() {
        var user = identityClient.getCurrentUser().getResult();
        Map<String, List<CourseDTO>> status = enrollmentService.getUserCourseStatus(user.getId());
        return ResponseEntity.ok(ApiResponse.success(status, "User course status retrieved successfully"));
    }
}
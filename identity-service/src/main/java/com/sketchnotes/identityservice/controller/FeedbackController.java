package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.dtos.request.FeedbackRequest;
import com.sketchnotes.identityservice.dtos.response.FeedbackResponse;
import com.sketchnotes.identityservice.dtos.response.FeedbackStatsResponse;
import com.sketchnotes.identityservice.dtos.response.UserResponse;
import com.sketchnotes.identityservice.service.interfaces.FeedbackService;
import com.sketchnotes.identityservice.service.interfaces.IUserService;
import com.sketchnotes.identityservice.ultils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Feedback", description = "Feedback management APIs for courses and resources")
public class FeedbackController {
    
    private final FeedbackService feedbackService;
    private final IUserService userService;
    
    // ==================== COURSE FEEDBACK ENDPOINTS ====================
    
    @PostMapping("/course")
    @PreAuthorize("hasAnyAuthority('STUDENT', 'DESIGNER')")
    @Operation(summary = "Create or update course feedback", 
               description = "Submit feedback for a course. Requires 30% progress for rating, 100% for comments.")
    public ResponseEntity<ApiResponse<FeedbackResponse>> createOrUpdateCourseFeedback(
            @Valid @RequestBody FeedbackRequest request) {
        
        log.info("Received course feedback request: {}", request);
        
        // Get current user ID from security context
        UserResponse currentUser = userService.getCurrentUser();
        Long userId = currentUser.getId();
        
        FeedbackResponse response = feedbackService.createOrUpdateCourseFeedback(userId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<FeedbackResponse>builder()
                        .code(HttpStatus.CREATED.value())
                        .message("Course feedback submitted successfully")
                        .result(response)
                        .build());
    }
    
    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get all feedback for a course", 
               description = "Retrieve all feedbacks and statistics for a specific course. Public endpoint.")
    public ResponseEntity<ApiResponse<FeedbackStatsResponse>> getCourseFeedbackStats(
            @PathVariable Long courseId) {
        
        log.info("Getting feedback stats for course: {}", courseId);
        
        FeedbackStatsResponse stats = feedbackService.getCourseFeedbackStats(courseId);
        
        return ResponseEntity.ok(ApiResponse.<FeedbackStatsResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Course feedback retrieved successfully")
                .result(stats)
                .build());
    }
    
    @GetMapping("/course/{courseId}/my-feedback")
    @PreAuthorize("hasAnyAuthority('STUDENT', 'DESIGNER')")
    @Operation(summary = "Get current user's feedback for a course", 
               description = "Retrieve the authenticated user's feedback for a specific course.")
    public ResponseEntity<ApiResponse<FeedbackResponse>> getUserCourseFeedback(
            @PathVariable Long courseId) {
        
        UserResponse currentUser = userService.getCurrentUser();
        Long userId = currentUser.getId();
        log.info("Getting user {} feedback for course {}", userId, courseId);
        
        FeedbackResponse feedback = feedbackService.getUserCourseFeedback(userId, courseId);
        
        if (feedback == null) {
            return ResponseEntity.ok(ApiResponse.<FeedbackResponse>builder()
                    .code(HttpStatus.OK.value())
                    .message("No feedback found")
                    .result(null)
                    .build());
        }
        
        return ResponseEntity.ok(ApiResponse.<FeedbackResponse>builder()
                .code(HttpStatus.OK.value())
                .message("User course feedback retrieved successfully")
                .result(feedback)
                .build());
    }
    
    // ==================== RESOURCE FEEDBACK ENDPOINTS ====================
    
    @PostMapping("/resource")
    @PreAuthorize("hasAnyAuthority('STUDENT', 'DESIGNER')")
    @Operation(summary = "Create or update resource feedback", 
               description = "Submit feedback for a resource. Requires purchase verification.")
    public ResponseEntity<ApiResponse<FeedbackResponse>> createOrUpdateResourceFeedback(
            @Valid @RequestBody FeedbackRequest request) {
        
        log.info("Received resource feedback request: {}", request);
        
        // Get current user ID from security context
        UserResponse currentUser = userService.getCurrentUser();
        Long userId = currentUser.getId();
        
        FeedbackResponse response = feedbackService.createOrUpdateResourceFeedback(userId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<FeedbackResponse>builder()
                        .code(HttpStatus.CREATED.value())
                        .message("Resource feedback submitted successfully")
                        .result(response)
                        .build());
    }
    
    @GetMapping("/resource/{resourceId}")
    @Operation(summary = "Get all feedback for a resource", 
               description = "Retrieve all feedbacks and statistics for a specific resource. Public endpoint.")
    public ResponseEntity<ApiResponse<FeedbackStatsResponse>> getResourceFeedbackStats(
            @PathVariable Long resourceId) {
        
        log.info("Getting feedback stats for resource: {}", resourceId);
        
        FeedbackStatsResponse stats = feedbackService.getResourceFeedbackStats(resourceId);
        
        return ResponseEntity.ok(ApiResponse.<FeedbackStatsResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Resource feedback retrieved successfully")
                .result(stats)
                .build());
    }
    
    @GetMapping("/resource/{resourceId}/my-feedback")
    @PreAuthorize("hasAnyAuthority('STUDENT', 'DESIGNER')")
    @Operation(summary = "Get current user's feedback for a resource", 
               description = "Retrieve the authenticated user's feedback for a specific resource.")
    public ResponseEntity<ApiResponse<FeedbackResponse>> getUserResourceFeedback(
            @PathVariable Long resourceId) {
        
        UserResponse currentUser = userService.getCurrentUser();
        Long userId = currentUser.getId();
        log.info("Getting user {} feedback for resource {}", userId, resourceId);
        
        FeedbackResponse feedback = feedbackService.getUserResourceFeedback(userId, resourceId);
        
        if (feedback == null) {
            return ResponseEntity.ok(ApiResponse.<FeedbackResponse>builder()
                    .code(HttpStatus.OK.value())
                    .message("No feedback found")
                    .result(null)
                    .build());
        }
        
        return ResponseEntity.ok(ApiResponse.<FeedbackResponse>builder()
                .code(HttpStatus.OK.value())
                .message("User resource feedback retrieved successfully")
                .result(feedback)
                .build());
    }
}

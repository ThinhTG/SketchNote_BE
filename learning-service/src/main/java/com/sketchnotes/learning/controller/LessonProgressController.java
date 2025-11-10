package com.sketchnotes.learning.controller;

import com.sketchnotes.learning.client.IdentityClient;
import com.sketchnotes.learning.dto.ApiResponse;
import com.sketchnotes.learning.dto.UpdateProgressRequest;
import com.sketchnotes.learning.service.LessonProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
public class LessonProgressController {
    private final LessonProgressService lessonProgressService;
    private final IdentityClient identityClient;

    @PostMapping("/courses/{courseId}/lessons/{lessonId}/progress")
    public ResponseEntity<ApiResponse<Void>> updateProgress(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @RequestBody UpdateProgressRequest request) {

        // Lấy userId từ token thông qua identity service
        Long userId = identityClient.getCurrentUser().getResult().getId();
        
        lessonProgressService.updateLessonProgress(userId, courseId, lessonId, request);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Progress updated successfully"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, ex.getMessage(), null));
    }
}
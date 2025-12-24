package com.sketchnotes.learning.controller;

import com.sketchnotes.learning.dto.ApiResponse;
import com.sketchnotes.learning.dto.LessonDTO;
import com.sketchnotes.learning.service.interfaces.ILessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final ILessonService lessonService;

    @PostMapping("/{courseId}")
    public ResponseEntity<ApiResponse<List<LessonDTO>>> createLessons(
            @PathVariable Long courseId,
            @RequestBody List<LessonDTO> dtos) {
        List<LessonDTO> lessons = lessonService.createLessonsForCourse(courseId, dtos);
        return new ResponseEntity<>(
                ApiResponse.success(lessons, "Lessons created successfully"),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<LessonDTO>>> getLessonsByCourse(@PathVariable Long courseId) {
        List<LessonDTO> lessons = lessonService.getLessonsByCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success(lessons, "Lessons fetched successfully"));
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<ApiResponse<LessonDTO>> getLessonById(@PathVariable Long lessonId) {
        LessonDTO lesson = lessonService.getLessonById(lessonId);
        return ResponseEntity.ok(ApiResponse.success(lesson, "Lesson retrieved successfully"));
    }

    @PutMapping("/{lessonId}")
    public ResponseEntity<ApiResponse<LessonDTO>> updateLesson(
            @PathVariable Long lessonId,
            @RequestBody LessonDTO dto) {
        LessonDTO updated = lessonService.updateLesson(lessonId, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Lesson updated successfully"));
    }

    @DeleteMapping("/{lessonId}")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(@PathVariable Long lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.ok(ApiResponse.success(null, "Lesson deleted successfully"));
    }
}
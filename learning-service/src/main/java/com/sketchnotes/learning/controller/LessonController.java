package com.sketchnotes.learning.controller;

import com.sketchnotes.learning.dto.ApiResponse;
import com.sketchnotes.learning.dto.LessonDTO;
import com.sketchnotes.learning.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

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

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<List<LessonDTO>>> getLessons(@PathVariable Long courseId) {
        List<LessonDTO> lessons = lessonService.getLessonsByCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success(lessons, "Lessons fetched successfully"));
    }
}
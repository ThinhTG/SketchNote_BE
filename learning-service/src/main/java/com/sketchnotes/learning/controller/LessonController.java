package com.sketchnotes.learning.controller;

import com.sketchnotes.learning.dto.ApiResponse;
import com.sketchnotes.learning.dto.LessonDTO;
import com.sketchnotes.learning.entity.Lesson;
import com.sketchnotes.learning.service.LessonService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning/lessons")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @PostMapping("/{courseId}")
    public ApiResponse<List<Lesson>> createLessons(
            @PathVariable Long courseId,
            @RequestBody List<LessonDTO> dtos) {
        List<Lesson> lessons = lessonService.createLessonsForCourse(courseId, dtos);
        return ApiResponse.success(lessons, "Lessons created successfully");
    }

    @GetMapping("/{courseId}")
    public ApiResponse<List<Lesson>> getLessons(@PathVariable Long courseId) {
        List<Lesson> lessons = lessonService.getLessonsByCourse(courseId);
        return ApiResponse.success(lessons, "Lessons fetched successfully");
    }
}
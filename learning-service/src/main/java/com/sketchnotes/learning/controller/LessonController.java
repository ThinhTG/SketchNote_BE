package com.sketchnotes.learning.controller;

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
    public List<Lesson> createLessons(@PathVariable Long courseId, @RequestBody List<LessonDTO> dtos) {
        return lessonService.createLessonsForCourse(courseId, dtos);
    }

    @GetMapping("/{courseId}")
    public List<Lesson> getLessons(@PathVariable Long courseId) {
        return lessonService.getLessonsByCourse(courseId);
    }
}
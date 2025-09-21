package com.sketchnotes.learning.controller;

import com.sketchnotes.learning.dto.ApiResponse;
import com.sketchnotes.learning.dto.CourseDTO;
import com.sketchnotes.learning.entity.Course;
import com.sketchnotes.learning.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    @PostMapping("/courses")
    public ResponseEntity<ApiResponse<Course>> createCourse(@RequestBody CourseDTO courseDTO) {
        Course saved = courseService.createCourse(courseDTO);
        return ResponseEntity.ok(ApiResponse.success(saved, "Course created successfully"));
    }

}
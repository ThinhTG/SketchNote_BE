package com.sketchnotes.learning.controller;

import com.sketchnotes.learning.repository.CourseEnrollmentRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses/admin/stats")
public class AdminCourseStatsController {

    private final CourseEnrollmentRepository courseEnrollmentRepository;

    public AdminCourseStatsController(CourseEnrollmentRepository courseEnrollmentRepository) {
        this.courseEnrollmentRepository = courseEnrollmentRepository;
    }

    @GetMapping("/top-selling")
    public List<Map<String, Object>> getTopSellingCourses(@RequestParam(defaultValue = "5") int limit) {
        List<Object[]> results = courseEnrollmentRepository.findTopSellingCourses();
        return results.stream()
                .limit(limit)
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("courseId", row[0]);
                    map.put("enrollmentCount", row[1]);
                    return map;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/total-enrollments")
    public long getTotalEnrollments() {
        return courseEnrollmentRepository.countTotalEnrollments();
    }
}

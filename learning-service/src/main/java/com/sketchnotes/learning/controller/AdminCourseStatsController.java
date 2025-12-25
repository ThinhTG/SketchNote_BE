package com.sketchnotes.learning.controller;

import com.sketchnotes.learning.service.interfaces.IAdminCourseStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses/admin/stats")
@RequiredArgsConstructor
public class AdminCourseStatsController {

    private final IAdminCourseStatsService adminCourseStatsService;

    @GetMapping("/top-selling")
    public List<Map<String, Object>> getTopSellingCourses(@RequestParam(defaultValue = "5") int limit) {
        return adminCourseStatsService.getTopSellingCourses(limit);
    }

    @GetMapping("/total-enrollments")
    public long getTotalEnrollments() {
        return adminCourseStatsService.getTotalEnrollments();
    }
}

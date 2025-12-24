package com.sketchnotes.learning.controller;

import com.sketchnotes.learning.dto.ApiResponse;
import com.sketchnotes.learning.dto.response.TopSellingCourseDTO;
import com.sketchnotes.learning.dto.response.TotalEnrollmentsDTO;
import com.sketchnotes.learning.service.interfaces.IAdminCourseStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses/admin/stats")
@RequiredArgsConstructor
public class AdminCourseStatsController {

    private final IAdminCourseStatsService adminCourseStatsService;

    @GetMapping("/top-selling")
    public ResponseEntity<ApiResponse<List<TopSellingCourseDTO>>> getTopSellingCourses(
            @RequestParam(defaultValue = "5") int limit) {
        List<TopSellingCourseDTO> topCourses = adminCourseStatsService.getTopSellingCourses(limit);
        return ResponseEntity.ok(ApiResponse.success(topCourses, "Top selling courses retrieved successfully"));
    }

    @GetMapping("/total-enrollments")
    public ResponseEntity<ApiResponse<TotalEnrollmentsDTO>> getTotalEnrollments() {
        long total = adminCourseStatsService.getTotalEnrollments();
        TotalEnrollmentsDTO dto = TotalEnrollmentsDTO.builder()
                .totalEnrollments(total)
                .build();
        return ResponseEntity.ok(ApiResponse.success(dto, "Total enrollments retrieved successfully"));
    }
}

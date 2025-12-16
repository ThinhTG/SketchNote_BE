package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.service.implement.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {
    private final AdminStatsService adminStatsService;

    @GetMapping("/users")
    public ResponseEntity<Map<String, Long>> getUserStats() {
        return ResponseEntity.ok(adminStatsService.getUserStats());
    }

    @GetMapping("/course-revenue")
    public ResponseEntity<List<Map<String, Object>>> getCourseRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "day") String groupBy) {
        return ResponseEntity.ok(adminStatsService.getCourseRevenue(start, end, groupBy));
    }
}

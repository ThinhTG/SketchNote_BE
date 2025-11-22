package com.sketchnotes.order_service.controller;

import com.sketchnotes.order_service.dtos.ApiResponse;
import com.sketchnotes.order_service.dtos.admin.AdminDashboardResponseDTO;
import com.sketchnotes.order_service.service.AdminDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/orders/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<AdminDashboardResponseDTO.UserStatsDTO>> getUsers() {
        AdminDashboardResponseDTO.UserStatsDTO result = adminDashboardService.getUserStats();
        return ResponseEntity.ok(ApiResponse.success(result, "Admin dashboard user stats"));
    }

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<AdminDashboardResponseDTO.RevenueStatsDTO>> getRevenue(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(defaultValue = "day") String groupBy,
            @RequestParam(required = false) String type) {
        
        LocalDateTime s = parseOrDefaultStart(start);
        LocalDateTime e = parseOrDefaultEnd(end);
        
        AdminDashboardResponseDTO.RevenueStatsDTO result = adminDashboardService.getRevenueStats(s, e, groupBy, type);
        return ResponseEntity.ok(ApiResponse.success(result, "Admin dashboard revenue stats"));
    }

    private LocalDateTime parseOrDefaultStart(String v) {
        if (v == null || v.trim().isEmpty()) {
            return LocalDateTime.of(1970, 1, 1, 0, 0);
        }
        try {
            return LocalDateTime.parse(v);
        } catch (DateTimeParseException ex) {
            try {
                return java.time.LocalDate.parse(v).atStartOfDay();
            } catch (Exception e) {
                return LocalDateTime.of(1970, 1, 1, 0, 0);
            }
        }
    }

    private LocalDateTime parseOrDefaultEnd(String v) {
        if (v == null || v.trim().isEmpty()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(v);
        } catch (DateTimeParseException ex) {
            try {
                return java.time.LocalDate.parse(v).atTime(23, 59, 59);
            } catch (Exception e) {
                return LocalDateTime.now();
            }
        }
    }
}

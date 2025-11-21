package com.sketchnotes.order_service.controller;

import com.sketchnotes.order_service.client.IdentityClient;
import com.sketchnotes.order_service.dtos.ApiResponse;
import com.sketchnotes.order_service.dtos.designer.DesignerDashboardSummaryDTO;
import com.sketchnotes.order_service.dtos.designer.RevenueChartResponseDTO;
import com.sketchnotes.order_service.dtos.designer.TimeSeriesPointDTO;
import com.sketchnotes.order_service.dtos.designer.TopTemplateDTO;
import com.sketchnotes.order_service.service.designer.DesignerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/orders/designer/dashboard")
@RequiredArgsConstructor
public class DesignerDashboardController {

    private final DesignerDashboardService dashboardService;
    private final IdentityClient  identityClient;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DesignerDashboardSummaryDTO>> getSummary(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        var designerId = identityClient.getCurrentUser().getResult().getId();
        LocalDateTime s = parseOrDefaultStart(start);
        LocalDateTime e = parseOrDefaultEnd(end);
        DesignerDashboardSummaryDTO dto = dashboardService.getSummary(designerId, s, e);
        return ResponseEntity.ok(ApiResponse.success(dto, "Designer dashboard summary"));
    }

    @GetMapping("/top-templates")
    public ResponseEntity<ApiResponse<List<TopTemplateDTO>>> getTopTemplates(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(defaultValue = "10") int limit) {
        var designerId = identityClient.getCurrentUser().getResult().getId();
        LocalDateTime s = parseOrDefaultStart(start);
        LocalDateTime e = parseOrDefaultEnd(end);
        List<TopTemplateDTO> list = dashboardService.getTopTemplates(designerId, s, e, limit);
        return ResponseEntity.ok(ApiResponse.success(list, "Top templates"));
    }

    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<RevenueChartResponseDTO>> getSalesTimeSeries(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(defaultValue = "day") String groupBy) {
        var designerId = identityClient.getCurrentUser().getResult().getId();
        LocalDateTime s = parseOrDefaultStart(start);
        LocalDateTime e = parseOrDefaultEnd(end);
        RevenueChartResponseDTO response = dashboardService.getSalesTimeSeries(designerId, s, e, groupBy);
        return ResponseEntity.ok(ApiResponse.success(response, "Sales time series"));
    }

    private LocalDateTime parseOrDefaultStart(String v) {
        if (v == null || v.trim().isEmpty()) {
            return LocalDateTime.of(1970, 1, 1, 0, 0);
        }
        
        try {
            // Try parsing as full ISO DateTime first (2025-01-01T00:00:00)
            return LocalDateTime.parse(v);
        } catch (DateTimeParseException ex1) {
            try {
                // Try parsing as date only (2025-01-01)
                return java.time.LocalDate.parse(v).atStartOfDay();
            } catch (DateTimeParseException ex2) {
                try {
                    // Try parsing as year-month (2025-08)
                    java.time.YearMonth yearMonth = java.time.YearMonth.parse(v);
                    return yearMonth.atDay(1).atStartOfDay();
                } catch (DateTimeParseException ex3) {
                    try {
                        // Try parsing as year only (2025)
                        int year = Integer.parseInt(v.trim());
                        return LocalDateTime.of(year, 1, 1, 0, 0);
                    } catch (NumberFormatException ex4) {
                        // If all parsing fails, return default
                        return LocalDateTime.of(1970, 1, 1, 0, 0);
                    }
                }
            }
        }
    }

    private LocalDateTime parseOrDefaultEnd(String v) {
        if (v == null || v.trim().isEmpty()) {
            return LocalDateTime.now();
        }
        
        try {
            // Try parsing as full ISO DateTime first (2025-12-31T23:59:59)
            return LocalDateTime.parse(v);
        } catch (DateTimeParseException ex1) {
            try {
                // Try parsing as date only (2025-12-31)
                return java.time.LocalDate.parse(v).atTime(23, 59, 59);
            } catch (DateTimeParseException ex2) {
                try {
                    // Try parsing as year-month (2025-08) - end of month
                    java.time.YearMonth yearMonth = java.time.YearMonth.parse(v);
                    return yearMonth.atEndOfMonth().atTime(23, 59, 59);
                } catch (DateTimeParseException ex3) {
                    try {
                        // Try parsing as year only (2025) - end of year
                        int year = Integer.parseInt(v.trim());
                        return LocalDateTime.of(year, 12, 31, 23, 59, 59);
                    } catch (NumberFormatException ex4) {
                        // If all parsing fails, return current time
                        return LocalDateTime.now();
                    }
                }
            }
        }
    }
}

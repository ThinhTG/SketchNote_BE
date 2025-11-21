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
        if (v == null) return LocalDateTime.of(1970,1,1,0,0);
        try {
            return LocalDateTime.parse(v);
        } catch (DateTimeParseException ex) {
            return LocalDateTime.of(1970,1,1,0,0);
        }
    }

    private LocalDateTime parseOrDefaultEnd(String v) {
        if (v == null) return LocalDateTime.now();
        try {
            return LocalDateTime.parse(v);
        } catch (DateTimeParseException ex) {
            return LocalDateTime.now();
        }
    }
}

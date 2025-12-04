package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.dtos.response.admin.AdminRevenueDashboardDTO;
import com.sketchnotes.identityservice.dtos.response.admin.AdminRevenueStatsDTO;
import com.sketchnotes.identityservice.dtos.response.admin.AdminWalletOverviewDTO;
import com.sketchnotes.identityservice.service.interfaces.IAdminRevenueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Controller cho Admin Revenue Dashboard
 * 
 * Logic nghiệp vụ:
 * - Revenue = Tiền Admin nhận được từ bán Subscription + Token/AI Credits
 * - KHÔNG bao gồm: Deposit, Withdraw (đây là tiền của user)
 * 
 * Base path: /api/admin/revenue
 */
@RestController
@RequestMapping("/api/admin/revenue")
@RequiredArgsConstructor
@Slf4j
public class AdminRevenueController {
    
    private final IAdminRevenueService adminRevenueService;
    
    // ==================== TỔNG QUAN ====================
    
    /**
     * Lấy tổng quan Admin Wallet
     * GET /api/admin/revenue/overview
     * 
     * Response: Tổng số dư, tổng từ subscription, tổng từ token, 
     *           thông tin tham khảo (deposit/withdraw của user)
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminWalletOverviewDTO>> getWalletOverview() {
        log.info("Admin: Getting wallet overview");
        
        AdminWalletOverviewDTO overview = adminRevenueService.getWalletOverview();
        
        return ResponseEntity.ok(ApiResponse.success(overview, "Admin wallet overview retrieved successfully"));
    }
    
    // ==================== THỐNG KÊ DOANH THU ====================
    
    /**
     * Lấy thống kê doanh thu theo khoảng thời gian
     * GET /api/admin/revenue/stats?start=2024-01-01&end=2024-12-31&groupBy=month&type=all
     * 
     * @param start Ngày bắt đầu (format: yyyy-MM-dd), mặc định 30 ngày trước
     * @param end Ngày kết thúc (format: yyyy-MM-dd), mặc định hôm nay
     * @param groupBy Cách nhóm: "day", "month", "year" (mặc định: day)
     * @param type Loại doanh thu: "all", "subscription", "token" (mặc định: all)
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminRevenueStatsDTO>> getRevenueStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "day") String groupBy,
            @RequestParam(defaultValue = "all") String type) {
        
        // Default: 30 days ago to now
        LocalDateTime startDateTime = start != null 
                ? start.atStartOfDay() 
                : LocalDateTime.now().minusDays(30);
        LocalDateTime endDateTime = end != null 
                ? end.atTime(LocalTime.MAX) 
                : LocalDateTime.now();
        
        log.info("Admin: Getting revenue stats from {} to {}, groupBy={}, type={}", 
                startDateTime, endDateTime, groupBy, type);
        
        AdminRevenueStatsDTO stats = adminRevenueService.getRevenueStats(
                startDateTime, endDateTime, groupBy, type);
        
        return ResponseEntity.ok(ApiResponse.success(stats, "Revenue stats retrieved successfully"));
    }
    
    /**
     * Lấy toàn bộ dashboard data trong một request
     * GET /api/admin/revenue/dashboard?start=2024-01-01&end=2024-12-31&groupBy=month
     * 
     * Response bao gồm: overview, stats, top subscriptions, top token packages
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminRevenueDashboardDTO>> getRevenueDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "day") String groupBy) {
        
        LocalDateTime startDateTime = start != null 
                ? start.atStartOfDay() 
                : LocalDateTime.now().minusDays(30);
        LocalDateTime endDateTime = end != null 
                ? end.atTime(LocalTime.MAX) 
                : LocalDateTime.now();
        
        log.info("Admin: Getting full revenue dashboard from {} to {}", startDateTime, endDateTime);
        
        AdminRevenueDashboardDTO dashboard = adminRevenueService.getRevenueDashboard(
                startDateTime, endDateTime, groupBy);
        
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Revenue dashboard retrieved successfully"));
    }
    
    // ==================== TOP ITEMS ====================
    
    /**
     * Lấy top subscription plans bán chạy nhất
     * GET /api/admin/revenue/top-subscriptions?limit=5
     */
    @GetMapping("/top-subscriptions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AdminRevenueDashboardDTO.TopSubscriptionDTO>>> getTopSubscriptions(
            @RequestParam(defaultValue = "5") int limit) {
        
        log.info("Admin: Getting top {} subscriptions", limit);
        
        List<AdminRevenueDashboardDTO.TopSubscriptionDTO> topSubscriptions = 
                adminRevenueService.getTopSubscriptions(limit);
        
        return ResponseEntity.ok(ApiResponse.success(topSubscriptions, "Top subscriptions retrieved successfully"));
    }
    
    /**
     * Lấy top token/credit packages bán chạy nhất
     * GET /api/admin/revenue/top-token-packages?limit=5
     */
    @GetMapping("/top-token-packages")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AdminRevenueDashboardDTO.TopTokenPackageDTO>>> getTopTokenPackages(
            @RequestParam(defaultValue = "5") int limit) {
        
        log.info("Admin: Getting top {} token packages", limit);
        
        List<AdminRevenueDashboardDTO.TopTokenPackageDTO> topPackages = 
                adminRevenueService.getTopTokenPackages(limit);
        
        return ResponseEntity.ok(ApiResponse.success(topPackages, "Top token packages retrieved successfully"));
    }
    
    // ==================== SO SÁNH ====================
    
    /**
     * Lấy phần trăm tăng trưởng doanh thu so với kỳ trước
     * GET /api/admin/revenue/growth?start=2024-01-01&end=2024-01-31
     * 
     * So sánh doanh thu kỳ hiện tại với kỳ trước có cùng độ dài
     */
    @GetMapping("/growth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Double>> getRevenueGrowth(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        
        LocalDateTime startDateTime = start != null 
                ? start.atStartOfDay() 
                : LocalDateTime.now().minusDays(30);
        LocalDateTime endDateTime = end != null 
                ? end.atTime(LocalTime.MAX) 
                : LocalDateTime.now();
        
        log.info("Admin: Getting revenue growth from {} to {}", startDateTime, endDateTime);
        
        Double growth = adminRevenueService.getRevenueGrowthPercentage(startDateTime, endDateTime);
        
        return ResponseEntity.ok(ApiResponse.success(growth, "Revenue growth calculated successfully"));
    }
}

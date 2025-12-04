package com.sketchnotes.order_service.controller;

import com.sketchnotes.order_service.dtos.ApiResponse;
import com.sketchnotes.order_service.dtos.OrderResponseDTO;
import com.sketchnotes.order_service.dtos.admin.AdminDashboardResponseDTO;
import com.sketchnotes.order_service.service.AdminDashboardService;
import com.sketchnotes.order_service.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Controller cho Admin Dashboard - Order Service
 * 
 * LƯU Ý: Logic thống kê REVENUE đã được chuyển sang identity-service.
 * Sử dụng API mới: /api/admin/revenue/* trong identity-service
 * 
 * Controller này chỉ còn quản lý:
 * - User stats (proxy từ identity-service)
 * - Order management
 * - Top selling items (resources, courses)
 * - Top designers
 */
@RestController
@RequestMapping("/api/orders/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;
    private final OrderService orderService;

    public AdminDashboardController(AdminDashboardService adminDashboardService, OrderService orderService) {
        this.adminDashboardService = adminDashboardService;
        this.orderService = orderService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<AdminDashboardResponseDTO.UserStatsDTO>> getUsers() {
        AdminDashboardResponseDTO.UserStatsDTO result = adminDashboardService.getUserStats();
        return ResponseEntity.ok(ApiResponse.success(result, "Admin dashboard user stats"));
    }

    /**
     * @deprecated Sử dụng API mới: GET /api/admin/revenue/stats trong identity-service
     * 
     * API mới cung cấp:
     * - Doanh thu từ Subscription
     * - Doanh thu từ Token/AI Credits  
     * - KHÔNG bao gồm Deposit/Withdraw (đây là tiền của user, không phải revenue)
     */
    @Deprecated
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<String>> getRevenue(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(defaultValue = "day") String groupBy,
            @RequestParam(required = false) String type) {
        
        return ResponseEntity.ok(ApiResponse.success(
            "API này đã deprecated. Vui lòng sử dụng GET /api/admin/revenue/stats trong identity-service",
            "Revenue API moved to identity-service"));
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<AdminDashboardResponseDTO.OverviewStatsDTO>> getOverview() {
        AdminDashboardResponseDTO.OverviewStatsDTO result = adminDashboardService.getOverviewStats();
        return ResponseEntity.ok(ApiResponse.success(result, "Admin dashboard overview stats"));
    }

    @GetMapping("/top-courses")
    public ResponseEntity<ApiResponse<java.util.List<AdminDashboardResponseDTO.TopItemDTO>>> getTopCourses(@RequestParam(defaultValue = "5") int limit) {
        java.util.List<AdminDashboardResponseDTO.TopItemDTO> result = adminDashboardService.getTopSellingCourses(limit);
        return ResponseEntity.ok(ApiResponse.success(result, "Top selling courses"));
    }

    @GetMapping("/top-resources")
    public ResponseEntity<ApiResponse<java.util.List<AdminDashboardResponseDTO.TopItemDTO>>> getTopResources(@RequestParam(defaultValue = "5") int limit) {
        java.util.List<AdminDashboardResponseDTO.TopItemDTO> result = adminDashboardService.getTopSellingResources(limit);
        return ResponseEntity.ok(ApiResponse.success(result, "Top selling resources"));
    }

    @GetMapping("/top-designers")
    public ResponseEntity<ApiResponse<java.util.List<AdminDashboardResponseDTO.TopDesignerDTO>>> getTopDesigners(@RequestParam(defaultValue = "5") int limit) {
        java.util.List<AdminDashboardResponseDTO.TopDesignerDTO> result = adminDashboardService.getTopDesigners(limit);
        return ResponseEntity.ok(ApiResponse.success(result, "Top designers by revenue"));
    }

    @GetMapping("/subscription-stats")
    public ResponseEntity<ApiResponse<java.util.List<AdminDashboardResponseDTO.SubscriptionStatDTO>>> getSubscriptionStats() {
        java.util.List<AdminDashboardResponseDTO.SubscriptionStatDTO> result = adminDashboardService.getSubscriptionStats();
        return ResponseEntity.ok(ApiResponse.success(result, "Subscription stats"));
    }
    
    // ==================== ORDER MANAGEMENT ====================
    
    /**
     * Lấy danh sách tất cả orders với phân trang và filter
     * GET /api/orders/admin/dashboard/orders?search=&orderStatus=&paymentStatus=&page=0&size=10
     * 
     * orderStatus options: PENDING, SUCCESS, CANCELLED
     * paymentStatus options: PENDING, PAID, FAILED, CANCELLED
     */
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<OrderResponseDTO>>> getAllOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<OrderResponseDTO> orders = orderService.getAllOrders(search, orderStatus, paymentStatus, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(orders, "Orders retrieved successfully"));
    }
    
    /**
     * Lấy orders của một user cụ thể
     * GET /api/orders/admin/dashboard/orders/user/{userId}?page=0&size=10
     */
    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<ApiResponse<Page<OrderResponseDTO>>> getOrdersByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderResponseDTO> orders = orderService.getOrdersByUserId(userId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(orders, "User orders retrieved successfully"));
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

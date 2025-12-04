package com.sketchnotes.order_service.service;

import com.sketchnotes.order_service.dtos.admin.AdminDashboardResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service cho Admin Dashboard - Order Service
 * 
 * LƯU Ý: Logic thống kê REVENUE đã được chuyển sang identity-service.
 * Sử dụng IAdminRevenueService trong identity-service để lấy doanh thu.
 * 
 * Service này chỉ còn quản lý:
 * - User stats
 * - Order overview stats
 * - Top selling items
 */
public interface AdminDashboardService {
    AdminDashboardResponseDTO.UserStatsDTO getUserStats();
    
    /**
     * @deprecated Sử dụng IAdminRevenueService.getRevenueStats() trong identity-service
     */
    @Deprecated
    AdminDashboardResponseDTO.RevenueStatsDTO getRevenueStats(LocalDateTime start, LocalDateTime end, String groupBy, String type);
    
    AdminDashboardResponseDTO.OverviewStatsDTO getOverviewStats();
    List<AdminDashboardResponseDTO.TopItemDTO> getTopSellingCourses(int limit);
    List<AdminDashboardResponseDTO.TopItemDTO> getTopSellingResources(int limit);
    List<AdminDashboardResponseDTO.TopDesignerDTO> getTopDesigners(int limit);
    List<AdminDashboardResponseDTO.SubscriptionStatDTO> getSubscriptionStats();
}
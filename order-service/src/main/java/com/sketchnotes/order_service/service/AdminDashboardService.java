package com.sketchnotes.order_service.service;

import com.sketchnotes.order_service.dtos.admin.AdminDashboardResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminDashboardService {
    AdminDashboardResponseDTO.UserStatsDTO getUserStats();
    AdminDashboardResponseDTO.RevenueStatsDTO getRevenueStats(LocalDateTime start, LocalDateTime end, String groupBy, String type);
    AdminDashboardResponseDTO.OverviewStatsDTO getOverviewStats();
    List<AdminDashboardResponseDTO.TopItemDTO> getTopSellingCourses(int limit);
    List<AdminDashboardResponseDTO.TopItemDTO> getTopSellingResources(int limit);
    List<AdminDashboardResponseDTO.TopDesignerDTO> getTopDesigners(int limit);
    List<AdminDashboardResponseDTO.SubscriptionStatDTO> getSubscriptionStats();
}

package com.sketchnotes.order_service.service;

import com.sketchnotes.order_service.dtos.admin.AdminDashboardResponseDTO;

import java.time.LocalDateTime;

public interface AdminDashboardService {
    AdminDashboardResponseDTO.UserStatsDTO getUserStats();
    AdminDashboardResponseDTO.RevenueStatsDTO getRevenueStats(LocalDateTime start, LocalDateTime end, String groupBy, String type);
}

package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.dtos.response.admin.AdminRevenueDashboardDTO;
import com.sketchnotes.identityservice.dtos.response.admin.AdminRevenueStatsDTO;
import com.sketchnotes.identityservice.dtos.response.admin.AdminWalletOverviewDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface cho Admin Revenue Dashboard
 * 
 * Logic nghiệp vụ:
 * - Revenue = Tiền Admin nhận được từ bán dịch vụ
 * - Bao gồm: Subscription + Token/AI Credits purchases
 * - KHÔNG bao gồm: Deposit, Withdraw (đây là tiền của user)
 */
public interface IAdminRevenueService {
    
    // ==================== TỔNG QUAN WALLET ====================
    
    /**
     * Lấy tổng quan Admin Wallet
     * @return AdminWalletOverviewDTO chứa số dư và thông tin tổng quan
     */
    AdminWalletOverviewDTO getWalletOverview();
    
    // ==================== THỐNG KÊ DOANH THU ====================
    
    /**
     * Lấy thống kê doanh thu theo khoảng thời gian và nhóm theo period
     * 
     * @param start Thời điểm bắt đầu
     * @param end Thời điểm kết thúc
     * @param groupBy Cách nhóm: "day", "month", "year"
     * @param type Loại doanh thu: "all", "subscription", "token"
     * @return AdminRevenueStatsDTO chứa thống kê chi tiết
     */
    AdminRevenueStatsDTO getRevenueStats(LocalDateTime start, LocalDateTime end, 
                                         String groupBy, String type);
    
    /**
     * Lấy toàn bộ dashboard data trong một request
     * 
     * @param start Thời điểm bắt đầu
     * @param end Thời điểm kết thúc
     * @param groupBy Cách nhóm: "day", "month", "year"
     * @return AdminRevenueDashboardDTO chứa tất cả dữ liệu dashboard
     */
    AdminRevenueDashboardDTO getRevenueDashboard(LocalDateTime start, LocalDateTime end, String groupBy);
    
    // ==================== TOP ITEMS ====================
    
    /**
     * Lấy top subscription plans bán chạy nhất
     * @param limit Số lượng kết quả tối đa
     * @return List các top subscription
     */
    List<AdminRevenueDashboardDTO.TopSubscriptionDTO> getTopSubscriptions(int limit);
    
    /**
     * Lấy top token/credit packages bán chạy nhất
     * @param limit Số lượng kết quả tối đa
     * @return List các top token packages
     */
    List<AdminRevenueDashboardDTO.TopTokenPackageDTO> getTopTokenPackages(int limit);
    
    /**
     * Lấy top khóa học bán chạy nhất
     * @param limit Số lượng kết quả tối đa
     * @return List các top courses
     */
    List<AdminRevenueDashboardDTO.TopCourseDTO> getTopCourses(int limit);
    
    // ==================== SO SÁNH ====================
    
    /**
     * So sánh doanh thu với kỳ trước (để tính % tăng/giảm)
     * 
     * @param currentStart Bắt đầu kỳ hiện tại
     * @param currentEnd Kết thúc kỳ hiện tại
     * @return % thay đổi so với kỳ trước
     */
    Double getRevenueGrowthPercentage(LocalDateTime currentStart, LocalDateTime currentEnd);
}

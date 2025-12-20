package com.sketchnotes.identityservice.dtos.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO tổng hợp cho Admin Revenue Dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRevenueDashboardDTO {
    
    /**
     * Tổng quan về Admin Wallet
     */
    private AdminWalletOverviewDTO walletOverview;
    
    /**
     * Thống kê doanh thu chi tiết
     */
    private AdminRevenueStatsDTO revenueStats;
    
    /**
     * Top gói subscription bán chạy
     */
    private List<TopSubscriptionDTO> topSubscriptions;
    
    /**
     * Top gói token/credit bán chạy
     */
    private List<TopTokenPackageDTO> topTokenPackages;
    
    /**
     * Top khóa học bán chạy
     */
    private List<TopCourseDTO> topCourses;
    
    /**
     * DTO cho top subscription plan
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopSubscriptionDTO {
        private Long planId;
        private String planName;
        private Long purchaseCount;
        private BigDecimal totalRevenue;
    }
    
    /**
     * DTO cho top token/credit package
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopTokenPackageDTO {
        private Long packageId;
        private String packageName;
        private Long purchaseCount;
        private BigDecimal totalRevenue;
    }
    
    /**
     * DTO cho top khóa học bán chạy
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCourseDTO {
        private Long courseId;
        private String courseName;
        private Long purchaseCount;
        private BigDecimal totalRevenue;
    }
}

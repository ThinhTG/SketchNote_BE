package com.sketchnotes.identityservice.dtos.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho thống kê doanh thu Admin Dashboard
 * Logic: Revenue = tiền Admin nhận được từ việc bán Subscription + Token/AI Credits
 * KHÔNG bao gồm: Deposit, Withdraw (đây là tiền của user, không phải doanh thu)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRevenueStatsDTO {
    
    // ==================== TỔNG QUAN ====================
    
    /**
     * Tổng doanh thu từ tất cả nguồn (Subscription + Token)
     */
    private BigDecimal totalRevenue;
    
    /**
     * Tổng doanh thu từ bán Subscription
     */
    private BigDecimal totalSubscriptionRevenue;
    
    /**
     * Tổng doanh thu từ bán Token/AI Credits
     */
    private BigDecimal totalTokenRevenue;
    
    /**
     * Số lượng giao dịch subscription thành công
     */
    private Long subscriptionTransactionCount;
    
    /**
     * Số lượng giao dịch mua token thành công
     */
    private Long tokenTransactionCount;
    
    // ==================== DỮ LIỆU THEO THỜI GIAN ====================
    
    /**
     * Doanh thu subscription theo thời gian (day/month/year)
     */
    private List<RevenueDataPoint> subscriptionRevenueTimeSeries;
    
    /**
     * Doanh thu token theo thời gian (day/month/year)
     */
    private List<RevenueDataPoint> tokenRevenueTimeSeries;
    
    /**
     * Tổng doanh thu theo thời gian (day/month/year)
     */
    private List<RevenueDataPoint> totalRevenueTimeSeries;
    
    /**
     * DTO cho một điểm dữ liệu doanh thu theo thời gian
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueDataPoint {
        /**
         * Kỳ thời gian (e.g., "2024-01-15", "2024-01", "2024")
         */
        private String period;
        
        /**
         * Số tiền doanh thu trong kỳ
         */
        private BigDecimal amount;
        
        /**
         * Số lượng giao dịch trong kỳ
         */
        private Long transactionCount;
    }
}

package com.sketchnotes.identityservice.service.implement;

import com.sketchnotes.identityservice.dtos.response.admin.AdminRevenueDashboardDTO;
import com.sketchnotes.identityservice.dtos.response.admin.AdminRevenueStatsDTO;
import com.sketchnotes.identityservice.dtos.response.admin.AdminWalletOverviewDTO;
import com.sketchnotes.identityservice.repository.AdminRevenueRepository;
import com.sketchnotes.identityservice.repository.IUserSubscriptionRepository;
import com.sketchnotes.identityservice.repository.IWalletRepository;
import com.sketchnotes.identityservice.service.interfaces.IAdminRevenueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service implementation cho Admin Revenue Dashboard
 * 
 * Logic nghiệp vụ:
 * - Revenue = Tiền Admin nhận được từ bán dịch vụ
 * - Bao gồm: Subscription + Token/AI Credits purchases  
 * - KHÔNG bao gồm: Deposit, Withdraw (đây là tiền của user, không tính là revenue)
 * 
 * Giải thích:
 * - Khi user Deposit: Tiền vào ví user, Admin chỉ giữ hộ -> không tính revenue
 * - Khi user Withdraw: User lấy lại tiền của họ -> không ảnh hưởng revenue
 * - Khi user mua Subscription/Token: Tiền này chuyển thành dịch vụ -> tính revenue
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminRevenueServiceImpl implements IAdminRevenueService {
    
    private final AdminRevenueRepository adminRevenueRepository;
    private final IWalletRepository walletRepository;
    private final IUserSubscriptionRepository userSubscriptionRepository;
    
    // ==================== TỔNG QUAN WALLET ====================
    
    @Override
    public AdminWalletOverviewDTO getWalletOverview() {
        log.info("Getting Admin Wallet Overview");
        
        // Tổng doanh thu từ tất cả thời gian (Subscription + Token)
        BigDecimal totalRevenue = adminRevenueRepository.getAllTimeRevenue();
        BigDecimal subscriptionRevenue = adminRevenueRepository.getAllTimeSubscriptionRevenue();
        BigDecimal tokenRevenue = adminRevenueRepository.getAllTimeTokenRevenue();
        
        // Tổng tiền user đã deposit/withdraw (để tham khảo)
        BigDecimal totalDeposits = adminRevenueRepository.getTotalUserDeposits();
        BigDecimal totalWithdrawals = adminRevenueRepository.getTotalUserWithdrawals();
        
        // Tổng số dư trong tất cả ví user (nghĩa vụ phải trả)
        BigDecimal totalUserWalletBalance = walletRepository.findAll().stream()
                .map(wallet -> wallet.getBalance() != null ? wallet.getBalance() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return AdminWalletOverviewDTO.builder()
                .totalBalance(totalRevenue)
                .subscriptionBalance(subscriptionRevenue)
                .tokenBalance(tokenRevenue)
                .totalUserDeposits(totalDeposits)
                .totalUserWithdrawals(totalWithdrawals)
                .totalUserWalletBalance(totalUserWalletBalance)
                .build();
    }
    
    // ==================== THỐNG KÊ DOANH THU ====================
    
    @Override
    public AdminRevenueStatsDTO getRevenueStats(LocalDateTime start, LocalDateTime end, 
                                                 String groupBy, String type) {
        log.info("Getting revenue stats from {} to {}, groupBy={}, type={}", start, end, groupBy, type);
        
        // Normalize parameters
        String normalizedGroupBy = normalizeGroupBy(groupBy);
        String normalizedType = normalizeType(type);
        
        // Lấy tổng doanh thu
        BigDecimal totalSubscriptionRevenue = BigDecimal.ZERO;
        BigDecimal totalTokenRevenue = BigDecimal.ZERO;
        Long subscriptionTxCount = 0L;
        Long tokenTxCount = 0L;
        
        List<AdminRevenueStatsDTO.RevenueDataPoint> subscriptionTimeSeries = new ArrayList<>();
        List<AdminRevenueStatsDTO.RevenueDataPoint> tokenTimeSeries = new ArrayList<>();
        List<AdminRevenueStatsDTO.RevenueDataPoint> totalTimeSeries = new ArrayList<>();
        
        // Fetch data based on type
        boolean fetchSubscription = "all".equals(normalizedType) || "subscription".equals(normalizedType);
        boolean fetchToken = "all".equals(normalizedType) || "token".equals(normalizedType);
        
        if (fetchSubscription) {
            totalSubscriptionRevenue = adminRevenueRepository.getTotalSubscriptionRevenue(start, end);
            subscriptionTxCount = adminRevenueRepository.countSubscriptionTransactions(start, end);
            subscriptionTimeSeries = getTimeSeriesData(start, end, normalizedGroupBy, "subscription");
        }
        
        if (fetchToken) {
            totalTokenRevenue = adminRevenueRepository.getTotalTokenRevenue(start, end);
            tokenTxCount = adminRevenueRepository.countTokenTransactions(start, end);
            tokenTimeSeries = getTimeSeriesData(start, end, normalizedGroupBy, "token");
        }
        
        // Calculate total time series
        if ("all".equals(normalizedType)) {
            totalTimeSeries = getTimeSeriesData(start, end, normalizedGroupBy, "total");
        } else if (fetchSubscription) {
            totalTimeSeries = subscriptionTimeSeries;
        } else {
            totalTimeSeries = tokenTimeSeries;
        }
        
        BigDecimal totalRevenue = totalSubscriptionRevenue.add(totalTokenRevenue);
        
        return AdminRevenueStatsDTO.builder()
                .totalRevenue(totalRevenue)
                .totalSubscriptionRevenue(totalSubscriptionRevenue)
                .totalTokenRevenue(totalTokenRevenue)
                .subscriptionTransactionCount(subscriptionTxCount)
                .tokenTransactionCount(tokenTxCount)
                .subscriptionRevenueTimeSeries(subscriptionTimeSeries)
                .tokenRevenueTimeSeries(tokenTimeSeries)
                .totalRevenueTimeSeries(totalTimeSeries)
                .build();
    }
    
    @Override
    public AdminRevenueDashboardDTO getRevenueDashboard(LocalDateTime start, LocalDateTime end, String groupBy) {
        log.info("Getting full revenue dashboard from {} to {}", start, end);
        
        AdminWalletOverviewDTO walletOverview = getWalletOverview();
        AdminRevenueStatsDTO revenueStats = getRevenueStats(start, end, groupBy, "all");
        List<AdminRevenueDashboardDTO.TopSubscriptionDTO> topSubscriptions = getTopSubscriptions(5);
        List<AdminRevenueDashboardDTO.TopTokenPackageDTO> topTokenPackages = getTopTokenPackages(5);
        
        return AdminRevenueDashboardDTO.builder()
                .walletOverview(walletOverview)
                .revenueStats(revenueStats)
                .topSubscriptions(topSubscriptions)
                .topTokenPackages(topTokenPackages)
                .build();
    }
    
    // ==================== TOP ITEMS ====================
    
    @Override
    public List<AdminRevenueDashboardDTO.TopSubscriptionDTO> getTopSubscriptions(int limit) {
        log.info("Getting top {} subscriptions", limit);
        
        // Query từ UserSubscription để group by plan
        List<Object[]> results = userSubscriptionRepository.findAll().stream()
                .filter(sub -> sub.getTransactionId() != null) // Có transaction = đã thanh toán
                .collect(Collectors.groupingBy(
                        sub -> sub.getSubscriptionPlan().getPlanId(),
                        Collectors.toList()
                ))
                .entrySet().stream()
                .map(entry -> {
                    var subs = entry.getValue();
                    var plan = subs.get(0).getSubscriptionPlan();
                    long count = subs.size();
                    BigDecimal revenue = plan.getPrice().multiply(BigDecimal.valueOf(count));
                    return new Object[]{plan.getPlanId(), plan.getPlanName(), count, revenue};
                })
                .sorted((a, b) -> ((BigDecimal) b[3]).compareTo((BigDecimal) a[3]))
                .limit(limit)
                .collect(Collectors.toList());
        
        return results.stream()
                .map(row -> AdminRevenueDashboardDTO.TopSubscriptionDTO.builder()
                        .planId((Long) row[0])
                        .planName((String) row[1])
                        .purchaseCount((Long) row[2])
                        .totalRevenue((BigDecimal) row[3])
                        .build())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<AdminRevenueDashboardDTO.TopTokenPackageDTO> getTopTokenPackages(int limit) {
        log.info("Getting top {} token packages", limit);
        
        // Tạm thời return empty list - có thể implement khi có CreditPurchase entity
        // Hoặc query từ Transaction với description chứa package info
        return new ArrayList<>();
    }
    
    // ==================== SO SÁNH ====================
    
    @Override
    public Double getRevenueGrowthPercentage(LocalDateTime currentStart, LocalDateTime currentEnd) {
        log.info("Calculating revenue growth percentage");
        
        // Tính khoảng thời gian
        Duration duration = Duration.between(currentStart, currentEnd);
        LocalDateTime previousStart = currentStart.minus(duration);
        LocalDateTime previousEnd = currentStart;
        
        BigDecimal currentRevenue = adminRevenueRepository.getTotalRevenue(currentStart, currentEnd);
        BigDecimal previousRevenue = adminRevenueRepository.getTotalRevenue(previousStart, previousEnd);
        
        if (previousRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return currentRevenue.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        
        BigDecimal growth = currentRevenue.subtract(previousRevenue)
                .divide(previousRevenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        return growth.doubleValue();
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    private String normalizeGroupBy(String groupBy) {
        if (groupBy == null || groupBy.trim().isEmpty()) {
            return "day";
        }
        String normalized = groupBy.trim().toLowerCase();
        if (!normalized.equals("day") && !normalized.equals("month") && !normalized.equals("year")) {
            return "day";
        }
        return normalized;
    }
    
    private String normalizeType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return "all";
        }
        String normalized = type.trim().toLowerCase();
        if (!normalized.equals("all") && !normalized.equals("subscription") && !normalized.equals("token")) {
            return "all";
        }
        return normalized;
    }
    
    private List<AdminRevenueStatsDTO.RevenueDataPoint> getTimeSeriesData(
            LocalDateTime start, LocalDateTime end, String groupBy, String dataType) {
        
        List<Object[]> rawData;
        
        switch (groupBy) {
            case "month":
                rawData = getMonthlyData(start, end, dataType);
                break;
            case "year":
                rawData = getYearlyData(start, end, dataType);
                break;
            default: // "day"
                rawData = getDailyData(start, end, dataType);
                break;
        }
        
        return rawData.stream()
                .map(row -> AdminRevenueStatsDTO.RevenueDataPoint.builder()
                        .period(row[0] != null ? row[0].toString() : null)
                        .amount(row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO)
                        .transactionCount(row[2] != null ? ((Number) row[2]).longValue() : 0L)
                        .build())
                .collect(Collectors.toList());
    }
    
    private List<Object[]> getDailyData(LocalDateTime start, LocalDateTime end, String dataType) {
        switch (dataType) {
            case "subscription":
                return adminRevenueRepository.getSubscriptionRevenueByDay(start, end);
            case "token":
                return adminRevenueRepository.getTokenRevenueByDay(start, end);
            default:
                return adminRevenueRepository.getTotalRevenueByDay(start, end);
        }
    }
    
    private List<Object[]> getMonthlyData(LocalDateTime start, LocalDateTime end, String dataType) {
        switch (dataType) {
            case "subscription":
                return adminRevenueRepository.getSubscriptionRevenueByMonth(start, end);
            case "token":
                return adminRevenueRepository.getTokenRevenueByMonth(start, end);
            default:
                return adminRevenueRepository.getTotalRevenueByMonth(start, end);
        }
    }
    
    private List<Object[]> getYearlyData(LocalDateTime start, LocalDateTime end, String dataType) {
        switch (dataType) {
            case "subscription":
                return adminRevenueRepository.getSubscriptionRevenueByYear(start, end);
            case "token":
                return adminRevenueRepository.getTokenRevenueByYear(start, end);
            default:
                return adminRevenueRepository.getTotalRevenueByYear(start, end);
        }
    }
}

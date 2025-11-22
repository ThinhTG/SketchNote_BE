package com.sketchnotes.order_service.service.implement;

import com.sketchnotes.order_service.client.IdentityClient;
import com.sketchnotes.order_service.client.LearningClient;
import com.sketchnotes.order_service.dtos.admin.AdminDashboardResponseDTO;
import com.sketchnotes.order_service.repository.AdminDashboardRepository;
import com.sketchnotes.order_service.service.AdminDashboardService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final IdentityClient identityClient;
    private final AdminDashboardRepository adminDashboardRepository;
    
    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.10"); // 10%

    private final LearningClient learningClient;

    public AdminDashboardServiceImpl(IdentityClient identityClient, AdminDashboardRepository adminDashboardRepository, LearningClient learningClient) {
        this.identityClient = identityClient;
        this.adminDashboardRepository = adminDashboardRepository;
        this.learningClient = learningClient;
    }

    @Override
    public AdminDashboardResponseDTO.UserStatsDTO getUserStats() {
        Map<String, Long> userStatsMap = identityClient.getUserStats();
        return AdminDashboardResponseDTO.UserStatsDTO.builder()
                .totalUsers(userStatsMap.getOrDefault("totalUsers", 0L))
                .customers(userStatsMap.getOrDefault("customers", 0L))
                .designers(userStatsMap.getOrDefault("designers", 0L))
                .build();
    }

    @Override
    public AdminDashboardResponseDTO.RevenueStatsDTO getRevenueStats(LocalDateTime start, LocalDateTime end, String groupBy, String type) {
        List<AdminDashboardResponseDTO.RevenueDataPoint> courseRevenue = new ArrayList<>();
        BigDecimal totalCourseRevenue = BigDecimal.ZERO;

        List<AdminDashboardResponseDTO.RevenueDataPoint> subscriptionRevenue = new ArrayList<>();
        BigDecimal totalSubscriptionRevenue = BigDecimal.ZERO;

        List<AdminDashboardResponseDTO.RevenueDataPoint> resourceCommissionRevenue = new ArrayList<>();
        BigDecimal totalResourceCommissionRevenue = BigDecimal.ZERO;

        boolean fetchAll = type == null || "all".equalsIgnoreCase(type);

        // 1. Course Revenue
        if (fetchAll || "course".equalsIgnoreCase(type)) {
            List<Map<String, Object>> courseRevenueRaw = identityClient.getCourseRevenue(start.toString(), end.toString(), groupBy);
            for (Map<String, Object> item : courseRevenueRaw) {
                String period = (String) item.get("period");
                BigDecimal amount = new BigDecimal(item.get("revenue").toString());
                courseRevenue.add(new AdminDashboardResponseDTO.RevenueDataPoint(period, amount));
                totalCourseRevenue = totalCourseRevenue.add(amount);
            }
        }

        // 2. Subscription Revenue
        if (fetchAll || "subscription".equalsIgnoreCase(type)) {
            List<Object[]> subRevenueRaw;
            if ("month".equalsIgnoreCase(groupBy)) {
                subRevenueRaw = adminDashboardRepository.subscriptionRevenueByMonth(start, end);
            } else if ("year".equalsIgnoreCase(groupBy)) {
                subRevenueRaw = adminDashboardRepository.subscriptionRevenueByYear(start, end);
            } else {
                subRevenueRaw = adminDashboardRepository.subscriptionRevenueByDay(start, end);
            }

            for (Object[] row : subRevenueRaw) {
                String period = row[0] == null ? null : row[0].toString();
                BigDecimal amount = row[1] == null ? BigDecimal.ZERO : new BigDecimal(row[1].toString());
                subscriptionRevenue.add(new AdminDashboardResponseDTO.RevenueDataPoint(period, amount));
                totalSubscriptionRevenue = totalSubscriptionRevenue.add(amount);
            }
        }

        // 3. Resource Commission Revenue
        if (fetchAll || "commission".equalsIgnoreCase(type)) {
            List<Object[]> resourceRevenueRaw;
            if ("month".equalsIgnoreCase(groupBy)) {
                resourceRevenueRaw = adminDashboardRepository.resourceRevenueByMonth(start, end);
            } else if ("year".equalsIgnoreCase(groupBy)) {
                resourceRevenueRaw = adminDashboardRepository.resourceRevenueByYear(start, end);
            } else {
                resourceRevenueRaw = adminDashboardRepository.resourceRevenueByDay(start, end);
            }

            for (Object[] row : resourceRevenueRaw) {
                String period = row[0] == null ? null : row[0].toString();
                BigDecimal totalResourceSales = row[1] == null ? BigDecimal.ZERO : new BigDecimal(row[1].toString());
                BigDecimal commission = totalResourceSales.multiply(COMMISSION_RATE);

                resourceCommissionRevenue.add(new AdminDashboardResponseDTO.RevenueDataPoint(period, commission));
                totalResourceCommissionRevenue = totalResourceCommissionRevenue.add(commission);
            }
        }

        return AdminDashboardResponseDTO.RevenueStatsDTO.builder()
                .courseRevenue(courseRevenue)
                .subscriptionRevenue(subscriptionRevenue)
                .resourceCommissionRevenue(resourceCommissionRevenue)
                .totalCourseRevenue(totalCourseRevenue)
                .totalSubscriptionRevenue(totalSubscriptionRevenue)
                .totalResourceCommissionRevenue(totalResourceCommissionRevenue)
                .build();
    }

    @Override
    public AdminDashboardResponseDTO.OverviewStatsDTO getOverviewStats() {
        long totalOrders = adminDashboardRepository.countSuccessfulOrders();
        long totalEnrollments = 0;
        try {
            totalEnrollments = learningClient.getTotalEnrollments();
        } catch (Exception e) {
            // Log error or handle fallback
            e.printStackTrace();
        }
        return new AdminDashboardResponseDTO.OverviewStatsDTO(totalOrders, totalEnrollments);
    }

    @Override
    public List<AdminDashboardResponseDTO.TopItemDTO> getTopSellingCourses(int limit) {
        try {
            List<Map<String, Object>> topCourses = learningClient.getTopSellingCourses(limit);
            List<AdminDashboardResponseDTO.TopItemDTO> result = new ArrayList<>();
            for (Map<String, Object> item : topCourses) {
                Long id = ((Number) item.get("courseId")).longValue();
                long count = ((Number) item.get("enrollmentCount")).longValue();
                String name = (String) item.get("title");
                result.add(new AdminDashboardResponseDTO.TopItemDTO(id, name, count));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<AdminDashboardResponseDTO.TopItemDTO> getTopSellingResources(int limit) {
        List<Object[]> rows = adminDashboardRepository.findTopSellingResources(limit);
        List<AdminDashboardResponseDTO.TopItemDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long id = row[0] == null ? null : ((Number) row[0]).longValue();
            long count = row[1] == null ? 0 : ((Number) row[1]).longValue();
            String name = row[2] == null ? null : row[2].toString();
            result.add(new AdminDashboardResponseDTO.TopItemDTO(id, name, count));
        }
        return result;
    }

    @Override
    public List<AdminDashboardResponseDTO.TopDesignerDTO> getTopDesigners(int limit) {
        List<Object[]> rows = adminDashboardRepository.findTopDesignersByRevenue(limit);
        List<AdminDashboardResponseDTO.TopDesignerDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long id = row[0] == null ? null : ((Number) row[0]).longValue();
            BigDecimal revenue = row[1] == null ? BigDecimal.ZERO : new BigDecimal(row[1].toString());
            result.add(new AdminDashboardResponseDTO.TopDesignerDTO(id, revenue));
        }
        return result;
    }

    @Override
    public List<AdminDashboardResponseDTO.SubscriptionStatDTO> getSubscriptionStats() {
        List<Object[]> rows = adminDashboardRepository.countSubscriptionsByPlan();
        List<AdminDashboardResponseDTO.SubscriptionStatDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long id = row[0] == null ? null : ((Number) row[0]).longValue();
            long count = row[1] == null ? 0 : ((Number) row[1]).longValue();
            result.add(new AdminDashboardResponseDTO.SubscriptionStatDTO(id, count));
        }
        return result;
    }
}

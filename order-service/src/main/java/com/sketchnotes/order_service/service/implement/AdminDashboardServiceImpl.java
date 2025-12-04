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

/**
 * Service implementation cho Admin Dashboard - Order Service
 * 
 * LƯU Ý: Logic thống kê REVENUE đã được chuyển sang identity-service.
 * Method getRevenueStats() đã deprecated, sử dụng AdminRevenueServiceImpl trong identity-service.
 */
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

    /**
     * @deprecated Logic revenue đã chuyển sang identity-service.
     * Sử dụng GET /api/admin/revenue/stats trong identity-service.
     * 
     * Logic mới:
     * - Revenue = Subscription + Token/AI Credits
     * - KHÔNG bao gồm Deposit/Withdraw (đây là tiền của user)
     */
    @Override
    @Deprecated
    public AdminDashboardResponseDTO.RevenueStatsDTO getRevenueStats(LocalDateTime start, LocalDateTime end, String groupBy, String type) {
        // Return empty stats - logic đã chuyển sang identity-service
        return AdminDashboardResponseDTO.RevenueStatsDTO.builder()
                .courseRevenue(new ArrayList<>())
                .subscriptionRevenue(new ArrayList<>())
                .resourceCommissionRevenue(new ArrayList<>())
                .totalCourseRevenue(BigDecimal.ZERO)
                .totalSubscriptionRevenue(BigDecimal.ZERO)
                .totalResourceCommissionRevenue(BigDecimal.ZERO)
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

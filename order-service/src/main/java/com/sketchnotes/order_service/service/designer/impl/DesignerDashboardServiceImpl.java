package com.sketchnotes.order_service.service.designer.impl;

import com.sketchnotes.order_service.dtos.designer.DesignerDashboardSummaryDTO;
import com.sketchnotes.order_service.dtos.designer.RevenueChartResponseDTO;
import com.sketchnotes.order_service.dtos.designer.RevenueDataPointDTO;
import com.sketchnotes.order_service.dtos.designer.TimeSeriesPointDTO;
import com.sketchnotes.order_service.dtos.designer.TopTemplateDTO;
import com.sketchnotes.order_service.repository.DashboardRepository;
import com.sketchnotes.order_service.service.designer.DesignerDashboardService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class DesignerDashboardServiceImpl implements DesignerDashboardService {

    private final DashboardRepository dashboardRepository;

    public DesignerDashboardServiceImpl(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Override
    public DesignerDashboardSummaryDTO getSummary(Long designerId, LocalDateTime start, LocalDateTime end) {
        BigDecimal totalRevenue = dashboardRepository.totalRevenueForDesigner(designerId, start, end);
        // Compute total sold count by summing daily counts (safe fallback)
        List<Object[]> byDay = dashboardRepository.salesByDay(designerId, start, end);
        long totalSold = 0L;
        for (Object[] r : byDay) {
            if (r[1] instanceof Number) totalSold += ((Number) r[1]).longValue();
        }
        return new DesignerDashboardSummaryDTO(designerId, totalRevenue, totalSold);
    }

    @Override
    public List<TopTemplateDTO> getTopTemplates(Long designerId, LocalDateTime start, LocalDateTime end, int limit) {
        List<Object[]> rows = dashboardRepository.findTopTemplates(designerId, start, end, limit);
        List<TopTemplateDTO> result = new ArrayList<>();
        for (Object[] r : rows) {
            Long templateId = r[0] == null ? null : ((Number) r[0]).longValue();
            String name = r[1] == null ? null : r[1].toString();
            Long soldCount = r[2] == null ? 0L : ((Number) r[2]).longValue();
            BigDecimal revenue = r[3] == null ? BigDecimal.ZERO : new BigDecimal(r[3].toString());
            result.add(new TopTemplateDTO(templateId, name, soldCount, revenue));
        }
        return result;
    }

    @Override
    public RevenueChartResponseDTO getSalesTimeSeries(Long designerId, LocalDateTime start, LocalDateTime end, String groupBy) {
        List<Object[]> rows;
        String gb = (groupBy == null) ? "day" : groupBy.toLowerCase();
        String type;
        
        switch (gb) {
            case "month":
                rows = dashboardRepository.salesByMonth(designerId, start, end);
                type = "monthly";
                break;
            case "year":
                rows = dashboardRepository.salesByYear(designerId, start, end);
                type = "yearly";
                break;
            default:
                rows = dashboardRepository.salesByDay(designerId, start, end);
                type = "daily";
        }

        List<RevenueDataPointDTO> dataPoints = new ArrayList<>();
        for (Object[] r : rows) {
            String period = r[0] == null ? null : r[0].toString();
            BigDecimal revenue = r[2] == null ? BigDecimal.ZERO : new BigDecimal(r[2].toString());
            
            RevenueDataPointDTO point = new RevenueDataPointDTO();
            point.setRevenue(revenue);
            
            // Set appropriate field based on type
            switch (type) {
                case "daily":
                    point.setDate(period);
                    break;
                case "monthly":
                    point.setMonth(period);
                    break;
                case "yearly":
                    point.setYear(period == null ? null : Integer.parseInt(period));
                    break;
            }
            
            dataPoints.add(point);
        }
        
        return new RevenueChartResponseDTO(type, dataPoints);
    }
}

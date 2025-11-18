package com.sketchnotes.order_service.service.designer;

import com.sketchnotes.order_service.dtos.designer.DesignerDashboardSummaryDTO;
import com.sketchnotes.order_service.dtos.designer.TimeSeriesPointDTO;
import com.sketchnotes.order_service.dtos.designer.TopTemplateDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface DesignerDashboardService {
    DesignerDashboardSummaryDTO getSummary(Long designerId, LocalDateTime start, LocalDateTime end);

    List<TopTemplateDTO> getTopTemplates(Long designerId, LocalDateTime start, LocalDateTime end, int limit);

    List<TimeSeriesPointDTO> getSalesTimeSeries(Long designerId, LocalDateTime start, LocalDateTime end, String groupBy);
}

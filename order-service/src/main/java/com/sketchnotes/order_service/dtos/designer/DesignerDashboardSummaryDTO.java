package com.sketchnotes.order_service.dtos.designer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DesignerDashboardSummaryDTO {
    private Long designerId;
    private BigDecimal totalRevenue;
    private Long totalSoldCount;
    private Long totalProductCount; // Tổng số lượng product (resource template) mà designer đăng bán
}

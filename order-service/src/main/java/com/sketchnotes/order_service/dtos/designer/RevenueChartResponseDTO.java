package com.sketchnotes.order_service.dtos.designer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueChartResponseDTO {
    private String type;  // "daily", "monthly", or "yearly"
    private List<RevenueDataPointDTO> data;
}

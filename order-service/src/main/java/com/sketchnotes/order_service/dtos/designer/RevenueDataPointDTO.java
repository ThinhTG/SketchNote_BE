package com.sketchnotes.order_service.dtos.designer;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RevenueDataPointDTO {
    private String date;     // For daily: "2025-01-01"
    private String month;    // For monthly: "2025-01"
    private Integer year;    // For yearly: 2025
    private BigDecimal revenue;
}

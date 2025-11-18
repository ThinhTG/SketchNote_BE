package com.sketchnotes.order_service.dtos.designer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSeriesPointDTO {
    private String period; // e.g. 2025-11-18 or 2025-11 or 2025
    private Long soldCount;
    private BigDecimal revenue;
}

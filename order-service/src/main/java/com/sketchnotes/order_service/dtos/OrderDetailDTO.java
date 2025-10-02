package com.sketchnotes.order_service.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderDetailDTO {
    private Long resourceId;
    private Integer quantity;
    private BigDecimal price;
}
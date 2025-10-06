package com.sketchnotes.order_service.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailDTO {
    private Long orderDetailId;
    private Long orderId;
    private Long resourceTemplateId;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal subtotalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Template information for response
    private String templateName;
    private String templateDescription;
    private String templateType;
}
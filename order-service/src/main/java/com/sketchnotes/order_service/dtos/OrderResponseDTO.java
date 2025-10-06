package com.sketchnotes.order_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponseDTO {
    private Long orderId;
    private Long userId;
    private Long resourceTemplateId;
    private Long subscriptionId;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String orderStatus;
    private String invoiceNumber;
    private LocalDateTime issueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderDetailDTO> items;
}
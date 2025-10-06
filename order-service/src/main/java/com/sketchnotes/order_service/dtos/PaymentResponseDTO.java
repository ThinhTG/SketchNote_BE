package com.sketchnotes.order_service.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {
    private String paymentId;
    private String orderCode;
    private BigDecimal amount;
    private String description;
    private String paymentUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
}

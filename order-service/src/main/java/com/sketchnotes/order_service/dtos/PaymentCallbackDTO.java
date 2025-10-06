package com.sketchnotes.order_service.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCallbackDTO {
    private Long orderId;
    private String orderCode;
    private String paymentId;
    private BigDecimal amount;
    private String status; // PAID, FAILED, CANCELLED
    private String description;
    private LocalDateTime paymentTime;
    private String transactionId;
}

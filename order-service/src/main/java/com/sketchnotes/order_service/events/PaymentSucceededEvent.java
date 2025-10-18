package com.sketchnotes.order_service.events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSucceededEvent {
    private Long orderId;
    private String transactionId;
    private String paymentMethod;
}
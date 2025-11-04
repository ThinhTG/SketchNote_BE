package com.sketchnotes.identityservice.events;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFailedEvent {
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String reason;
}
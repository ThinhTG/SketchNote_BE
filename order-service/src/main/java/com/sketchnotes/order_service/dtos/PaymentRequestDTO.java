package com.sketchnotes.order_service.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDTO {
    private Long orderId;
    private BigDecimal amount;
    private String description;
    private String returnUrl;
    private String cancelUrl;
    private List<PaymentItemDTO> items;
    
    @Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
    public static class PaymentItemDTO {
        private String name;
        private Integer quantity;
        private BigDecimal price;
    }
}

package com.sketchnotes.order_service.dtos;

import lombok.*;
import net.minidev.json.annotate.JsonIgnore;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDTO {
    @JsonIgnore
    private  Long userId;
    private Long subscriptionId; // Optional for subscription orders
    private List<OrderDetailRequestDTO> items;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderDetailRequestDTO {
        private Long resourceTemplateId;
        private BigDecimal discount; // Optional discount
    }
}

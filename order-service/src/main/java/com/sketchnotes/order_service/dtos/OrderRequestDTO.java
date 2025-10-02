package com.sketchnotes.order_service.dtos;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDTO {
    private Long userId;
    private List<OrderDetailDTO> items;
}

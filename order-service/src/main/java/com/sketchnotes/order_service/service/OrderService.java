package com.sketchnotes.order_service.service;

import com.sketchnotes.order_service.dtos.OrderRequestDTO;
import com.sketchnotes.order_service.dtos.OrderResponseDTO;

import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(OrderRequestDTO request);
    OrderResponseDTO getOrderById(Long id);
    List<OrderResponseDTO> getAllOrdersByUser(Long userId);
    OrderResponseDTO updateOrderStatus(Long id, String status);
}

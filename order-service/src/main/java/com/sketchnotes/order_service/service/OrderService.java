package com.sketchnotes.order_service.service;

import com.sketchnotes.order_service.dtos.*;

import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(OrderRequestDTO request);
    OrderResponseDTO getOrderById(Long id);
    List<OrderResponseDTO> getAllOrdersByUser(Long userId);
    OrderResponseDTO updateOrderStatus(Long id, String status);
    OrderResponseDTO updatePaymentStatus(Long id, String paymentStatus);
    
    // Template management
    List<ResourceTemplateDTO> getAllTemplates();
    List<ResourceTemplateDTO> getTemplatesByType(String type);
    List<ResourceTemplateDTO> searchTemplates(String keyword);
    ResourceTemplateDTO getTemplateById(Long id);
    List<ResourceTemplateDTO> getTemplatesByPriceRange(java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice);
}

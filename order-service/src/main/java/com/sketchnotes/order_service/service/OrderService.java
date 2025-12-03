package com.sketchnotes.order_service.service;

import com.sketchnotes.order_service.dtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    
    // ==================== ADMIN APIs ====================
    
    /**
     * Lấy tất cả orders với phân trang và filter (Admin)
     */
    Page<OrderResponseDTO> getAllOrders(String search, String orderStatus, String paymentStatus, Pageable pageable);
    
    /**
     * Lấy orders của một user cụ thể với phân trang (Admin)
     */
    Page<OrderResponseDTO> getOrdersByUserId(Long userId, Pageable pageable);
}

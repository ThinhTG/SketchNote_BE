package com.sketchnotes.order_service.service;

import com.sketchnotes.order_service.dtos.*;
import com.sketchnotes.order_service.utils.PagedResponse;

import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(OrderRequestDTO request);
    OrderResponseDTO getOrderById(Long id);
    List<OrderResponseDTO> getAllOrdersByUser(Long userId);
    OrderResponseDTO updateOrderStatus(Long id, String status);
    OrderResponseDTO updatePaymentStatus(Long id, String paymentStatus);
    
    /**
     * Hủy đơn hàng đang PENDING.
     * Chỉ user sở hữu order hoặc admin mới có thể hủy.
     * 
     * @param orderId ID của order
     * @param userId ID của user thực hiện hành động
     * @return OrderResponseDTO với trạng thái CANCELLED
     */
    OrderResponseDTO cancelOrder(Long orderId, Long userId);
    
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
    PagedResponse<OrderResponseDTO> getAllOrders(String search, String orderStatus, String paymentStatus, int pageNo, int pageSize);
    
    /**
     * Lấy orders của một user cụ thể với phân trang (Admin)
     */
    PagedResponse<OrderResponseDTO> getOrdersByUserId(Long userId, int pageNo, int pageSize);
}

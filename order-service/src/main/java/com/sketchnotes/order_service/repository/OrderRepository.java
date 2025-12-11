package com.sketchnotes.order_service.repository;

import com.sketchnotes.order_service.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Count the number of successful orders for a specific resource template
     * Only count orders with payment status PAID and order status SUCCESS
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.resourceTemplateId = :templateId AND o.paymentStatus = 'PAID' AND o.orderStatus = 'SUCCESS'")
    Long countSuccessfulOrdersByTemplateId(@Param("templateId") Long templateId);
    
    // ==================== ADMIN APIs ====================
    
    /**
     * Lấy tất cả orders của một user
     */
    List<Order> findByUserId(Long userId);
    
    /**
     * Lấy tất cả orders của một user với phân trang
     */
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Lấy tất cả orders theo status với phân trang
     */
    Page<Order> findByOrderStatus(String orderStatus, Pageable pageable);
    
    /**
     * Lấy tất cả orders theo payment status với phân trang
     */
    Page<Order> findByPaymentStatus(String paymentStatus, Pageable pageable);
    
    /**
     * Lấy tất cả orders theo cả order status và payment status
     */
    Page<Order> findByOrderStatusAndPaymentStatus(String orderStatus, String paymentStatus, Pageable pageable);
    
    /**
     * Tìm kiếm orders theo invoiceNumber
     */
    Page<Order> findByInvoiceNumberContainingIgnoreCase(String invoiceNumber, Pageable pageable);
}

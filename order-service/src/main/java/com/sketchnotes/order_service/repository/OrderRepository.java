package com.sketchnotes.order_service.repository;

import com.sketchnotes.order_service.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Count the number of successful orders for a specific resource template
     * Only count orders with payment status PAID and order status SUCCESS
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.resourceTemplateId = :templateId AND o.paymentStatus = 'PAID' AND o.orderStatus = 'SUCCESS'")
    Long countSuccessfulOrdersByTemplateId(@Param("templateId") Long templateId);
}

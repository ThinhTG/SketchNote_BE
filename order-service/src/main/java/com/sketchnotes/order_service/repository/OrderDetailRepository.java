package com.sketchnotes.order_service.repository;

import com.sketchnotes.order_service.dtos.OrderDetailDTO;
import com.sketchnotes.order_service.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    @Query("select od.resourceTemplateId from OrderDetail od where od.order.orderId = :orderId")
    java.util.List<Long> findTemplateIdsByOrderId(@Param("orderId") Long orderId);
}
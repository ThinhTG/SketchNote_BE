package com.sketchnotes.order_service.repository;

import com.sketchnotes.order_service.dtos.OrderDetailDTO;
import com.sketchnotes.order_service.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    @Query("select od.resourceTemplateId from OrderDetail od where od.order.orderId = :orderId")
    java.util.List<Long> findTemplateIdsByOrderId(@Param("orderId") Long orderId);

    @Query("""
            select case when count(od) > 0 then true else false end
            from OrderDetail od
            join od.order o
            where o.userId = :userId
            and od.resourceTemplateId = :templateId
            and o.orderStatus in :orderStatuses
            and o.paymentStatus in :paymentStatuses
            """)
    boolean existsByUserAndTemplateWithStatuses(@Param("userId") Long userId,
                                                 @Param("templateId") Long templateId,
                                                 @Param("orderStatuses") List<String> orderStatuses,
                                                 @Param("paymentStatuses") List<String> paymentStatuses);
}
package com.sketchnotes.order_service.repository;

import com.sketchnotes.order_service.dtos.OrderDetailDTO;
import com.sketchnotes.order_service.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {}
package com.sketchnotes.order_service.repository;

import com.sketchnotes.order_service.entity.OrderEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderEventLogRepository extends JpaRepository<OrderEventLog, Long> {
}
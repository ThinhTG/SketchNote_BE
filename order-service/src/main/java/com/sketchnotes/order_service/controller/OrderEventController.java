package com.sketchnotes.order_service.controller;

import com.sketchnotes.order_service.dtos.ApiResponse;
import com.sketchnotes.order_service.entity.OrderEventLog;
import com.sketchnotes.order_service.repository.OrderEventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class OrderEventController {

    private final OrderEventLogRepository orderEventLogRepository;

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<List<OrderEventLog>>> getEventsForOrder(@PathVariable Long orderId) {
        List<OrderEventLog> events = orderEventLogRepository.findAll().stream()
                .filter(e -> e.getOrderId() != null && e.getOrderId().equals(orderId))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(events, "events"));
    }
}

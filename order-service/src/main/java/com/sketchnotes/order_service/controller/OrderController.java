package com.sketchnotes.order_service.controller;

import com.sketchnotes.order_service.dtos.*;
import com.sketchnotes.order_service.service.OrderPaymentService;
import com.sketchnotes.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderPaymentService orderPaymentService;

    // Order management endpoints
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO dto) {
        return ResponseEntity.ok(orderService.createOrder(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getAllOrdersByUser(userId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @PutMapping("/{id}/payment-status")
    public ResponseEntity<OrderResponseDTO> updatePaymentStatus(@PathVariable Long id, @RequestParam String paymentStatus) {
        return ResponseEntity.ok(orderService.updatePaymentStatus(id, paymentStatus));
    }

    /**
     * Tạo payment link cho đơn hàng
     */
    @PostMapping("/{id}/payment")
    public ResponseEntity<PaymentResponseDTO> createPaymentForOrder(@PathVariable Long id) {
        PaymentResponseDTO paymentResponse = orderPaymentService.createPaymentForOrder(id);
        return ResponseEntity.ok(paymentResponse);
    }

    /**
     * Kiểm tra trạng thái payment của đơn hàng
     */
    @GetMapping("/{id}/payment-status")
    public ResponseEntity<PaymentResponseDTO> getPaymentStatus(@PathVariable Long id) {
        PaymentResponseDTO paymentStatus = orderPaymentService.getOrderPaymentStatus(id);
        return ResponseEntity.ok(paymentStatus);
    }

    /**
     * Hủy payment link của đơn hàng
     */
    @PostMapping("/{id}/cancel-payment")
    public ResponseEntity<Boolean> cancelPayment(@PathVariable Long id) {
        boolean result = orderPaymentService.cancelOrderPayment(id);
        return ResponseEntity.ok(result);
    }

    // Template management endpoints
    @GetMapping("/templates")
    public ResponseEntity<List<ResourceTemplateDTO>> getAllTemplates() {
        return ResponseEntity.ok(orderService.getAllTemplates());
    }

    @GetMapping("/templates/{id}")
    public ResponseEntity<ResourceTemplateDTO> getTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getTemplateById(id));
    }

    @GetMapping("/templates/type/{type}")
    public ResponseEntity<List<ResourceTemplateDTO>> getTemplatesByType(@PathVariable String type) {
        return ResponseEntity.ok(orderService.getTemplatesByType(type));
    }

    @GetMapping("/templates/search")
    public ResponseEntity<List<ResourceTemplateDTO>> searchTemplates(@RequestParam String keyword) {
        return ResponseEntity.ok(orderService.searchTemplates(keyword));
    }

    @GetMapping("/templates/price-range")
    public ResponseEntity<List<ResourceTemplateDTO>> getTemplatesByPriceRange(
            @RequestParam BigDecimal minPrice, 
            @RequestParam BigDecimal maxPrice) {
        return ResponseEntity.ok(orderService.getTemplatesByPriceRange(minPrice, maxPrice));
    }
}

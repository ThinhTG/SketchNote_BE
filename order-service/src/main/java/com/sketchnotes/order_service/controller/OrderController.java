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

    /**
     * Tạo đơn hàng mới
     */
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO dto) {
        return ResponseEntity.ok(orderService.createOrder(dto));
    }

    /**
     * Lấy thông tin đơn hàng theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    /**
     * Lấy danh sách đơn hàng của user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getAllOrdersByUser(userId));
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
}

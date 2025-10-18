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
    public ResponseEntity<ApiResponse<OrderResponseDTO>> createOrder(@RequestBody OrderRequestDTO dto) {
        var result = orderService.createOrder(dto);
        return ResponseEntity.ok(ApiResponse.success(result, "Order created"));
    }

    /**
     * Lấy thông tin đơn hàng theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getOrder(@PathVariable Long id) {
        var result = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(result, "Fetched order"));
    }

    /**
     * Lấy danh sách đơn hàng của user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getOrdersByUser(@PathVariable Long userId) {
        var result = orderService.getAllOrdersByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(result, "User orders"));
    }

    /**
     * Tạo payment link cho đơn hàng
     */
    @PostMapping("/{id}/payment")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> createPaymentForOrder(@PathVariable Long id) {
        PaymentResponseDTO paymentResponse = orderPaymentService.createPaymentForOrder(id);
        return ResponseEntity.ok(ApiResponse.success(paymentResponse, "Payment created"));
    }

    /**
     * Kiểm tra trạng thái payment của đơn hàng
     */
    @GetMapping("/{id}/payment-status")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> getPaymentStatus(@PathVariable Long id) {
        PaymentResponseDTO paymentStatus = orderPaymentService.getOrderPaymentStatus(id);
        return ResponseEntity.ok(ApiResponse.success(paymentStatus, "Payment status"));
    }

    /**
     * Hủy payment link của đơn hàng
     */
    @PostMapping("/{id}/cancel-payment")
    public ResponseEntity<ApiResponse<Boolean>> cancelPayment(@PathVariable Long id) {
        boolean result = orderPaymentService.cancelOrderPayment(id);
        return ResponseEntity.ok(ApiResponse.success(result, "Payment canceled"));
    }
}

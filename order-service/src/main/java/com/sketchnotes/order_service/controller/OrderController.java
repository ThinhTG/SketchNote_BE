package com.sketchnotes.order_service.controller;

import com.sketchnotes.order_service.client.IdentityClient;
import com.sketchnotes.order_service.dtos.*;
import com.sketchnotes.order_service.service.OrderPaymentService;
import com.sketchnotes.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderPaymentService orderPaymentService;
    private final IdentityClient identityClient;


    /**
     * Tạo đơn hàng mới
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDTO>> createOrder(@RequestBody OrderRequestDTO dto) {
        var apiResponse = identityClient.getCurrentUser();
        UserResponse user = apiResponse.getResult();
        dto.setUserId(user.getId());
        var result = orderService.createOrder(dto);
        return ResponseEntity.ok(ApiResponse.success(result, "Order created"));
    }

    /**
     * Lấy thông tin đơn hàng theo ID
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getOrder(@PathVariable Long id) {
        var result = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(result, "Fetched order"));
    }

    /**
     * Lấy danh sách đơn hàng của user
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getOrdersByUser() {
        var apiResponse = identityClient.getCurrentUser();
        UserResponse user = apiResponse.getResult();
        var result = orderService.getAllOrdersByUser(user.getId());
        return ResponseEntity.ok(ApiResponse.success(result, "User orders"));
    }

    /**
     * Cập nhật trạng thái đơn hàng
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        var result = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(result, "Order status updated"));
    }
    
    /**
     * Thanh toán lại cho đơn hàng có trạng thái PaymentFail
     */
    @PostMapping("/{id}/payment/retry")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> retryPaymentForFailedOrder(@PathVariable Long id) {
        var result = orderPaymentService.retryPaymentForFailedOrder(id);
        return ResponseEntity.ok(ApiResponse.success(result, "Payment retry created successfully"));
    }
}
    

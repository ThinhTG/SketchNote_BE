package com.sketchnotes.order_service.controller;

import com.sketchnotes.order_service.dtos.PaymentCallbackDTO;
import com.sketchnotes.order_service.dtos.ApiResponse;
import com.sketchnotes.order_service.service.OrderPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment-callback")
@RequiredArgsConstructor
@Slf4j
public class PaymentCallbackController {

    private final OrderPaymentService orderPaymentService;

    /**
     * Callback endpoint để nhận thông báo từ payment-service
     */
    @PostMapping("/status")
    public ResponseEntity<ApiResponse<String>> handlePaymentCallback(@RequestBody PaymentCallbackDTO callback) {
        try {
            log.info("Received payment callback for order {}: status={}", 
                    callback.getOrderId(), callback.getStatus());
            
            // Sử dụng OrderPaymentService để xử lý callback
            orderPaymentService.handlePaymentCallback(callback.getOrderId(), callback.getStatus());
            return ResponseEntity.ok(ApiResponse.success("OK", "Callback processed"));
            
        } catch (Exception e) {
            log.error("Error handling payment callback for order {}: {}", 
                    callback.getOrderId(), e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder().code(400).message("FAILED").result(null).build());
        }
    }

    /**
     * Health check endpoint cho payment-service
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Order Service is running", "OK"));
    }
}

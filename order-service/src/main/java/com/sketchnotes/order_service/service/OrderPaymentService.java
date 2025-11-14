package com.sketchnotes.order_service.service;

import com.sketchnotes.order_service.dtos.OrderResponseDTO;
import com.sketchnotes.order_service.dtos.PaymentRequestDTO;
import com.sketchnotes.order_service.dtos.PaymentResponseDTO;

public interface OrderPaymentService {
    
    /**
     * Tạo payment link cho order
     */
    PaymentResponseDTO createPaymentForOrder(Long orderId);
    
    /**
     * Kiểm tra trạng thái payment của order
     */
    PaymentResponseDTO getOrderPaymentStatus(Long orderId);
    
    /**
     * Hủy payment của order
     */
    boolean cancelOrderPayment(Long orderId);
    
    /**
     * Xử lý callback từ payment-service
     */
    void handlePaymentCallback(Long orderId, String paymentStatus);
    
    /**
     * Thanh toán lại cho order có trạng thái PaymentFail
     */
    PaymentResponseDTO retryPaymentForFailedOrder(Long orderId);
}

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
    
    /**
     * Kiểm tra và cập nhật trạng thái payment của order từ PayOS.
     * Dùng khi cần manual check hoặc refresh status.
     * 
     * @param orderId ID của order
     * @return OrderResponseDTO với trạng thái đã được cập nhật
     */
    OrderResponseDTO checkAndUpdatePaymentStatus(Long orderId);
}

package com.sketchnotes.order_service.service.implement;

import com.sketchnotes.order_service.client.PaymentClient;
import com.sketchnotes.order_service.dtos.OrderResponseDTO;
import com.sketchnotes.order_service.dtos.PaymentRequestDTO;
import com.sketchnotes.order_service.dtos.PaymentResponseDTO;
import com.sketchnotes.order_service.service.OrderPaymentService;
import com.sketchnotes.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPaymentServiceImpl implements OrderPaymentService {
    
    private final OrderService orderService;
    private final PaymentClient paymentClient;

    @Override
    public PaymentResponseDTO createPaymentForOrder(Long orderId) {
        OrderResponseDTO order = orderService.getOrderById(orderId);
        
        // Tạo payment request từ order
        PaymentRequestDTO paymentRequest = PaymentRequestDTO.builder()
                .orderId(orderId)
                .amount(order.getTotalAmount())
                .description("Payment for Order #" + order.getInvoiceNumber())
                .returnUrl("http://localhost:3000/payment/success")
                .cancelUrl("http://localhost:3000/payment/cancel")
                .items(order.getItems().stream()
                        .map(item -> PaymentRequestDTO.PaymentItemDTO.builder()
                                .name(item.getTemplateName())
                                .quantity(1)
                                .price(item.getUnitPrice())
                                .build())
                        .toList())
                .build();
        
        PaymentResponseDTO paymentResponse = paymentClient.createPaymentLink(paymentRequest);
        
        log.info("Payment link created for order {}: {}", orderId, paymentResponse.getPaymentUrl());
        
        return paymentResponse;
    }

    @Override
    public PaymentResponseDTO getOrderPaymentStatus(Long orderId) {
        OrderResponseDTO order = orderService.getOrderById(orderId);
        
        // Tạo orderCode từ invoiceNumber hoặc orderId
        String orderCode = generateOrderCode(order);
        
        return paymentClient.getPaymentStatus(orderCode);
    }

    @Override
    public boolean cancelOrderPayment(Long orderId) {
        OrderResponseDTO order = orderService.getOrderById(orderId);
        
        String orderCode = generateOrderCode(order);
        
        boolean result = paymentClient.cancelPaymentLink(orderCode);
        
        if (result) {
            log.info("Payment cancelled for order {}", orderId);
            // Cập nhật trạng thái order
            orderService.updateOrderStatus(orderId, "CANCELLED");
            orderService.updatePaymentStatus(orderId, "CANCELLED");
        }
        
        return result;
    }

    @Override
    public void handlePaymentCallback(Long orderId, String paymentStatus) {
        log.info("Handling payment callback for order {}: status={}", orderId, paymentStatus);
        
        // Cập nhật trạng thái payment của order
        orderService.updatePaymentStatus(orderId, paymentStatus);
        
        // Cập nhật trạng thái order dựa trên payment status
        switch (paymentStatus) {
            case "PAID":
                orderService.updateOrderStatus(orderId, "CONFIRMED");
                log.info("Order {} confirmed after successful payment", orderId);
                break;
            case "FAILED":
            case "CANCELLED":
                orderService.updateOrderStatus(orderId, "CANCELLED");
                log.info("Order {} cancelled due to payment failure", orderId);
                break;
            default:
                log.warn("Unknown payment status: {}", paymentStatus);
        }
    }
    
    private String generateOrderCode(OrderResponseDTO order) {
        // Sử dụng invoiceNumber làm orderCode, hoặc fallback về orderId
        if (order.getInvoiceNumber() != null) {
            return order.getInvoiceNumber().replace("INV-", "");
        }
        return String.valueOf(order.getOrderId());
    }
}

package com.sketchnotes.order_service.service.implement;

import com.sketchnotes.order_service.client.PaymentClient;
import com.sketchnotes.order_service.client.IdentityClient;
import com.sketchnotes.order_service.dtos.*;
import com.sketchnotes.order_service.entity.ResourceTemplate;
import com.sketchnotes.order_service.repository.ResourceTemplateRepository;
import com.sketchnotes.order_service.service.OrderPaymentService;
import com.sketchnotes.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPaymentServiceImpl implements OrderPaymentService {
    
    private final OrderService orderService;
    private final PaymentClient paymentClient;
    private final IdentityClient identityClient;
    private final ResourceTemplateRepository resourceTemplateRepository;

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
            // Cập nhật trạng thái order
            orderService.updateOrderStatus(orderId, "CANCELLED");
            orderService.updatePaymentStatus(orderId, "CANCELLED");
        }
        
        return result;
    }

    @Override
    public void handlePaymentCallback(Long orderId, String paymentStatus) {
        
        // Cập nhật trạng thái payment của order
        orderService.updatePaymentStatus(orderId, paymentStatus);
        
        // Cập nhật trạng thái order dựa trên payment status
        switch (paymentStatus) {
            case "PAID":
                orderService.updateOrderStatus(orderId, "SUCCESS");
                // Send notifications on successful payment
                sendPaymentSuccessNotifications(orderId);
                break;
            case "FAILED":
            case "CANCELLED":
                orderService.updateOrderStatus(orderId, "CANCELLED");
                break;
            default:
                log.error("Unknown payment status: {}", paymentStatus);
        }
    }
    
    @Override
    public PaymentResponseDTO retryPaymentForFailedOrder(Long orderId) {
        OrderResponseDTO order = orderService.getOrderById(orderId);
        
        // Kiểm tra xem payment status có phải là FAILED không
        if (order.getPaymentStatus() == null || !order.getPaymentStatus().equals("FAILED")) {
            throw new RuntimeException("Order payment status is not FAILED. Current status: " + 
                    (order.getPaymentStatus() != null ? order.getPaymentStatus() : "null"));
        }
        
        log.info("Retrying payment for failed order: {}", orderId);
        ApiResponse<?> payResult = identityClient.payOrderFromWallet(order.getUserId(), order.getTotalAmount(),
                "Retry payment for order #" + order.getInvoiceNumber());

        if (payResult == null || payResult.getCode() != 200) {
            String msg = payResult != null ? payResult.getMessage() : "Unknown error";
            log.warn("Wallet payment failed for order {}: {}", orderId, msg);
            throw new RuntimeException("Wallet payment failed: " + msg);
        }

        orderService.updatePaymentStatus(orderId, "PAID");
        orderService.updateOrderStatus(orderId, "SUCCESS");

        log.info("Wallet payment successful for order: {}", orderId);
        
        // Send notifications on successful payment
        sendPaymentSuccessNotifications(orderId);

        return PaymentResponseDTO.builder()
                .paymentId("WALLET-" + orderId)
                .orderCode(order.getInvoiceNumber() != null ? order.getInvoiceNumber() : String.valueOf(orderId))
                .amount(order.getTotalAmount())
                .description("Wallet payment for order retry")
                .status("PAID")
                .build();
    }
    
    private String generateOrderCode(OrderResponseDTO order) {
        // Sử dụng invoiceNumber làm orderCode, hoặc fallback về orderId
        if (order.getInvoiceNumber() != null) {
            return order.getInvoiceNumber().replace("INV-", "");
        }
        return String.valueOf(order.getOrderId());
    }
    
    /**
     * Send notifications to buyer and designers when payment is successful.
     */
    private void sendPaymentSuccessNotifications(Long orderId) {
        try {
            OrderResponseDTO order = orderService.getOrderById(orderId);
            
            // Notification to buyer
            String buyerMessage = buildBuyerNotificationMessage(order);
            CreateNotificationRequest buyerNotification = CreateNotificationRequest.builder()
                    .userId(order.getUserId())
                    .title("Payment Successful")
                    .message(buyerMessage)
                    .type("PURCHASE_CONFIRM")
                    .orderId(order.getOrderId())
                    .build();
            
            identityClient.createNotification(buyerNotification);
            log.info("Sent purchase confirmation notification to user {}", order.getUserId());
            
            // Notifications to designers (one per unique designer)
            order.getItems().stream()
                    .collect(Collectors.groupingBy(OrderDetailDTO::getResourceTemplateId))
                    .forEach((templateId, items) -> {
                        try {
                            ResourceTemplate template = resourceTemplateRepository
                                    .findById(templateId)
                                    .orElse(null);
                            
                            if (template != null) {
                                String designerMessage = String.format(
                                        "Your resource '%s' has been purchased. Order ID: %s",
                                        template.getName(),
                                        order.getInvoiceNumber()
                                );
                                
                                CreateNotificationRequest designerNotification = CreateNotificationRequest.builder()
                                        .userId(template.getDesignerId())
                                        .title("Your resource has been purchased")
                                        .message(designerMessage)
                                        .type("PURCHASE")
                                        .orderId(order.getOrderId())
                                        .resourceItemId(templateId)
                                        .build();
                                
                                identityClient.createNotification(designerNotification);
                                log.info("Sent purchase notification to designer {} for template {}", 
                                        template.getDesignerId(), templateId);
                            }
                        } catch (Exception e) {
                            log.error("Failed to send notification to designer for template {}: {}", 
                                    templateId, e.getMessage());
                        }
                    });
            
        } catch (Exception e) {
            log.error("Failed to send payment success notifications for order {}: {}", 
                    orderId, e.getMessage(), e);
            // Don't throw exception - notifications are not critical
        }
    }
    
    /**
     * Build a detailed message for the buyer notification.
     */
    private String buildBuyerNotificationMessage(OrderResponseDTO order) {
        String itemsList = order.getItems().stream()
                .map(item -> item.getTemplateName())
                .collect(Collectors.joining(", "));
        
        return String.format(
                "You have successfully purchased: %s. Order ID: %s. Total: %s VND",
                itemsList,
                order.getInvoiceNumber(),
                order.getTotalAmount()
        );
    }
}

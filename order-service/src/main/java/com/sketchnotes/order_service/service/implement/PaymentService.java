package com.sketchnotes.order_service.service.implement;

import com.sketchnotes.order_service.client.IdentityClient;
import com.sketchnotes.order_service.dtos.CreateNotificationRequest;
import com.sketchnotes.order_service.entity.Order;
import com.sketchnotes.order_service.entity.OrderDetail;
import com.sketchnotes.order_service.entity.ResourceTemplate;
import com.sketchnotes.order_service.events.PaymentFailedEvent;
import com.sketchnotes.order_service.events.PaymentSucceededEvent;
import com.sketchnotes.order_service.repository.OrderRepository;
import com.sketchnotes.order_service.repository.OrderDetailRepository;
import com.sketchnotes.order_service.repository.ResourceTemplateRepository;
import com.sketchnotes.order_service.service.UserResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserResourceService userResourceService;
    private final ResourceTemplateRepository resourceTemplateRepository;
    private final IdentityClient identityClient;

    @Transactional
    public void handlePaymentSuccess(PaymentSucceededEvent event) {
        orderRepository.findById(event.getOrderId()).ifPresentOrElse(order -> {
            boolean alreadyPaid = "PAID".equalsIgnoreCase(order.getPaymentStatus());

            try {
                if (!alreadyPaid) {
                    order.setPaymentStatus("PAID");
                    order.setOrderStatus("SUCCESS");
                    orderRepository.save(order);
                }

                // Thu thập tất cả resource template IDs cần tạo UserResource (truy vấn trực tiếp DB để chắc chắn)
                Set<Long> resourceTemplateIds = new HashSet<>();
                if (order.getResourceTemplateId() != null) {
                    resourceTemplateIds.add(order.getResourceTemplateId());
                }
                var detailIds = orderDetailRepository.findTemplateIdsByOrderId(order.getOrderId());
                if (detailIds != null) resourceTemplateIds.addAll(detailIds);

                
                // Tạo UserResource cho tất cả các resource templates (idempotent)
                if (!resourceTemplateIds.isEmpty()) {
                    int successCount = 0;
                    int skipCount = 0;
                    int errorCount = 0;
                    
                    for (Long resourceTemplateId : resourceTemplateIds) {
                        try {
                            userResourceService.createUserResource(
                                order.getOrderId(),
                                order.getUserId(),
                                resourceTemplateId
                            );
                            successCount++;
                        } catch (IllegalStateException e) {
                            skipCount++;
                        } catch (Exception e) {
                            log.error("Failed to create UserResource for Order {} and ResourceTemplate {}: {}", 
                                order.getOrderId(), resourceTemplateId, e.getMessage(), e);
                            errorCount++;
                        }
                    }
                }

                // Cộng tiền cho designer từ mỗi OrderDetail
                if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
                    for (OrderDetail detail : order.getOrderDetails()) {
                        try {
                            // Lấy ResourceTemplate để biết designerId
                            ResourceTemplate template = resourceTemplateRepository
                                    .findByTemplateIdAndStatus(detail.getResourceTemplateId(), ResourceTemplate.TemplateStatus.PUBLISHED)
                                    .orElse(null);
                            
                            if (template != null && template.getDesignerId() != null) {
                                Long designerId = template.getDesignerId();
                                BigDecimal amountToDeposit = detail.getSubtotalAmount();
                                
                                if (amountToDeposit != null && amountToDeposit.compareTo(BigDecimal.ZERO) > 0) {
                                    // Gọi identity service để cộng tiền cho designer
                                    String description = String.format("Payment from order %d for template %d", 
                                            order.getOrderId(), detail.getResourceTemplateId());
                                    identityClient.depositForDesigner(designerId, amountToDeposit, description);
                                    log.info("Deposited {} to designer {} for order {} template {}", 
                                            amountToDeposit, designerId, order.getOrderId(), detail.getResourceTemplateId());
                                }
                            } else {
                                log.warn("Template {} not found or has no designerId for order {}", 
                                        detail.getResourceTemplateId(), order.getOrderId());
                            }
                        } catch (Exception e) {
                            // Log error nhưng không throw để không ảnh hưởng đến các designer khác
                            log.error("Failed to deposit money to designer for OrderDetail {} in Order {}: {}", 
                                    detail.getOrderDetailId(), order.getOrderId(), e.getMessage(), e);
                        }
                    }
                }
                
                // 🔔 Send notifications to buyer and designers
                sendPaymentSuccessNotifications(order);
                
            } catch (Exception e) {
                log.error("Error processing payment success for Order {}: {}", order.getOrderId(), e.getMessage(), e);
                throw e;
            }
        }, () -> {
            log.error("Order {} not found when processing PaymentSucceededEvent", event.getOrderId());
        });
    }

    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        orderRepository.findById(event.getOrderId()).ifPresentOrElse(order -> {
            String current = order.getPaymentStatus();
            if (current != null && ("FAILED".equalsIgnoreCase(current) || "CANCELLED".equalsIgnoreCase(current))) return;

            order.setPaymentStatus("FAILED");
            order.setOrderStatus("CANCELLED");
            orderRepository.save(order);

        }, () -> log.error("Order {} not found when processing PaymentFailedEvent", event.getOrderId()));
    }
    
    /**
     * Send notifications to buyer and designers when payment is successful.
     * This method is called after payment processing is complete.
     */
    private void sendPaymentSuccessNotifications(Order order) {
        try {
            // 1️⃣ Send notification to buyer (purchase confirmation)
            String buyerMessage = buildBuyerNotificationMessage(order);
            CreateNotificationRequest buyerNotification = CreateNotificationRequest.builder()
                    .userId(order.getUserId())
                    .title("Thanh toán thành công")
                    .message(buyerMessage)
                    .type("PURCHASE_CONFIRM")
                    .orderId(order.getOrderId())
                    .build();
            
            identityClient.createNotification(buyerNotification);
            log.info("✅ Sent purchase confirmation notification to user {}", order.getUserId());
            
            // 2️⃣ Send notifications to designers (one per unique template)
            if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
                order.getOrderDetails().stream()
                        .collect(Collectors.groupingBy(OrderDetail::getResourceTemplateId))
                        .forEach((templateId, details) -> {
                            try {
                                ResourceTemplate template = resourceTemplateRepository
                                        .findById(templateId)
                                        .orElse(null);
                                
                                if (template != null && template.getDesignerId() != null) {
                                    String designerMessage = String.format(
                                            "Tài nguyên '%s' của bạn đã được mua. Mã đơn: %s",
                                            template.getName(),
                                            order.getInvoiceNumber()
                                    );
                                    
                                    CreateNotificationRequest designerNotification = CreateNotificationRequest.builder()
                                            .userId(template.getDesignerId())
                                            .title("Tài nguyên của bạn đã được mua")
                                            .message(designerMessage)
                                            .type("PURCHASE")
                                            .orderId(order.getOrderId())
                                            .resourceItemId(templateId)
                                            .build();
                                    
                                    identityClient.createNotification(designerNotification);
                                    log.info("✅ Sent purchase notification to designer {} for template {}", 
                                            template.getDesignerId(), templateId);
                                }
                            } catch (Exception e) {
                                log.error("❌ Failed to send notification to designer for template {}: {}", 
                                        templateId, e.getMessage());
                            }
                        });
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to send payment success notifications for order {}: {}", 
                    order.getOrderId(), e.getMessage(), e);
            // Don't throw exception - notifications are not critical to payment flow
        }
    }
    
    /**
     * Build a detailed message for the buyer notification.
     */
    private String buildBuyerNotificationMessage(Order order) {
        if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            return String.format(
                    "Bạn đã thanh toán thành công. Mã đơn: %s. Tổng tiền: %s VND",
                    order.getInvoiceNumber(),
                    order.getTotalAmount()
            );
        }
        
        String itemsList = order.getOrderDetails().stream()
                .map(detail -> {
                    ResourceTemplate template = resourceTemplateRepository
                            .findById(detail.getResourceTemplateId())
                            .orElse(null);
                    return template != null ? template.getName() : "Template #" + detail.getResourceTemplateId();
                })
                .collect(Collectors.joining(", "));
        
        return String.format(
                "Bạn đã mua thành công: %s. Mã đơn: %s. Tổng tiền: %s VND",
                itemsList,
                order.getInvoiceNumber(),
                order.getTotalAmount()
        );
    }
}

package com.sketchnotes.order_service.service.implement;

import com.sketchnotes.order_service.client.IdentityClient;
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
}

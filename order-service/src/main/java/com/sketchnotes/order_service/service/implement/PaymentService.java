package com.sketchnotes.order_service.service.implement;

import com.sketchnotes.order_service.events.PaymentFailedEvent;
import com.sketchnotes.order_service.events.PaymentSucceededEvent;
import com.sketchnotes.order_service.repository.OrderRepository;
import com.sketchnotes.order_service.repository.OrderDetailRepository;
import com.sketchnotes.order_service.service.UserResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserResourceService userResourceService;

    @Transactional
    public void handlePaymentSuccess(PaymentSucceededEvent event) {
        orderRepository.findById(event.getOrderId()).ifPresentOrElse(order -> {
            boolean alreadyPaid = "PAID".equalsIgnoreCase(order.getPaymentStatus());

            try {
                if (!alreadyPaid) {
                    order.setPaymentStatus("PAID");
                    order.setOrderStatus("CONFIRMED");
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

package com.sketchnotes.order_service.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sketchnotes.order_service.entity.Order;
import com.sketchnotes.order_service.entity.OrderEventLog;
import com.sketchnotes.order_service.exception.OrderNotFoundException;
import com.sketchnotes.order_service.repository.OrderEventLogRepository;
import com.sketchnotes.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderRepository orderRepository;
    private final OrderEventLogRepository orderEventLogRepository;
    private final ObjectMapper objectMapper;
    private final StreamBridge streamBridge;

    // Consumer cho PaymentSucceededEvent
    @Bean
    public Consumer<PaymentSucceededEvent> paymentSucceededConsumer() {
        return event -> {
            log.info("✅ Payment succeeded for order {}", event.getOrderId());

            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new OrderNotFoundException("Order not found: " + event.getOrderId()));
            order.setPaymentStatus("PAID");
            order.setOrderStatus("CONFIRMED");
            orderRepository.save(order);

            // Ghi log
            try {
                orderEventLogRepository.save(OrderEventLog.builder()
                        .orderId(order.getOrderId())
                        .eventType("PAYMENT_SUCCEEDED")
                        .payload(objectMapper.writeValueAsString(event))
                        .build());
            } catch (Exception e) {
                log.error("Error logging payment success event: {}", e.getMessage());
            }

            // 👉 Grant quyền template cho user (nếu có logic riêng, implement tại đây)
            log.info("🎁 Granting resource templates to user {}", order.getUserId());
        };
    }

    // Consumer cho PaymentFailedEvent
    @Bean
    public Consumer<PaymentFailedEvent> paymentFailedConsumer() {
        return event -> {
            log.warn("❌ Payment failed for order {}", event.getOrderId());

            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new OrderNotFoundException("Order not found: " + event.getOrderId()));
            order.setPaymentStatus("FAILED");
            order.setOrderStatus("CANCELLED");
            orderRepository.save(order);

            // Ghi log event
            try {
                orderEventLogRepository.save(OrderEventLog.builder()
                        .orderId(order.getOrderId())
                        .eventType("PAYMENT_FAILED")
                        .payload(objectMapper.writeValueAsString(event))
                        .build());
            } catch (Exception e) {
                log.error("Error logging payment failed event: {}", e.getMessage());
            }
        };
    }
}
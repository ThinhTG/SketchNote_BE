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
            try {
                orderRepository.findById(event.getOrderId()).ifPresentOrElse(order -> {
                    if ("PAID".equalsIgnoreCase(order.getPaymentStatus())) {
                        return;
                    }
                    order.setPaymentStatus("PAID");
                    order.setOrderStatus("CONFIRMED");
                    orderRepository.save(order);
                    log.info("Order {} - Payment succeeded", order.getOrderId());
                }, () -> {
                    log.warn("Order {} not found", event.getOrderId());
                });
            } catch (Exception ex) {
                log.error("Order {} - Payment processing error: {}", event.getOrderId(), ex.getMessage());
            }
        };
    }

    // Consumer cho PaymentFailedEvent
    @Bean
    public Consumer<PaymentFailedEvent> paymentFailedConsumer() {
        return event -> {
            try {
                orderRepository.findById(event.getOrderId()).ifPresentOrElse(order -> {
                    String current = order.getPaymentStatus();
                    if (current != null && ("FAILED".equalsIgnoreCase(current) || "CANCELLED".equalsIgnoreCase(current))) {
                        return;
                    }
                    order.setPaymentStatus("FAILED");
                    order.setOrderStatus("CANCELLED");
                    orderRepository.save(order);
                    log.info("Order {} - Payment failed: {}", order.getOrderId(), event.getReason());
                }, () -> {
                    log.warn("Order {} not found", event.getOrderId());
                });
            } catch (Exception ex) {
                log.error("Order {} - Payment failure processing error: {}", event.getOrderId(), ex.getMessage());
            }


        };
    }
}
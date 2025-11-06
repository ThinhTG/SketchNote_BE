package com.sketchnotes.order_service.events;

import com.sketchnotes.order_service.service.implement.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final PaymentService paymentService;

    @Bean
    public Consumer<PaymentSucceededEvent> paymentSucceededConsumer() {
        log.info("Registering paymentSucceededConsumer bean");
        return event -> {
            log.info("PaymentSucceededEvent received: orderId={}, txId={}, method={}",
                    event.getOrderId(), event.getTransactionId(), event.getPaymentMethod());
            try {
                paymentService.handlePaymentSuccess(event);
            } catch (Exception e) {
                log.error("Error processing PaymentSucceededEvent: {}", e.getMessage(), e);
                throw e;
            }
        };
    }

    @Bean
    public Consumer<PaymentFailedEvent> paymentFailedConsumer() {
        log.info("Registering paymentFailedConsumer bean");
        return event -> {
            log.info("PaymentFailedEvent received: orderId={}, reason={}", event.getOrderId(), event.getReason());
            try {
                paymentService.handlePaymentFailed(event);
            } catch (Exception e) {
                log.error("Error processing PaymentFailedEvent: {}", e.getMessage(), e);
                throw e;
            }
        };
    }
}

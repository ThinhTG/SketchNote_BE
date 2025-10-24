package com.sketchnotes.payment_service.events;

import com.sketchnotes.payment_service.entity.Wallet;
import com.sketchnotes.payment_service.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProcessor {

    private final WalletService walletService;
    private final StreamBridge streamBridge;

    @Bean
    public Consumer<OrderCreatedEvent> orderCreatedConsumer() {
        return event -> {
            log.info("📥 Received OrderCreatedEvent orderId={} userId={} amount={}", event.getOrderId(), event.getUserId(), event.getTotalAmount());
            try {
                Wallet wallet = walletService.getWalletByUserId(event.getUserId());
                if (wallet.getBalance().compareTo(event.getTotalAmount()) >= 0) {
                    // perform payment
                    var tx = walletService.pay(wallet.getWalletId(), event.getTotalAmount());
                    log.info("✅ Payment processed for order {} txId={}", event.getOrderId(), tx.getId());

                    PaymentSucceededEvent success = PaymentSucceededEvent.builder()
                            .orderId(event.getOrderId())
                            .userId(event.getUserId())
                            .amount(event.getTotalAmount())
                            .transactionId(String.valueOf(tx.getId()))
                            .build();

                    streamBridge.send("paymentSucceeded-out-0", success);
                } else {
                    log.warn("❌ Insufficient balance for user {} order {}", event.getUserId(), event.getOrderId());
                    PaymentFailedEvent failed = PaymentFailedEvent.builder()
                            .orderId(event.getOrderId())
                            .userId(event.getUserId())
                            .amount(event.getTotalAmount())
                            .reason("INSUFFICIENT_BALANCE")
                            .build();
                    streamBridge.send("paymentFailed-out-0", failed);
                }
            } catch (Exception e) {
                log.error("Error processing payment for order {}: {}", event.getOrderId(), e.getMessage(), e);
                PaymentFailedEvent failed = PaymentFailedEvent.builder()
                        .orderId(event.getOrderId())
                        .userId(event.getUserId())
                        .amount(event.getTotalAmount())
                        .reason(e.getMessage())
                        .build();
                streamBridge.send("paymentFailed-out-0", failed);
            }
        };
    }
}

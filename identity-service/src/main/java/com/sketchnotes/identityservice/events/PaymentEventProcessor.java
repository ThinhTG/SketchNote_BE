package com.sketchnotes.identityservice.events;

import com.sketchnotes.identityservice.model.Wallet;
import com.sketchnotes.identityservice.service.interfaces.IWalletService;
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

    private final IWalletService walletService;
    private final StreamBridge streamBridge;

    @Bean
    public Consumer<OrderCreatedEvent> orderCreatedConsumer() {
        return event -> {
            try {
                Wallet wallet = walletService.getWalletByUserId(event.getUserId());
                if (wallet.getBalance().compareTo(event.getTotalAmount()) >= 0) {
                    // perform payment
                    var tx = walletService.pay(wallet.getWalletId(), event.getTotalAmount());
                    PaymentSucceededEvent success = PaymentSucceededEvent.builder()
                            .orderId(event.getOrderId())
                            .userId(event.getUserId())
                            .amount(event.getTotalAmount())
                            .transactionId(String.valueOf(tx.getTransactionId()))
                            .build();

                    streamBridge.send("paymentSucceeded-out-0", success);
                } else {
                    PaymentFailedEvent failed = PaymentFailedEvent.builder()
                            .orderId(event.getOrderId())
                            .userId(event.getUserId())
                            .amount(event.getTotalAmount())
                            .reason("INSUFFICIENT_BALANCE")
                            .build();
                    streamBridge.send("paymentFailed-out-0", failed);
                }
            } catch (Exception e) {
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
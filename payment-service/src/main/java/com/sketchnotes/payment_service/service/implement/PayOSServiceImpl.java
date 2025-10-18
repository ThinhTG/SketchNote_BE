package com.sketchnotes.payment_service.service.implement;

import com.sketchnotes.payment_service.entity.Transaction;
import com.sketchnotes.payment_service.entity.Wallet;
import com.sketchnotes.payment_service.entity.enumeration.PaymentStatus;
import com.sketchnotes.payment_service.entity.enumeration.TransactionType;
import com.sketchnotes.payment_service.repository.TransactionRepository;
import com.sketchnotes.payment_service.repository.WalletRepository;
import com.sketchnotes.payment_service.service.PaymentGatewayService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.type.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@Service("payosService")
@RequiredArgsConstructor
public class PayOSServiceImpl implements PaymentGatewayService {

    private final PayOS payOS;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public String createPaymentLink(Long walletId, BigDecimal amount, String description) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        try {
            long orderCode = System.currentTimeMillis(); // unique order id

            // tạo request payment
            PaymentData paymentData = PaymentData.builder()
                    .orderCode(orderCode)
                    .amount(amount.intValue()) // PayOS dùng int (VND)
                    .description(description)
                    .returnUrl("http://localhost:3000/payment/success") // FE sẽ handle
                    .cancelUrl("http://localhost:3000/payment/cancel")
                    .items(Collections.singletonList(
                            ItemData.builder()
                                    .name("Deposit to wallet " + walletId)
                                    .quantity(1)
                                    .price(amount.intValue())
                                    .build()
                    ))
                    .build();

            CheckoutResponseData response = payOS.createPaymentLink(paymentData);

            // lưu transaction ở trạng thái PENDING
            Transaction tx = Transaction.builder()
                    .wallet(wallet)
                    .amount(amount)
                    .status(PaymentStatus.PENDING)
                    .type(TransactionType.DEPOSIT)
                    .orderCode(orderCode) // lưu để mapping với callback
                    .createdAt(LocalDateTime.now())
                    .build();
            transactionRepository.save(tx);

            return response.getCheckoutUrl();

        } catch (Exception e) {
            throw new RuntimeException("Error creating PayOS payment: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void handleCallback(Webhook webhook) {
        try {
            // 1️⃣ Verify callback với SDK PayOS
            WebhookData data = payOS.verifyPaymentWebhookData(webhook);

            Long orderCode = data.getOrderCode();
            int amount = data.getAmount();
            String code = data.getCode(); // "00" = success, "01" = fail

            log.info("📩 Received PayOS webhook: orderCode={}, amount={}, code={}", orderCode, amount, code);

            // 2️⃣ Tìm transaction tương ứng
            Transaction tx = transactionRepository.findByOrderCode(orderCode).orElse(null);
            if (tx == null) {
                log.warn("⚠️ Transaction not found for orderCode {}", orderCode);
                return; // KHÔNG throw để PayOS nhận 200 OK
            }

            // 3️⃣ Idempotent: nếu đã xử lý thì bỏ qua
            if (tx.getStatus() == PaymentStatus.SUCCESS || tx.getStatus() == PaymentStatus.FAILED) {
                log.info("ℹ️ Transaction {} already processed with status {}", orderCode, tx.getStatus());
                return;
            }

            // 4️⃣ Cập nhật trạng thái thanh toán
            if ("00".equals(code)) {
                tx.setStatus(PaymentStatus.SUCCESS);
                Wallet wallet = tx.getWallet();
                if (wallet != null) {
                    wallet.setBalance(wallet.getBalance().add(tx.getAmount()));
                    walletRepository.save(wallet);
                }
                log.info("✅ Payment SUCCESS for orderCode={}, amount={}", orderCode, tx.getAmount());
            } else {
                tx.setStatus(PaymentStatus.FAILED);
                log.info("❌ Payment FAILED for orderCode={}", orderCode);
            }

//            tx.(LocalDateTime.now());
            transactionRepository.save(tx);

            // 5️⃣ (Tuỳ chọn) gửi sự kiện sang Order-Service nếu bạn dùng SAGA
            // orderEventProducer.publishPaymentEvent(tx);

        } catch (Exception e) {
            // ❗ Không throw để tránh trả lỗi 500 cho PayOS
            log.error("🚨 Error verifying PayOS callback: {}", e.getMessage(), e);
        }
    }


}

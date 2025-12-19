package com.sketchnotes.identityservice.service.implement;

import com.sketchnotes.identityservice.model.Transaction;
import com.sketchnotes.identityservice.model.Wallet;
import com.sketchnotes.identityservice.enums.PaymentStatus;
import com.sketchnotes.identityservice.enums.TransactionType;
import com.sketchnotes.identityservice.repository.ITransactionRepository;
import com.sketchnotes.identityservice.repository.IWalletRepository;
import com.sketchnotes.identityservice.service.interfaces.IPaymentGatewayService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.WebhookData;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service("payosService")
@RequiredArgsConstructor
public class PayOSServiceImpl implements IPaymentGatewayService {

    private final PayOS payOS;
    private final IWalletRepository walletRepository;
    private final ITransactionRepository transactionRepository;

    @Override
    public String createPaymentLink(Long walletId, BigDecimal amount, String description) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        try {
            String txnRef = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            long orderCode = Math.abs(txnRef.hashCode());

            CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(amount.longValue())
                    .description(txnRef)
                    .returnUrl("https://sketch-note-visual-note-taking.web.app/wallet-success")
                    .cancelUrl("https://sketch-note-visual-note-taking.web.app/wallet-fail")
                    .build();

            CreatePaymentLinkResponse checkoutResponse = payOS.paymentRequests().create(paymentData);

            // lưu transaction ở trạng thái PENDING
            Transaction tx = Transaction.builder()
                    .wallet(wallet)
                    .amount(amount)
                    .balance(wallet.getBalance())
                    .status(PaymentStatus.PENDING)
                    .type(TransactionType.DEPOSIT)
                    .orderCode(orderCode) // lưu để mapping với callback
                    .createdAt(LocalDateTime.now())
                    .build();
            transactionRepository.save(tx);

            return checkoutResponse.getCheckoutUrl();

        } catch (Exception e) {
            throw new RuntimeException("Error creating PayOS payment: " + e.getMessage(), e);
        }
    }

    @Transactional
    public ResponseEntity<String> handleWebhook(Map<String, Object> requestBody) {
        try {
            // 1️⃣ Xác thực webhook bằng SDK PayOS 2.0.1
            WebhookData data = payOS.webhooks().verify(requestBody);

            Long orderCode = data.getOrderCode();
            long amount = data.getAmount();
            String code = data.getCode(); // "00" = thành công

            log.info("📩 Webhook PayOS: orderCode={}, amount={}, code={}",
                    orderCode, amount, code);

            // 2️⃣ Tìm transaction tương ứng
            Transaction tx = transactionRepository.findByOrderCode(orderCode).orElse(null);
            if (tx == null) {
                log.warn("⚠️ No transaction found for orderCode {}", orderCode);
                return ResponseEntity.ok("ignored");  // tránh PayOS retry
            }

            // 3️⃣ Idempotency: Tránh xử lý 2 lần
            if (tx.getStatus() != PaymentStatus.PENDING) {
                log.info("ℹ️ Transaction {} already processed (status={})",
                        orderCode, tx.getStatus());
                return ResponseEntity.ok("ignored");
            }

            // 4️⃣ Update trạng thái giao dịch
            if ("00".equals(code)) {
                tx.setStatus(PaymentStatus.SUCCESS);

                Wallet wallet = tx.getWallet();
                wallet.setBalance(wallet.getBalance().add(tx.getAmount()));
                walletRepository.save(wallet);

                log.info("✅ Deposit SUCCESS: orderCode={}, amount={}",
                        orderCode, tx.getAmount());
            } else {
                tx.setStatus(PaymentStatus.FAILED);
                log.info("❌ Deposit FAILED: orderCode={}", orderCode);
            }

            transactionRepository.save(tx);

            // 5️⃣ Trả về 200 OK để PayOS không retry
            return ResponseEntity.ok("ok");

        } catch (Exception e) {
            log.error("🚨 Error verifying PayOS webhook: {}", e.getMessage(), e);

            // Luôn trả về 200 OK — KHÔNG BAO GIỜ trả lỗi để tránh PayOS retry liên tục
            return ResponseEntity.ok("ignored");
        }
    }



}

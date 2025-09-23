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
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.type.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

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
            // 1. Verify callback
            WebhookData data = payOS.verifyPaymentWebhookData(webhook);

            // 2. Lấy thông tin từ callback
            Long orderCode = data.getOrderCode();
            int amount = data.getAmount();
            String code = data.getCode(); // theo PayOS docs: "00" = success, "01" = fail

            // 3. Tìm transaction tương ứng
            Transaction tx = transactionRepository.findByOrderCode(orderCode)
                    .orElseThrow(() -> new RuntimeException("Transaction not found for orderCode " + orderCode));

            // 4. Idempotent: nếu đã xử lý thì bỏ qua
            if (tx.getStatus() == PaymentStatus.SUCCESS || tx.getStatus() == PaymentStatus.FAILED) {
                return;
            }

            if ("00".equals(code)) { // thành công
                tx.setStatus(PaymentStatus.SUCCESS);

                // Cộng tiền vào ví
                Wallet wallet = tx.getWallet();
                wallet.setBalance(wallet.getBalance().add(tx.getAmount()));
                walletRepository.save(wallet);

            } else { // thất bại
                tx.setStatus(PaymentStatus.FAILED);
            }

            transactionRepository.save(tx);

        } catch (Exception e) {
            throw new RuntimeException("Error verifying PayOS callback: " + e.getMessage(), e);
        }
    }
}

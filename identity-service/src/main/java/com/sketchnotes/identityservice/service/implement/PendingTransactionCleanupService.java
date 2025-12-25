package com.sketchnotes.identityservice.service.implement;

import com.sketchnotes.identityservice.enums.PaymentStatus;
import com.sketchnotes.identityservice.model.Transaction;
import com.sketchnotes.identityservice.repository.ITransactionRepository;
import com.sketchnotes.identityservice.service.interfaces.IPendingTransactionCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service để xử lý các Transaction PENDING đã quá hạn.
 * 
 * Khi user click vào link thanh toán PayOS nhưng KHÔNG quét mã QR hoặc cancel,
 * PayOS sẽ KHÔNG gửi webhook (vì chưa có giao dịch nào xảy ra).
 * Do đó các Transaction sẽ mãi mãi ở trạng thái PENDING.
 * 
 * Service này sẽ:
 * 1. Tìm tất cả Transaction PENDING đã tạo quá 20 phút (PayOS link expire sau 15 phút + 5 phút grace)
 * 2. Đánh dấu FAILED vì payment link đã hết hạn
 * 
 * Lưu ý: PayOS SDK 2.0.1 không có API để query trạng thái payment link.
 * Do đó chúng ta dựa vào timeout-based cleanup.
 * Nếu webhook PAID đến sau khi transaction đã bị mark FAILED, webhook handler
 * sẽ từ chối xử lý vì transaction status != PENDING (idempotency check).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PendingTransactionCleanupService implements IPendingTransactionCleanupService {

    private final ITransactionRepository transactionRepository;

    /**
     * Thời gian timeout cho một payment link (phút).
     * PayOS payment link mặc định expire sau 15 phút.
     */
    private static final int PAYMENT_TIMEOUT_MINUTES = 15;

    /**
     * Thời gian grace period thêm (phút) để đảm bảo webhook có thời gian arrive.
     * PayOS có thể delay gửi webhook vài phút sau khi user thanh toán.
     */
    private static final int GRACE_PERIOD_MINUTES = 5;

    /**
     * Scheduled job chạy mỗi 5 phút để cleanup các PENDING transactions đã quá hạn.
     * 
     * Logic:
     * - PayOS link hết hạn sau 15 phút
     * - Thêm 5 phút grace period để webhook có thời gian đến
     * - Sau 20 phút mà vẫn PENDING => đánh dấu FAILED
     */
    @Override
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5 phút
    @Transactional
    public void cleanupPendingTransactions() {
        log.info("Starting cleanup of pending transactions...");

        LocalDateTime cutoffTime = LocalDateTime.now()
                .minusMinutes(PAYMENT_TIMEOUT_MINUTES + GRACE_PERIOD_MINUTES);

        List<Transaction> pendingTransactions = transactionRepository
                .findPendingTransactionsBeforeCutoff(PaymentStatus.PENDING, cutoffTime);

        if (pendingTransactions.isEmpty()) {
            log.info("No pending transactions to cleanup");
            return;
        }

        log.info("Found {} pending transactions to mark as FAILED (payment link expired)", 
                pendingTransactions.size());

        int processedCount = 0;
        int errorCount = 0;

        for (Transaction tx : pendingTransactions) {
            try {
                markTransactionAsFailed(tx);
                processedCount++;
            } catch (Exception e) {
                log.error("Error processing transaction {}: {}", tx.getTransactionId(), e.getMessage());
                errorCount++;
            }
        }

        log.info("Cleanup completed: {} transactions marked as FAILED, {} errors", 
                processedCount, errorCount);
    }

    /**
     * Đánh dấu transaction là FAILED vì payment link đã hết hạn.
     */
    private void markTransactionAsFailed(Transaction tx) {
        Long orderCode = tx.getOrderCode();
        LocalDateTime createdAt = tx.getCreatedAt();
        
        log.info("Transaction {} (orderCode: {}) created at {} has expired", 
                tx.getTransactionId(), orderCode, createdAt);

        tx.setStatus(PaymentStatus.FAILED);
        
        String failReason = "Payment link expired - no payment received within " 
                + (PAYMENT_TIMEOUT_MINUTES + GRACE_PERIOD_MINUTES) + " minutes";
        tx.setDescription(tx.getDescription() != null 
                ? tx.getDescription() + " | " + failReason 
                : failReason);
        
        transactionRepository.save(tx);
        log.info("Transaction {} marked as FAILED: {}", tx.getTransactionId(), failReason);
    }

    /**
     * Admin API: Force cleanup tất cả PENDING transactions (không chờ timeout).
     * Có thể gọi từ controller khi cần cleanup thủ công.
     */
    @Transactional
    public int forceCleanupAllPending() {
        log.info("Admin: Force cleanup all pending transactions...");

        List<Transaction> pendingTransactions = transactionRepository
                .findPendingTransactionsBeforeCutoff(PaymentStatus.PENDING, LocalDateTime.now());

        if (pendingTransactions.isEmpty()) {
            log.info("No pending transactions found");
            return 0;
        }

        int count = 0;
        for (Transaction tx : pendingTransactions) {
            try {
                tx.setStatus(PaymentStatus.FAILED);
                tx.setDescription(tx.getDescription() != null 
                        ? tx.getDescription() + " | Admin force cleanup" 
                        : "Admin force cleanup");
                transactionRepository.save(tx);
                count++;
            } catch (Exception e) {
                log.error("Error force cleaning transaction {}: {}", tx.getTransactionId(), e.getMessage());
            }
        }

        log.info("Force cleanup completed: {} transactions marked as FAILED", count);
        return count;
    }
}

package com.sketchnotes.order_service.service.implement;

import com.sketchnotes.order_service.client.PaymentClient;
import com.sketchnotes.order_service.dtos.PaymentResponseDTO;
import com.sketchnotes.order_service.entity.Order;
import com.sketchnotes.order_service.repository.OrderRepository;
import com.sketchnotes.order_service.service.IPendingOrderCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service để xử lý các Order PENDING đã quá hạn.
 * 
 * Khi user tạo order và chuyển sang trang thanh toán PayOS nhưng KHÔNG thanh toán,
 * Order sẽ mãi mãi ở trạng thái PENDING vì PayOS không gửi webhook khi không có action.
 * 
 * Service này sẽ:
 * 1. Tìm tất cả Order PENDING đã tạo quá 20 phút (PayOS link expire sau 15 phút + buffer)
 * 2. Gọi Payment Service để kiểm tra trạng thái thực tế
 * 3. Cập nhật trạng thái Order tương ứng:
 *    - PAID -> SUCCESS
 *    - CANCELLED/EXPIRED -> CANCELLED
 *    - PENDING (quá lâu) -> EXPIRED/CANCELLED
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PendingOrderCleanupService implements IPendingOrderCleanupService {

    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;

    /**
     * Thời gian timeout cho một payment link (phút).
     * PayOS payment link mặc định expire sau 15 phút.
     */
    private static final int PAYMENT_TIMEOUT_MINUTES = 15;

    /**
     * Thời gian grace period thêm (phút) để đảm bảo webhook có thời gian arrive.
     */
    private static final int GRACE_PERIOD_MINUTES = 5;

    /**
     * Thời gian tối đa để giữ order ở trạng thái PENDING (phút).
     * Sau thời gian này, order sẽ tự động bị hủy.
     */
    private static final int MAX_PENDING_MINUTES = 30;

    /**
     * Scheduled job chạy mỗi 5 phút để kiểm tra và cleanup các PENDING orders.
     */
    @Override
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5 phút
    @Transactional
    public void cleanupPendingOrders() {
        log.info("🔄 Starting cleanup of pending orders...");

        // Tìm các order PENDING đã tạo quá (TIMEOUT + GRACE) phút
        LocalDateTime cutoffTime = LocalDateTime.now()
                .minusMinutes(PAYMENT_TIMEOUT_MINUTES + GRACE_PERIOD_MINUTES);

        List<Order> pendingOrders = orderRepository.findPendingOrdersBeforeCutoff(cutoffTime);

        if (pendingOrders.isEmpty()) {
            log.info("✅ No pending orders to cleanup");
            return;
        }

        log.info("📋 Found {} pending orders to check", pendingOrders.size());

        int paidCount = 0;
        int cancelledCount = 0;
        int errorCount = 0;

        for (Order order : pendingOrders) {
            try {
                processOrder(order);
                if ("PAID".equals(order.getPaymentStatus())) {
                    paidCount++;
                } else if ("CANCELLED".equals(order.getPaymentStatus()) || 
                           "EXPIRED".equals(order.getPaymentStatus())) {
                    cancelledCount++;
                }
            } catch (Exception e) {
                log.error("❌ Error processing order {}: {}", order.getOrderId(), e.getMessage());
                errorCount++;
                
                // Nếu order quá cũ và lỗi, vẫn đánh dấu là EXPIRED
                if (isOrderTooOld(order)) {
                    markOrderAsExpired(order, "Processing error after timeout: " + e.getMessage());
                    cancelledCount++;
                }
            }
        }

        log.info("✅ Cleanup completed: {} paid, {} cancelled/expired, {} errors", 
                paidCount, cancelledCount, errorCount);
    }

    /**
     * Kiểm tra trạng thái payment và cập nhật order.
     */
    private void processOrder(Order order) {
        String orderCode = generateOrderCode(order);
        log.info("🔍 Checking payment status for order {} (orderCode: {})", 
                order.getOrderId(), orderCode);

        try {
            // Gọi Payment Service để lấy trạng thái payment
            PaymentResponseDTO paymentStatus = paymentClient.getPaymentStatus(orderCode);

            if (paymentStatus == null) {
                log.warn("⚠️ No payment status found for orderCode: {}", orderCode);
                if (isOrderTooOld(order)) {
                    markOrderAsExpired(order, "Payment link not found");
                }
                return;
            }

            String status = paymentStatus.getStatus();
            log.info("📩 Payment status for order {}: {}", order.getOrderId(), status);

            if (status == null) {
                if (isOrderTooOld(order)) {
                    markOrderAsExpired(order, "Unknown payment status");
                }
                return;
            }

            switch (status.toUpperCase()) {
                case "PAID":
                    // Payment thành công - cập nhật order
                    // Lưu ý: việc xử lý business logic (tạo UserResource, etc.) 
                    // sẽ do PaymentEventConsumer xử lý khi nhận event
                    handlePaidOrder(order);
                    break;
                    
                case "CANCELLED":
                case "EXPIRED":
                    // Payment bị hủy hoặc hết hạn
                    markOrderAsCancelled(order, "Payment " + status.toLowerCase() + " by user");
                    break;
                    
                case "PENDING":
                    // Vẫn đang chờ - kiểm tra xem có quá lâu không
                    if (isOrderTooOld(order)) {
                        log.info("⏰ Order {} is too old (still PENDING), marking as EXPIRED", 
                                order.getOrderId());
                        markOrderAsExpired(order, "Payment timeout - user did not complete payment");
                    } else {
                        log.info("⏳ Order {} still PENDING, will check again later", 
                                order.getOrderId());
                    }
                    break;
                    
                default:
                    log.warn("⚠️ Unknown payment status: {} for order: {}", status, order.getOrderId());
                    if (isOrderTooOld(order)) {
                        markOrderAsExpired(order, "Unknown status: " + status);
                    }
            }

        } catch (Exception e) {
            log.error("❌ Error checking payment status for order {}: {}", 
                    order.getOrderId(), e.getMessage());
            throw e;
        }
    }

    /**
     * Xử lý order đã được thanh toán thành công.
     * Lưu ý: Business logic chính (tạo UserResource, etc.) được xử lý bởi event.
     */
    private void handlePaidOrder(Order order) {
        // Kiểm tra idempotency
        if ("PAID".equals(order.getPaymentStatus())) {
            log.info("ℹ️ Order {} already marked as PAID", order.getOrderId());
            return;
        }

        order.setPaymentStatus("PAID");
        order.setOrderStatus("SUCCESS");
        orderRepository.save(order);
        
        log.info("✅ Order {} marked as PAID/SUCCESS (discovered by cleanup job)", order.getOrderId());
        
        // TODO: Có thể cần publish event để trigger business logic
        // Tuy nhiên webhook của PayOS đã xử lý rồi, nên chỗ này chỉ là safety net
    }

    /**
     * Đánh dấu order bị hủy.
     */
    private void markOrderAsCancelled(Order order, String reason) {
        order.setPaymentStatus("CANCELLED");
        order.setOrderStatus("CANCELLED");
        orderRepository.save(order);
        log.info("❌ Order {} marked as CANCELLED: {}", order.getOrderId(), reason);
    }

    /**
     * Đánh dấu order hết hạn (timeout).
     */
    private void markOrderAsExpired(Order order, String reason) {
        order.setPaymentStatus("EXPIRED");
        order.setOrderStatus("CANCELLED");
        orderRepository.save(order);
        log.info("⏰ Order {} marked as EXPIRED: {}", order.getOrderId(), reason);
    }

    /**
     * Kiểm tra xem order có quá cũ không (hơn MAX_PENDING_MINUTES phút).
     */
    private boolean isOrderTooOld(Order order) {
        if (order.getCreatedAt() == null) return true;
        LocalDateTime maxAge = LocalDateTime.now().minusMinutes(MAX_PENDING_MINUTES);
        return order.getCreatedAt().isBefore(maxAge);
    }

    /**
     * Generate orderCode từ Order (dùng invoiceNumber hoặc orderId).
     */
    private String generateOrderCode(Order order) {
        if (order.getInvoiceNumber() != null) {
            return order.getInvoiceNumber().replace("INV-", "");
        }
        return String.valueOf(order.getOrderId());
    }
}

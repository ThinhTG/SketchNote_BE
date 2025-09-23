package com.sketchnotes.payment_service.service;

import java.math.BigDecimal;

public interface PaymentGatewayService {
    /**
     * Tạo link thanh toán (redirect user đến cổng thanh toán)
     */
    String createPaymentLink(Long walletId, BigDecimal amount, String description);

//    /**
//     * Xác minh callback từ cổng thanh toán
//     */
//    boolean verifyCallback(String rawData, String signature);
//
//    /**
//     * Xử lý callback (update ví, transaction...)
//     */
//    void handleCallback(String rawData, String signature);
}
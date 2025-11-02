package com.sketchnotes.identityservice.service.interfaces;

import vn.payos.type.Webhook;

import java.math.BigDecimal;

public interface IPaymentGatewayService {
    /**
     * Tạo link thanh toán (redirect user đến cổng thanh toán)
     */
    String createPaymentLink(Long walletId, BigDecimal amount, String description);

    void handleCallback(Webhook webhook);
}
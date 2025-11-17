package com.sketchnotes.identityservice.service.interfaces;



import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;

public interface IPaymentGatewayService {
    /**
     * Tạo link thanh toán (redirect user đến cổng thanh toán)
     */
    String createPaymentLink(Long walletId, BigDecimal amount, String description);

    public ResponseEntity<String> handleWebhook(Map<String, Object> requestBody);

}
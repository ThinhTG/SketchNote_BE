package com.sketchnotes.order_service.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayOSWebhookDTO {
    private String code;
    private String desc;
    private String data;
    private String signature;
    private String checksum;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PayOSDataDTO {
        private String orderCode;
        private BigDecimal amount;
        private String description;
        private String accountNumber;
        private String reference;
        private String transactionDateTime;
        private String currency;
        private String paymentLinkId;
        private String code;
        private String desc;
        private String counterAccountBankId;
        private String counterAccountBankName;
        private String counterAccountName;
        private String counterAccountNumber;
        private String virtualAccountName;
        private String virtualAccountNumber;
    }
}

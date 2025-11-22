package com.sketchnotes.order_service.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {
    private Long transactionId;
    private Long orderId;
    private BigDecimal amount;
    private BigDecimal balance;
    private TransactionType type;
    private String status;
    private String provider;
    private String externalTransactionId;
    private LocalDateTime createdAt;
    private Long orderCode;

    public TransactionResponse() {}

    public TransactionResponse(Long transactionId, Long orderId, BigDecimal amount, BigDecimal balance, TransactionType type, String status, String provider, String externalTransactionId, LocalDateTime createdAt, Long orderCode) {
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.amount = amount;
        this.balance = balance;
        this.type = type;
        this.status = status;
        this.provider = provider;
        this.externalTransactionId = externalTransactionId;
        this.createdAt = createdAt;
        this.orderCode = orderCode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(Long orderCode) {
        this.orderCode = orderCode;
    }

    public static class Builder {
        private Long transactionId;
        private Long orderId;
        private BigDecimal amount;
        private BigDecimal balance;
        private TransactionType type;
        private String status;
        private String provider;
        private String externalTransactionId;
        private LocalDateTime createdAt;
        private Long orderCode;

        public Builder transactionId(Long transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder orderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder balance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }

        public Builder type(TransactionType type) {
            this.type = type;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder externalTransactionId(String externalTransactionId) {
            this.externalTransactionId = externalTransactionId;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder orderCode(Long orderCode) {
            this.orderCode = orderCode;
            return this;
        }

        public TransactionResponse build() {
            return new TransactionResponse(transactionId, orderId, amount, balance, type, status, provider, externalTransactionId, createdAt, orderCode);
        }
    }
}

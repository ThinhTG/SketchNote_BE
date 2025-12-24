package com.sketchnotes.identityservice.mapper;

import com.sketchnotes.identityservice.dtos.response.TransactionResponse;
import com.sketchnotes.identityservice.model.Transaction;

public class TransactionMapper {
    
    public static TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        
        return TransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .orderId(transaction.getOrderId())
                .amount(transaction.getAmount())
                .balance(transaction.getBalance())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .provider(transaction.getProvider())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}

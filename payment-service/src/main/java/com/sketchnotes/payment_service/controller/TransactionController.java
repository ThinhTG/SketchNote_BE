package com.sketchnotes.payment_service.controller;

import com.sketchnotes.payment_service.dtos.ApiResponse;
import com.sketchnotes.payment_service.entity.Transaction;
import com.sketchnotes.payment_service.entity.enumeration.PaymentStatus;
import com.sketchnotes.payment_service.service.implement.TransactionServiceImp;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment/transactions")
public class TransactionController {

    private final TransactionServiceImp transactionServiceImp;

    @GetMapping("/filter")
    public ApiResponse<List<Transaction>> filterTransactions(
            @RequestParam(required = false) Long walletId,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String type
    ) {
        List<Transaction> transactions = transactionServiceImp.filterTransactions(walletId, status, type);
        return ApiResponse.success(transactions, "Transactions retrieved successfully");
    }

}

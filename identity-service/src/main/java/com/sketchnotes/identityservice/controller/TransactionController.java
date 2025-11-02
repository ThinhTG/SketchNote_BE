package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dto.ApiResponse;
import com.sketchnotes.identityservice.model.Transaction;
import com.sketchnotes.identityservice.enums.PaymentStatus;
import com.sketchnotes.identityservice.repository.ITransactionRepository;
import com.sketchnotes.identityservice.service.interfaces.ITransactionService;
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

    private final ITransactionService transactionServiceImp;

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

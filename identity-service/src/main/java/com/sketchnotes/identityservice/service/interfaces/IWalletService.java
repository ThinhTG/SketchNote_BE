package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.model.Transaction;
import com.sketchnotes.identityservice.model.Wallet;

import java.math.BigDecimal;
import java.util.List;

public interface IWalletService {
        Wallet createWallet(Long userId);
        Wallet getWallet(Long walletId);
        Wallet getWalletByUserId(Long userId);

        Transaction deposit(Long walletId, BigDecimal amount);
        /**
         * Deposit with explicit transaction type and optional description.
         * Use this for admin revenue deposits to distinguish between different sources.
         */
        Transaction depositWithType(Long walletId, BigDecimal amount, com.sketchnotes.identityservice.enums.TransactionType type, String description);
        Transaction withdraw(Long walletId, BigDecimal amount);
        Transaction pay(Long walletId, BigDecimal amount);
        /**
         * Pay with explicit transaction type and optional description. Useful to record specific purchase reasons.
         */
        Transaction payWithType(Long walletId, BigDecimal amount, com.sketchnotes.identityservice.enums.TransactionType type, String description);
        Transaction chargeCourse(Long walletId, BigDecimal amount);
        List<Transaction> getTransactions(Long walletId);
    }



package com.sketchnotes.payment_service.service;

import com.sketchnotes.payment_service.entity.Transaction;
import com.sketchnotes.payment_service.entity.Wallet;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
        Wallet createWallet(Long userId);
        Wallet getWallet(Long walletId);
        Wallet getWalletByUserId(Long userId);

        Transaction deposit(Long walletId, BigDecimal amount);
        Transaction withdraw(Long walletId, BigDecimal amount);
        Transaction pay(Long walletId, BigDecimal amount);

        List<Transaction> getTransactions(Long walletId);
    }



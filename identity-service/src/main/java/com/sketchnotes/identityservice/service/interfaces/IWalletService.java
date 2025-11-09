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
        Transaction withdraw(Long walletId, BigDecimal amount);
        Transaction pay(Long walletId, BigDecimal amount);
        Transaction chargeCourse(Long walletId, BigDecimal amount);
        List<Transaction> getTransactions(Long walletId);
    }



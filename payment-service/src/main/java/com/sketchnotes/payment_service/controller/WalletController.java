package com.sketchnotes.payment_service.controller;

import com.sketchnotes.payment_service.entity.Transaction;
import com.sketchnotes.payment_service.entity.Wallet;
import com.sketchnotes.payment_service.entity.enumeration.PaymentStatus;
import com.sketchnotes.payment_service.entity.enumeration.TransactionType;
import com.sketchnotes.payment_service.service.TransactionService;
import com.sketchnotes.payment_service.service.WalletService;
import com.sketchnotes.payment_service.service.implement.WalletServiceImp;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    // Tạo ví mới cho user
    @PostMapping("/create")
    public Wallet createWallet(@RequestParam Long userId) {
        return walletService.createWallet(userId);
    }

    // Lấy ví theo userId
    @GetMapping("/{userId}")
    public Wallet getWallet(@PathVariable Long userId) {
        return walletService.getWallet(userId);
    }

}


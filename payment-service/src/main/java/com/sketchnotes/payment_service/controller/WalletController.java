package com.sketchnotes.payment_service.controller;

import com.sketchnotes.payment_service.clients.IdentityClient;
import com.sketchnotes.payment_service.dtos.UserResponse;
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
    private final IdentityClient  identityClient;

    @PostMapping("/create")
    public Wallet createWallet() {
        var apiResponse = identityClient.getCurrentUser();

        UserResponse user = apiResponse.getResult(); // lấy user thật từ ApiResponse

        if (user == null || user.getId() == null) {
            throw new RuntimeException("User ID is null!");
        }

        return walletService.createWallet(user.getId());
    }


    // Lấy ví theo userId
    @GetMapping("/my-wallet")
    public Wallet getWallet() {
        var apiResponse = identityClient.getCurrentUser();
        UserResponse user = apiResponse.getResult(); // lấy user thật từ ApiResponse
        return walletService.getWalletByUserId(user.getId());
    }

}


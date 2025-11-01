package com.sketchnotes.payment_service.controller;

import com.sketchnotes.payment_service.clients.IdentityClient;
import com.sketchnotes.payment_service.dtos.ApiResponse;
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
    public ApiResponse<Wallet> createWallet() {
        var apiResponse = identityClient.getCurrentUser();

        UserResponse user = apiResponse.getResult(); // lấy user thật từ ApiResponse

        if (user == null || user.getId() == null) {
            throw new RuntimeException("User ID is null!");
        }

        Wallet wallet = walletService.createWallet(user.getId());
        return ApiResponse.success(wallet, "Wallet created successfully");
    }


    // Lấy ví theo userId
    @GetMapping("/my-wallet")
    public ApiResponse<Wallet> getWallet() {
        var apiResponse = identityClient.getCurrentUser();
        UserResponse user = apiResponse.getResult(); // lấy user thật từ ApiResponse
        Wallet wallet = walletService.getWalletByUserId(user.getId());
        return ApiResponse.success(wallet, "Wallet retrieved successfully");
    }

}


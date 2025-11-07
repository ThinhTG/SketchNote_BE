package com.sketchnotes.identityservice.controller;


import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.model.Transaction;
import com.sketchnotes.identityservice.model.Wallet;
import com.sketchnotes.identityservice.service.interfaces.IUserService;
import com.sketchnotes.identityservice.service.interfaces.IWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final IWalletService walletService;
    private final IUserService userService;

    @PostMapping("/create")
    public ApiResponse<Wallet> createWallet() {
        var user =  userService.getCurrentUser();

        if (user == null || user.getId() == null) {
            throw new RuntimeException("User ID is null!");
        }
        Wallet wallet = walletService.createWallet(user.getId());
        return ApiResponse.success(wallet, "Wallet created successfully");
    }


    // Lấy ví theo userId
    @GetMapping("/my-wallet")
    public ApiResponse<Wallet> getWallet() {
        var user =  userService.getCurrentUser();
        Wallet wallet = walletService.getWalletByUserId(user.getId());
        return ApiResponse.success(wallet, "Wallet retrieved successfully");
    }

    // Internal endpoint để deposit tiền cho designer khi order thành công
    @PostMapping("/internal/deposit-for-designer")
    public ApiResponse<Transaction> depositForDesigner(
            @RequestParam Long designerId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description) {
        try {
            Wallet wallet = walletService.getWalletByUserId(designerId);
            if (wallet == null) {
                // Tạo wallet nếu chưa có
                wallet = walletService.createWallet(designerId);
            }
            Transaction transaction = walletService.deposit(wallet.getWalletId(), amount);
            return ApiResponse.success(transaction, "Deposit for designer successful");
        } catch (Exception e) {
            throw new RuntimeException("Failed to deposit for designer: " + e.getMessage(), e);
        }
    }

}


package com.sketchnotes.identityservice.controller;


import com.sketchnotes.identityservice.dto.ApiResponse;
import com.sketchnotes.identityservice.model.Wallet;
import com.sketchnotes.identityservice.service.interfaces.IUserService;
import com.sketchnotes.identityservice.service.interfaces.IWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}


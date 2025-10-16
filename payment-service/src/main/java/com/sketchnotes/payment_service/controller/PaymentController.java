package com.sketchnotes.payment_service.controller;

import com.sketchnotes.payment_service.clients.IdentityClient;
import com.sketchnotes.payment_service.dtos.UserResponse;
import com.sketchnotes.payment_service.entity.Wallet;
import com.sketchnotes.payment_service.service.WalletService;
import com.sketchnotes.payment_service.service.implement.PayOSServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.type.Webhook;

import java.math.BigDecimal;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PayOSServiceImpl payOSService;
    private final IdentityClient identityClient;
    private final WalletService walletService;

    @PostMapping("/deposit")
    public String deposit(@RequestParam BigDecimal amount) {
        var apiResponse = identityClient.getCurrentUser();
        UserResponse user = apiResponse.getResult();

        if (user == null || user.getId() == null) {
            throw new RuntimeException("User not authenticated or invalid token!");
        }

        Wallet wallet = walletService.getWalletByUserId(user.getId());
        if (wallet == null) {
            throw new RuntimeException("Wallet not found for this user!");
        }


        return payOSService.createPaymentLink(wallet.getWalletId(), amount, "Deposit to wallet " + wallet.getWalletId());
    }

    @PostMapping("/callback")
    public ResponseEntity<String> callback(@RequestBody Webhook webhook) {
        payOSService.handleCallback(webhook);
        return ResponseEntity.ok("OK");
    }
}


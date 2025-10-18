package com.sketchnotes.payment_service.controller;

import com.sketchnotes.payment_service.clients.IdentityClient;
import com.sketchnotes.payment_service.dtos.PayosWebhookDTO;
import com.sketchnotes.payment_service.dtos.PayosWebhookDataDTO;
import com.sketchnotes.payment_service.dtos.UserResponse;
import com.sketchnotes.payment_service.entity.Wallet;
import com.sketchnotes.payment_service.service.WalletService;
import com.sketchnotes.payment_service.service.implement.PayOSServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.sketchnotes.payment_service.dtos.ApiResponse;
import org.springframework.web.bind.annotation.*;
import vn.payos.type.Webhook;

import java.math.BigDecimal;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
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

    @PostMapping("/payos/webhook")
    public ResponseEntity<ApiResponse<Object>> receivePayosPaymentWebhook(@RequestBody Webhook webhookRequest) {
        try {
            payOSService.handleCallback(webhookRequest); // truyền toàn bộ object, không chỉ data
                ApiResponse<Object> resp = ApiResponse.success("OK", "Webhook processed");
                return ResponseEntity.ok(resp);
        } catch (Exception e) {
                log.error("Error processing PayOS webhook", e);
                // Always return HTTP 200 but with error details in ApiResponse
                ApiResponse<Object> err = ApiResponse.<Object>builder()
                        .code(500)
                        .message("Error processing webhook")
                        .result(e.getMessage())
                        .build();
                return ResponseEntity.ok(err);
        }
    }
}


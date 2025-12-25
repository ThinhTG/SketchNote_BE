package com.sketchnotes.identityservice.controller;


import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.model.Wallet;
import com.sketchnotes.identityservice.service.interfaces.IPendingTransactionCleanupService;
import com.sketchnotes.identityservice.service.interfaces.IPaymentGatewayService;
import com.sketchnotes.identityservice.service.interfaces.IUserService;
import com.sketchnotes.identityservice.service.interfaces.IWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.model.webhooks.ConfirmWebhookResponse;


import java.math.BigDecimal;
import java.util.Map;


@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final IPaymentGatewayService payOSService;
    private final IWalletService walletService;
    private final IUserService userService;
    private final PayOS payOS;
    private final IPendingTransactionCleanupService cleanupService;

    @PostMapping("/deposit")
    public ApiResponse<String> deposit(@RequestParam BigDecimal amount) {
       var user = userService.getCurrentUser();
        if (user == null || user.getId() == null) {
            throw new RuntimeException("User not authenticated or invalid token!");
        }

        Wallet wallet = walletService.getWalletByUserId(user.getId());
        if (wallet == null) {
            throw new RuntimeException("Wallet not found for this user!");
        }

        String paymentLink = payOSService.createPaymentLink(wallet.getWalletId(), amount, "Deposit to wallet " + wallet.getWalletId());
        return ApiResponse.success("ok", paymentLink);
    }

    @PostMapping("/payos/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> requestBody) {
        return payOSService.handleWebhook(requestBody);
    }



    @PostMapping(path = "/confirm-webhook")
    public ApiResponse<ConfirmWebhookResponse> confirmWebhook(
            @RequestBody Map<String, String> requestBody) {
        try {
            ConfirmWebhookResponse result = payOS.webhooks().confirm(requestBody.get("webhookUrl"));
            return ApiResponse.success2("ok", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(e.getMessage());
        }
    }
    

    @PostMapping("/admin/cleanup-pending")
    public ApiResponse<String> manualCleanupPendingTransactions() {
        log.info("Manual cleanup of pending transactions triggered");
        cleanupService.cleanupPendingTransactions();
        return ApiResponse.success("Cleanup completed", "Pending transactions have been processed");
    }

}


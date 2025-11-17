package com.sketchnotes.identityservice.controller;


import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.model.Wallet;
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

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final IPaymentGatewayService payOSService;
    private final IWalletService walletService;
    private final IUserService userService;
    private final PayOS payOS;

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

//    @PostMapping("/payos/webhook")
//    public ResponseEntity<ApiResponse<Object>> receivePayosPaymentWebhook(@RequestBody Webhook webhookRequest) {
//        try {
//            payOSService.handleCallback(webhookRequest); // truyền toàn bộ object, không chỉ data
//                ApiResponse<Object> resp = ApiResponse.success("OK", "Webhook processed");
//                return ResponseEntity.ok(resp);
//        } catch (Exception e) {
//                log.error("Error processing PayOS webhook", e);
//                // Always return HTTP 200 but with error details in ApiResponse
//                ApiResponse<Object> err = ApiResponse.<Object>builder()
//                        .code(500)
//                        .message("Error processing webhook")
//                        .result(e.getMessage())
//                        .build();
//                return ResponseEntity.ok(err);
//        }
//    }


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

}


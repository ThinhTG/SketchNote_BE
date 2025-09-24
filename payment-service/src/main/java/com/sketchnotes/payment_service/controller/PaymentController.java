package com.sketchnotes.payment_service.controller;

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

    @PostMapping("/deposit")
    public String deposit(@RequestParam Long walletId, @RequestParam BigDecimal amount) {
        return payOSService.createPaymentLink(walletId, amount, "Deposit to wallet " + walletId);
    }

    @PostMapping("/callback")
    public ResponseEntity<String> callback(@RequestBody Webhook webhook) {
        payOSService.handleCallback(webhook);
        return ResponseEntity.ok("OK");
    }
}

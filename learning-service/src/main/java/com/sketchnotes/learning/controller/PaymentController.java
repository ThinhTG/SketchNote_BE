package com.sketchnotes.learning.controller;

import com.sketchnotes.learning.client.IdentityClient;
import com.sketchnotes.learning.dto.ApiResponse;
import com.sketchnotes.learning.dto.EnrollmentDTO;
import com.sketchnotes.learning.dto.RetryPaymentRequest;
import com.sketchnotes.learning.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final EnrollmentService enrollmentService;
    private final IdentityClient identityClient;

    @GetMapping("/failed")
    public ResponseEntity<ApiResponse<List<EnrollmentDTO>>> getFailedPayments() {
        Long userId = identityClient.getCurrentUser().getResult().getId();
        List<EnrollmentDTO> failedPayments = enrollmentService.getFailedPayments(userId);
        return ResponseEntity.ok(ApiResponse.success(failedPayments, "Retrieved failed payments successfully"));
    }

    @PostMapping("/retry")
    public ResponseEntity<ApiResponse<EnrollmentDTO>> retryPayment(@RequestBody RetryPaymentRequest request) {
        Long userId = identityClient.getCurrentUser().getResult().getId();
        EnrollmentDTO enrollment = enrollmentService.retryPayment(userId, request);
        return ResponseEntity.ok(ApiResponse.success(enrollment, "Payment processed successfully"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, ex.getMessage(), null));
    }
}
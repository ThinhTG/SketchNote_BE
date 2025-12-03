package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.dtos.request.PurchaseCreditRequest;
import com.sketchnotes.identityservice.dtos.request.UseCreditRequest;
import com.sketchnotes.identityservice.dtos.response.CreditBalanceResponse;
import com.sketchnotes.identityservice.dtos.response.CreditTransactionResponse;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.service.interfaces.ICreditService;
import com.sketchnotes.identityservice.service.interfaces.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các API liên quan đến AI Credits
 * Base path: /api/credits
 */
@RestController
@RequestMapping("/api/credits")
@RequiredArgsConstructor
@Slf4j
public class CreditController {
    
    private final ICreditService creditService;
    private final IUserRepository userRepository;
    private final IUserService userService;
    
    /**
     * Lấy thông tin số dư credit của user hiện tại
     * GET /api/credits/balance
     */
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<CreditBalanceResponse>> getCreditBalance(
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        log.info("Getting credit balance for user: {}", userId);
        
        CreditBalanceResponse response = creditService.getCreditBalance(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Credit balance retrieved successfully"));
    }
    
    /**
     * Mua credit
     * POST /api/credits/purchase
     */
    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse<CreditBalanceResponse>> purchaseCredits(
            Authentication authentication,
            @Valid @RequestBody PurchaseCreditRequest request) {
        
        Long userId = extractUserId(authentication);
        log.info("User {} purchasing credits: {}", userId, request.getAmount());
        
        CreditBalanceResponse response = creditService.purchaseCredits(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, 
                "Successfully purchased " + request.getAmount() + " credits"));
    }
    
    /**
     * Sử dụng credit (API này được gọi từ các service khác qua Feign Client)
     * POST /api/credits/use
     */
    @PostMapping("/use")
    public ResponseEntity<ApiResponse<CreditBalanceResponse>> useCredits(
            @Valid @RequestBody UseCreditRequest request) {

        Long userId = userService.getCurrentUser().getId();
        
        log.info("Using credits for user {}: {} credits", userId, request.getAmount());
        
        CreditBalanceResponse response = creditService.useCredits(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, 
                "Successfully used " + request.getAmount() + " credits"));
    }
    
    /**
     * Kiểm tra xem user có đủ credit không
     * GET /api/credits/check?amount=10
     */
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> checkCredits(
            Authentication authentication,
            @RequestParam Integer amount) {
        
        Long userId = extractUserId(authentication);
        log.info("Checking if user {} has {} credits", userId, amount);
        
        boolean hasEnough = creditService.hasEnoughCredits(userId, amount);
        return ResponseEntity.ok(ApiResponse.success(hasEnough, 
                hasEnough ? "Sufficient credits" : "Insufficient credits"));
    }
    
    /**
     * Lấy lịch sử giao dịch credit
     * GET /api/credits/history?page=0&size=10
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<CreditTransactionResponse>>> getCreditHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long userId = extractUserId(authentication);
        log.info("Getting credit history for user {}: page={}, size={}", userId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CreditTransactionResponse> response = creditService.getCreditHistory(userId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Credit history retrieved successfully"));
    }
    
    /**
     * Helper method để extract userId từ JWT token
     */
    private Long extractUserId(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        
        // Get 'sub' claim (Keycloak user ID)
        String keycloakId = jwt.getSubject();
        if (keycloakId == null || keycloakId.isBlank()) {
            log.error("Cannot extract 'sub' claim from JWT. Available claims: {}", jwt.getClaims().keySet());
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        
        // Query User by keycloakId
        return userRepository.findByKeycloakId(keycloakId)
                .map(user -> {
                    log.debug("User found for keycloakId: {} -> userId: {}", keycloakId, user.getId());
                    return user.getId();
                })
                .orElseThrow(() -> {
                    log.error("User not found for keycloakId: {}", keycloakId);
                    return new AppException(ErrorCode.UNAUTHENTICATED);
                });
    }
}

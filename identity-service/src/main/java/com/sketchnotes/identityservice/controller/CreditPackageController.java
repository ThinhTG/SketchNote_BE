package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.dtos.request.CreditPackageRequest;
import com.sketchnotes.identityservice.dtos.response.CreditPackageResponse;
import com.sketchnotes.identityservice.dtos.response.PurchasePackageResponse;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.service.interfaces.INotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý các API liên quan đến Credit Packages
 * Base path: /api/credit-packages
 */
@RestController
@RequestMapping("/api/credit-packages")
@RequiredArgsConstructor
@Slf4j
public class CreditPackageController {
    
    private final INotificationService.ICreditPackageService creditPackageService;
    private final IUserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CreditPackageResponse>>> getActivePackages() {
        List<CreditPackageResponse> packages = creditPackageService.getActivePackages();
        return ResponseEntity.ok(ApiResponse.success(packages, "Credit packages retrieved successfully"));
    }
    

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CreditPackageResponse>> getPackageById(@PathVariable Long id) {
        CreditPackageResponse response = creditPackageService.getPackageById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Credit package retrieved successfully"));
    }

    /**
     * Mua gói credit package (User)
     * POST /api/credit-packages/{packageId}/purchase
     * Thanh toán bằng wallet, cộng credits vào tài khoản user
     */
    @PostMapping("/{packageId}/purchase")
    public ResponseEntity<ApiResponse<PurchasePackageResponse>> purchasePackage(
            Authentication authentication,
            @PathVariable Long packageId) {
        
        Long userId = extractUserId(authentication);
        log.info("User {} purchasing credit package: {}", userId, packageId);
        
        PurchasePackageResponse response = creditPackageService.purchasePackage(userId, packageId);
        
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }
    
    // ==================== ADMIN APIs ====================
    
    /**
     * Lấy tất cả gói credit (Admin)
     * GET /api/credit-packages/admin/all
     */
    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<List<CreditPackageResponse>>> getAllPackages() {
        log.info("Admin: Getting all credit packages");
        List<CreditPackageResponse> packages = creditPackageService.getAllPackages();
        return ResponseEntity.ok(ApiResponse.success(packages, "All credit packages retrieved successfully"));
    }
    
    /**
     * Tạo gói credit mới (Admin)
     * POST /api/credit-packages/admin
     */
    @PostMapping("/admin")
    public ResponseEntity<ApiResponse<CreditPackageResponse>> createPackage(
            @Valid @RequestBody CreditPackageRequest request) {
        log.info("Admin: Creating new credit package: {}", request.getName());
        CreditPackageResponse response = creditPackageService.createPackage(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Credit package created successfully"));
    }
    
    /**
     * Cập nhật gói credit (Admin)
     * PUT /api/credit-packages/admin/{id}
     */
    @PutMapping("/admin/{id}")
    public ResponseEntity<ApiResponse<CreditPackageResponse>> updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody CreditPackageRequest request) {
        log.info("Admin: Updating credit package ID: {}", id);
        CreditPackageResponse response = creditPackageService.updatePackage(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Credit package updated successfully"));
    }
    
    /**
     * Xóa gói credit (Admin) - Soft delete
     * DELETE /api/credit-packages/admin/{id}
     */
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<ApiResponse<String>> deletePackage(@PathVariable Long id) {
        log.info("Admin: Deleting credit package ID: {}", id);
        creditPackageService.deletePackage(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Credit package deleted successfully"));
    }
    
    /**
     * Toggle trạng thái active của gói credit (Admin)
     * PATCH /api/credit-packages/admin/{id}/toggle
     */
    @PatchMapping("/admin/{id}/toggle")
    public ResponseEntity<ApiResponse<CreditPackageResponse>> togglePackageStatus(@PathVariable Long id) {
        log.info("Admin: Toggling status for credit package ID: {}", id);
        CreditPackageResponse response = creditPackageService.togglePackageStatus(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Credit package status toggled successfully"));
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Helper method để extract userId từ JWT token
     */
    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            log.error("Authentication is null or principal is null");
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        
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

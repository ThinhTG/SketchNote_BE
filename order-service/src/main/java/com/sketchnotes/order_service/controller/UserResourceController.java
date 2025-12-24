package com.sketchnotes.order_service.controller;

import com.sketchnotes.order_service.client.IdentityClient;
import com.sketchnotes.order_service.dtos.ApiResponse;
import com.sketchnotes.order_service.dtos.PagedResponseDTO;
import com.sketchnotes.order_service.dtos.PurchasedTemplateDTO;
import com.sketchnotes.order_service.entity.UserResource;
import com.sketchnotes.order_service.dtos.ResourceTemplateDTO;
import com.sketchnotes.order_service.service.UserResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders/user_resources")
@RequiredArgsConstructor
@Tag(name = "User Resources", description = "APIs for managing user purchased resources")
public class UserResourceController {
    private final UserResourceService userResourceService;
    private final IdentityClient identityClient;

    /**
     * 📦 [GET] Lấy tất cả resource mà user đang sở hữu (library của họ)
     */
    @GetMapping("/user/me")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<PagedResponseDTO<UserResource>>> getUserResources(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var user = identityClient.getCurrentUser();
        Page<UserResource> resources = userResourceService.getUserResources(user.getResult().getId(), PageRequest.of(page, size));

        PagedResponseDTO<UserResource> paged = PagedResponseDTO.<UserResource>builder()
                .content(resources.getContent())
                .page(resources.getNumber())
                .size(resources.getSize())
                .totalElements(resources.getTotalElements())
                .totalPages(resources.getTotalPages())
                .first(resources.isFirst())
                .last(resources.isLast())
                .hasNext(resources.hasNext())
                .hasPrevious(resources.hasPrevious())
                .build();

        return ResponseEntity.ok(ApiResponse.success(paged, "Fetched user resources"));
    }

    /**
     * 📦 [GET] Lấy danh sách ResourceTemplate mà user đã mua (bao gồm các itemUrl)dv
     * @deprecated Use /user/me/templates/v2 for better version support
     */
    @Deprecated
    @GetMapping("/user/me/templates")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<ResourceTemplateDTO>>> getMyPurchasedTemplates() {
        var user = identityClient.getCurrentUser();
        List<ResourceTemplateDTO> templates = userResourceService.getPurchasedTemplates(user.getResult().getId());
        return ResponseEntity.ok(ApiResponse.success(templates, "Fetched purchased templates"));
    }

    /**
     * 📦 [GET] Lấy danh sách ResourceTemplate mà user đã mua với thông tin version đầy đủ
     * - User sẽ thấy version đã mua (purchasedVersion)
     * - User sẽ thấy version mới nhất (currentVersion) nếu có
     * - User có quyền truy cập tất cả version từ lúc mua trở đi (free upgrade)
     */
    @Operation(
        summary = "Get purchased templates with version info",
        description = "Returns all templates purchased by the user with full version information. " +
                      "Users can access their purchased version plus all newer versions (free upgrade)."
    )
    @GetMapping("/user/me/templates/v2")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<PurchasedTemplateDTO>>> getMyPurchasedTemplatesWithVersions() {
        var user = identityClient.getCurrentUser();
        List<PurchasedTemplateDTO> templates = userResourceService.getPurchasedTemplatesWithVersions(user.getResult().getId());
        return ResponseEntity.ok(ApiResponse.success(templates, "Fetched purchased templates with version info"));
    }

    /**
     * 🛒 [POST] Thêm mới user_resource (sử dụng khi test hoặc admin muốn thêm thủ công)
     * Trong thực tế, Kafka consumer sẽ tạo tự động sau khi payment success.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResource>> createUserResource(
            @RequestParam Long orderId,
            @RequestParam Long userId,
            @RequestParam Long resourceTemplateId
    ) {
        UserResource newResource = userResourceService.createUserResource(orderId, userId, resourceTemplateId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(newResource, "User resource created"));
    }
    
    /**
     * Check if user has purchased a specific resource
     * This endpoint is used by identity-service to validate feedback eligibility
     */
    @GetMapping("/user/{userId}/resource/{resourceId}")
    public ResponseEntity<ApiResponse<UserResource>> getUserResource(
            @PathVariable Long userId,
            @PathVariable Long resourceId) {
        UserResource userResource = userResourceService.getUserResourceByUserIdAndResourceId(userId, resourceId);
        return ResponseEntity.ok(ApiResponse.success(userResource, "User resource retrieved successfully"));
    }
    
    /**
     * 🔄 [POST] Upgrade user's resource to the latest published version (FREE)
     * 
     * Business rules:
     * - Only the resource owner can upgrade
     * - Upgrade is FREE (no payment required, no new order created)
     * - Only updates for the specific user (not globally)
     * - User must own the resource (active)
     * - There must be a newer version available
     */
    @Operation(
        summary = "Upgrade to latest version (free)",
        description = "Allows user to upgrade their purchased resource to the latest published version for free. " +
                      "This only updates for the specific user, no payment or new order is created."
    )
    @PostMapping("/user/me/resource/{resourceTemplateId}/upgrade")
    @Transactional
    public ResponseEntity<ApiResponse<UserResource>> upgradeToLatestVersion(
            @PathVariable Long resourceTemplateId) {
        var user = identityClient.getCurrentUser();
        Long userId = user.getResult().getId();
        
        UserResource upgraded = userResourceService.upgradeToLatestVersion(userId, resourceTemplateId);
        return ResponseEntity.ok(ApiResponse.success(upgraded, "Resource upgraded to latest version successfully"));
    }
    
    /**
     * 🔍 [GET] Check if there's a newer version available for the user's resource
     */
    @Operation(
        summary = "Check for newer version",
        description = "Check if there is a newer version available for a specific resource owned by the user."
    )
    @GetMapping("/user/me/resource/{resourceTemplateId}/check-upgrade")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Boolean>> checkForNewerVersion(
            @PathVariable Long resourceTemplateId) {
        var user = identityClient.getCurrentUser();
        Long userId = user.getResult().getId();
        
        boolean hasNewer = userResourceService.hasNewerVersionAvailable(userId, resourceTemplateId);
        return ResponseEntity.ok(ApiResponse.success(hasNewer, 
                hasNewer ? "A newer version is available" : "You are using the latest version"));
    }

}

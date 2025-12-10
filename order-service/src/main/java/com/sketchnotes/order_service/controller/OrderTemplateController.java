package com.sketchnotes.order_service.controller;

import com.sketchnotes.order_service.client.IdentityClient;
import com.sketchnotes.order_service.dtos.*;
import com.sketchnotes.order_service.dtos.designer.ResourceTemplateVersionDTO;
import com.sketchnotes.order_service.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/orders/template")
@RequiredArgsConstructor
public class OrderTemplateController {

    private final TemplateService templateService;
    private final IdentityClient identityClient;

    /**
     * Lấy tất cả template đang active và đã được PUBLISHED với pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDTO<ResourceTemplateDTO>>> getAllActiveTemplates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Long currentUserId = getCurrentUserId();
        var result = templateService.getAllActiveTemplates(page, size, sortBy, sortDir, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(result, "Fetched templates"));
    }

    /**
     * Lấy template theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResourceTemplateDTO>> getTemplateById(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        var result = templateService.getTemplateById(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(result, "Fetched template"));
    }

    /**
     * Lấy template theo designer ID với pagination
     * chức năng này cho Customer muốn xem các design của 1 designer nào đó
     */
    @GetMapping("/designer/{designerId}")
    public ResponseEntity<ApiResponse<PagedResponseDTO<ResourceTemplateDTO>>> getTemplatesByDesignerPaged(
            @PathVariable Long designerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = getCurrentUserId();
        var result = templateService.getTemplatesByDesigner(designerId, page, size, "createdAt", "desc", currentUserId);
        return ResponseEntity.ok(ApiResponse.success(result, "Fetched templates by designer"));
    }

    /**
     * Lấy template theo designer ID với pagination
     * Chức năng này để Designer xem các design của bản thân
     * Có thể lọc theo status: PENDING_REVIEW, PUBLISHED, REJECTED
     * Nếu không truyền status sẽ lấy tất cả
     */
    @GetMapping("/my-template")
    public ResponseEntity<ApiResponse<PagedResponseDTO<ResourceTemplateDTO>>> getMyTemplatesPaged(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UserResponse user = getCurrentUser();
        var result = templateService.getTemplatesByDesignerAndStatus(user.getId(), status, page, size, "createdAt", "desc");
        return ResponseEntity.ok(ApiResponse.success(result, "Fetched My templates"));
    }

    /**
     * Lấy template theo loại với pagination
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<PagedResponseDTO<ResourceTemplateDTO>>> getTemplatesByTypePaged(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = getCurrentUserId();
        var result = templateService.getTemplatesByType(type, page, size, "createdAt", "desc", currentUserId);
        return ResponseEntity.ok(ApiResponse.success(result, "Fetched templates by type"));
    }

    /**
     * Tìm kiếm template với pagination
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponseDTO<ResourceTemplateDTO>>> searchTemplates(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = getCurrentUserId();
        var result = templateService.searchTemplates(keyword, page, size, "createdAt", "desc", currentUserId);
        return ResponseEntity.ok(ApiResponse.success(result, "Search results"));
    }

    /**
     * Lấy template phổ biến nhất
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<ResourceTemplateDTO>>> getPopularTemplates(
            @RequestParam(defaultValue = "10") int limit) {
        Long currentUserId = getCurrentUserId();
        var result = templateService.getPopularTemplates(limit, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(result, "Popular templates"));
    }

    /**
     * Lấy template mới nhất
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<ResourceTemplateDTO>>> getLatestTemplates(
            @RequestParam(defaultValue = "10") int limit) {
        Long currentUserId = getCurrentUserId();
        var result = templateService.getLatestTemplates(limit, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(result, "Latest templates"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ResourceTemplateDTO>> createTemplate(@RequestBody TemplateCreateUpdateDTO dto) {
        UserResponse user = getCurrentUser();
        if (user.getRole() == null || !"DESIGNER".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only designers can create templates");
        }
        dto.setDesignerId(user.getId());
        ResourceTemplateDTO created = templateService.createTemplate(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Template created and pending review"));
    }

    /**
     * Xác nhận template và chuyển trạng thái từ PENDING_REVIEW sang PUBLISHED
     * Chỉ staff mới có quyền thực hiện chức năng này
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<ResourceTemplateDTO>> confirmTemplate(@PathVariable Long id) {
        ensureStaff();
        ResourceTemplateDTO confirmed = templateService.confirmTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(confirmed, "Template confirmed and published"));
    }

    /**
     * Từ chối template và chuyển trạng thái sang REJECTED
     * Chỉ staff mới có quyền thực hiện chức năng này
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ResourceTemplateDTO>> rejectTemplate(@PathVariable Long id) {
        ensureStaff();
        ResourceTemplateDTO rejected = templateService.rejectTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(rejected, "Template rejected"));
    }

    /**
     * Lấy template theo trạng thái review (PENDING_REVIEW, PUBLISHED, REJECTED)
     */
    @GetMapping("/review-status/{status}")
    public ResponseEntity<ApiResponse<PagedResponseDTO<ResourceTemplateDTO>>> getTemplatesByReviewStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        ensureStaff();
        var result = templateService.getTemplatesByReviewStatus(status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(result, "Fetched templates by review status"));
    }

    /**
     * Staff: danh sách version đang PENDING_REVIEW
     */
    @GetMapping("/review/versions")
    public ResponseEntity<ApiResponse<PagedResponseDTO<ResourceTemplateVersionDTO>>> getPendingVersions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        ensureStaff();
        var result = templateService.getPendingVersions(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(result, "Fetched pending versions"));
    }

    /**
     * Staff: duyệt hoặc từ chối một version
     */
    @PostMapping("/versions/{versionId}/review")
    public ResponseEntity<ApiResponse<ResourceTemplateVersionDTO>> reviewVersion(
            @PathVariable Long versionId,
            @RequestBody VersionReviewRequest request) {
        ensureStaff();
        UserResponse user = getCurrentUser();

        if (request.getApprove() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "approve is required (true/false)");
        }
        if (!request.getApprove() && (request.getReviewComment() == null || request.getReviewComment().trim().isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reviewComment is required when rejecting");
        }

        ResourceTemplateVersionDTO reviewed = templateService.reviewVersion(
                versionId,
                user.getId(),
                request.getApprove(),
                request.getReviewComment());

        return ResponseEntity.ok(ApiResponse.success(reviewed,
                request.getApprove() ? "Version approved" : "Version rejected"));
    }

    @PostMapping("/sell/{projectId}")
    public ResponseEntity<ApiResponse<ResourceTemplateDTO>> sellProjectAsTemplate(
            @PathVariable Long projectId,
            @RequestBody TemplateSellDTO dto) {
        UserResponse user = getCurrentUser();

        if (user.getRole() == null || !"DESIGNER".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only designers can sell projects as templates");
        }

        ResourceTemplateDTO created = templateService.createTemplateFromProject(projectId, user.getId(), dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Template created from project and pending review"));
    }

    // ==================== Private Helper Methods ====================

    /**
     * Get current user ID safely (returns null if not authenticated)
     */
    private Long getCurrentUserId() {
        try {
            var userResponse = identityClient.getCurrentUser();
            if (userResponse != null && userResponse.getResult() != null) {
                return userResponse.getResult().getId();
            }
        } catch (Exception e) {
            // User not authenticated or error getting user info
        }
        return null;
    }

    /**
     * Get current user (throws exception if not authenticated)
     */
    private UserResponse getCurrentUser() {
        try {
            ApiResponse<UserResponse> apiResponse = identityClient.getCurrentUser();
            if (apiResponse != null && apiResponse.getResult() != null) {
                return apiResponse.getResult();
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Failed to authenticate user");
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
    }

    /**
     * Ensure current user is a staff member
     */
    private void ensureStaff() {
        UserResponse user = getCurrentUser();
        if (user.getRole() == null || !"STAFF".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only staff can access this endpoint");
        }
    }
}

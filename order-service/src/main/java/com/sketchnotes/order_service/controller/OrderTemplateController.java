package com.sketchnotes.order_service.controller;

import com.sketchnotes.order_service.client.IdentityClient;
import com.sketchnotes.order_service.dtos.*;
import com.sketchnotes.order_service.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders/template")
@RequiredArgsConstructor
public class OrderTemplateController {

    private final TemplateService templateService;
    private final IdentityClient  identityClient;

    /**
     * Lấy tất cả template đang active và đã được PUBLISHED với pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDTO<ResourceTemplateDTO>>> getAllActiveTemplates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        var result = templateService.getAllActiveTemplates(page, size, sortBy, sortDir);

        //  Gán thông tin designer vào từng ResourceTemplateDTO
        if (result != null && result.getContent() != null && !result.getContent().isEmpty()) {
            result.getContent().forEach(template -> {
                var apiResponse = identityClient.getUser(template.getDesignerId());
                UserResponse user = apiResponse.getResult();
                DesignerInfoDTO designerInfo = new DesignerInfoDTO();
                designerInfo.setEmail(user.getEmail());
                designerInfo.setFirstName(user.getFirstName());
                designerInfo.setLastName(user.getLastName());
                designerInfo.setAvatarUrl(user.getAvatarUrl());
                template.setDesignerInfo(designerInfo);
            });
        }
        return ResponseEntity.ok(ApiResponse.success(result, "Fetched templates"));
    }

    /**
     * Lấy template theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResourceTemplateDTO>> getTemplateById(@PathVariable Long id) {
        var result = templateService.getTemplateById(id);
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
        var result = templateService.getTemplatesByDesigner(designerId, page, size, "createdAt", "desc");
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

        // Lấy thông tin user hiện tại từ Identity Service
        var apiResponse = identityClient.getCurrentUser();
        UserResponse user = apiResponse.getResult();

        // Gọi service để lấy danh sách template theo designerId và status
        var result = templateService.getTemplatesByDesignerAndStatus(user.getId(), status, page, size, "createdAt", "desc");

        //  Gán thông tin designer vào từng ResourceTemplateDTO
        if (result != null && result.getContent() != null && !result.getContent().isEmpty()) {
            result.getContent().forEach(template -> {
                DesignerInfoDTO designerInfo = new DesignerInfoDTO();
                designerInfo.setEmail(user.getEmail());
                designerInfo.setFirstName(user.getFirstName());
                designerInfo.setLastName(user.getLastName());
                designerInfo.setAvatarUrl(user.getAvatarUrl());
                template.setDesignerInfo(designerInfo);
            });
        }
        // Trả về kết quả
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
        var result = templateService.getTemplatesByType(type, page, size, "createdAt", "desc");
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
        var result = templateService.searchTemplates(keyword, page, size, "createdAt", "desc");
        return ResponseEntity.ok(ApiResponse.success(result, "Search results"));
    }

    /**
     * Lấy template phổ biến nhất
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<ResourceTemplateDTO>>> getPopularTemplates(
            @RequestParam(defaultValue = "10") int limit) {
        var result = templateService.getPopularTemplates(limit);
        return ResponseEntity.ok(ApiResponse.success(result, "Popular templates"));
    }

    /**
     * Lấy template mới nhất
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<ResourceTemplateDTO>>> getLatestTemplates(
            @RequestParam(defaultValue = "10") int limit) {
        var result = templateService.getLatestTemplates(limit);
        return ResponseEntity.ok(ApiResponse.success(result, "Latest templates"));
    }


    @PostMapping
    public ResponseEntity<ApiResponse<ResourceTemplateDTO>> createTemplate(@RequestBody TemplateCreateUpdateDTO dto) {
        ApiResponse<UserResponse> apiResponse = identityClient.getCurrentUser();
        UserResponse user = apiResponse.getResult();
        if (user == null || user.getRole() == null || !"DESIGNER".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only designers can create templates");
        }
        dto.setDesignerId(user.getId());
        ResourceTemplateDTO created = templateService.createTemplate(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Template created and pending review"));
    }

    /**
     * Xác nhận template và chuyển trạng thái từ PENDING_REVIEW sang PUBLISHED
     * Chỉ staff mới có quyền thực hiện chức năng này ( chua co )
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<ResourceTemplateDTO>> confirmTemplate(@PathVariable Long id) {
        // TODO: Add staff role check here
        ResourceTemplateDTO confirmed = templateService.confirmTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(confirmed, "Template confirmed and published"));
    }

    /**
     * Từ chối template và chuyển trạng thái sang REJECTED
     * Chỉ staff mới có quyền thực hiện chức năng này ( chua co )
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ResourceTemplateDTO>> rejectTemplate(@PathVariable Long id) {
        // TODO: Add staff role check here
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
        // TODO: Add staff role check here
        var result = templateService.getTemplatesByReviewStatus(status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(result, "Fetched templates by review status"));
    }
}

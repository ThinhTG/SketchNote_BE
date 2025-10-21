package com.sketchnotes.order_service.controller;

import com.sketchnotes.order_service.client.IdentityClient;
import com.sketchnotes.order_service.dtos.*;
import com.sketchnotes.order_service.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders/template")
@RequiredArgsConstructor
public class OrderTemplateController {

    private final TemplateService templateService;
    private final IdentityClient  identityClient;

    /**
     * Lấy tất cả template đang active với pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDTO<ResourceTemplateDTO>>> getAllActiveTemplates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        var result = templateService.getAllActiveTemplates(page, size, sortBy, sortDir);
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
        dto.setDesignerId(user.getId());
        ResourceTemplateDTO created = templateService.createTemplate(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Template created"));
    }




}

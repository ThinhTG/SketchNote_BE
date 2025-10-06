package com.sketchnotes.order_service.controller;

import com.sketchnotes.order_service.dtos.PagedResponseDTO;
import com.sketchnotes.order_service.dtos.ResourceTemplateDTO;
import com.sketchnotes.order_service.dtos.TemplateCreateUpdateDTO;
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

    /**
     * Lấy tất cả template đang active
     */
    @GetMapping
    public ResponseEntity<List<ResourceTemplateDTO>> getAllActiveTemplates() {
        return ResponseEntity.ok(templateService.getAllActiveTemplates());
    }

    /**
     * Lấy tất cả template đang active với pagination
     */
    @GetMapping("/paged")
    public ResponseEntity<PagedResponseDTO<ResourceTemplateDTO>> getAllActiveTemplatesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(templateService.getAllActiveTemplates(page, size, sortBy, sortDir));
    }

    /**
     * Lấy template theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResourceTemplateDTO> getTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(templateService.getTemplateById(id));
    }

    /**
     * Lấy template theo designer ID
     */
    @GetMapping("/designer/{designerId}")
    public ResponseEntity<List<ResourceTemplateDTO>> getTemplatesByDesigner(@PathVariable Long designerId) {
        return ResponseEntity.ok(templateService.getTemplatesByDesigner(designerId));
    }

    /**
     * Lấy template theo designer ID với pagination
     */
    @GetMapping("/designer/{designerId}/paged")
    public ResponseEntity<PagedResponseDTO<ResourceTemplateDTO>> getTemplatesByDesignerPaged(
            @PathVariable Long designerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(templateService.getTemplatesByDesigner(designerId, page, size, sortBy, sortDir));
    }

    /**
     * Lấy template theo loại
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ResourceTemplateDTO>> getTemplatesByType(@PathVariable String type) {
        return ResponseEntity.ok(templateService.getTemplatesByType(type));
    }

    /**
     * Lấy template theo loại với pagination
     */
    @GetMapping("/type/{type}/paged")
    public ResponseEntity<PagedResponseDTO<ResourceTemplateDTO>> getTemplatesByTypePaged(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(templateService.getTemplatesByType(type, page, size, sortBy, sortDir));
    }

    /**
     * Tìm kiếm template theo từ khóa
     */
    @GetMapping("/search")
    public ResponseEntity<List<ResourceTemplateDTO>> searchTemplates(@RequestParam String keyword) {
        return ResponseEntity.ok(templateService.searchTemplates(keyword));
    }

    /**
     * Tìm kiếm template theo từ khóa với pagination
     */
    @GetMapping("/search/paged")
    public ResponseEntity<PagedResponseDTO<ResourceTemplateDTO>> searchTemplatesPaged(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(templateService.searchTemplates(keyword, page, size, sortBy, sortDir));
    }

    /**
     * Lấy template theo khoảng giá
     */
    @GetMapping("/price-range")
    public ResponseEntity<List<ResourceTemplateDTO>> getTemplatesByPriceRange(
            @RequestParam BigDecimal minPrice, 
            @RequestParam BigDecimal maxPrice) {
        return ResponseEntity.ok(templateService.getTemplatesByPriceRange(minPrice, maxPrice));
    }

    /**
     * Lấy template theo khoảng giá với pagination
     */
    @GetMapping("/price-range/paged")
    public ResponseEntity<PagedResponseDTO<ResourceTemplateDTO>> getTemplatesByPriceRangePaged(
            @RequestParam BigDecimal minPrice, 
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(templateService.getTemplatesByPriceRange(minPrice, maxPrice, page, size, sortBy, sortDir));
    }

    /**
     * Tạo template mới
     */
    @PostMapping
    public ResponseEntity<ResourceTemplateDTO> createTemplate(@RequestBody TemplateCreateUpdateDTO templateDTO) {
        ResourceTemplateDTO created = templateService.createTemplate(templateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Cập nhật template
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResourceTemplateDTO> updateTemplate(
            @PathVariable Long id, 
            @RequestBody TemplateCreateUpdateDTO templateDTO) {
        return ResponseEntity.ok(templateService.updateTemplate(id, templateDTO));
    }

    /**
     * Xóa template (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Kích hoạt/vô hiệu hóa template
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ResourceTemplateDTO> toggleTemplateStatus(@PathVariable Long id) {
        return ResponseEntity.ok(templateService.toggleTemplateStatus(id));
    }

    /**
     * Lấy template theo trạng thái
     */
    @GetMapping("/status/{isActive}")
    public ResponseEntity<List<ResourceTemplateDTO>> getTemplatesByStatus(@PathVariable Boolean isActive) {
        return ResponseEntity.ok(templateService.getTemplatesByStatus(isActive));
    }

    /**
     * Lấy template sắp hết hạn
     */
    @GetMapping("/expiring-soon")
    public ResponseEntity<List<ResourceTemplateDTO>> getTemplatesExpiringSoon(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(templateService.getTemplatesExpiringSoon(days));
    }

    /**
     * Lấy template mới nhất
     */
    @GetMapping("/latest")
    public ResponseEntity<List<ResourceTemplateDTO>> getLatestTemplates(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(templateService.getLatestTemplates(limit));
    }

    /**
     * Lấy template phổ biến nhất
     */
    @GetMapping("/popular")
    public ResponseEntity<List<ResourceTemplateDTO>> getPopularTemplates(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(templateService.getPopularTemplates(limit));
    }
}

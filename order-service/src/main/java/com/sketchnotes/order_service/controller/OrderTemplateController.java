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
     * Lấy tất cả template đang active với pagination
     */
    @GetMapping
    public ResponseEntity<PagedResponseDTO<ResourceTemplateDTO>> getAllActiveTemplates(
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
     * Lấy template theo designer ID với pagination
     */
    @GetMapping("/designer/{designerId}")
    public ResponseEntity<PagedResponseDTO<ResourceTemplateDTO>> getTemplatesByDesignerPaged(
            @PathVariable Long designerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(templateService.getTemplatesByDesigner(designerId, page, size, "createdAt", "desc"));
    }

    /**
     * Lấy template theo loại với pagination
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<PagedResponseDTO<ResourceTemplateDTO>> getTemplatesByTypePaged(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(templateService.getTemplatesByType(type, page, size, "createdAt", "desc"));
    }

    /**
     * Tìm kiếm template với pagination
     */
    @GetMapping("/search")
    public ResponseEntity<PagedResponseDTO<ResourceTemplateDTO>> searchTemplates(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(templateService.searchTemplates(keyword, page, size, "createdAt", "desc"));
    }

    /**
     * Lấy template phổ biến nhất
     */
    @GetMapping("/popular")
    public ResponseEntity<List<ResourceTemplateDTO>> getPopularTemplates(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(templateService.getPopularTemplates(limit));
    }

    /**
     * Lấy template mới nhất
     */
    @GetMapping("/latest")
    public ResponseEntity<List<ResourceTemplateDTO>> getLatestTemplates(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(templateService.getLatestTemplates(limit));
    }


    @PostMapping
    public ResponseEntity<ResourceTemplateDTO> createTemplate(@RequestBody TemplateCreateUpdateDTO dto) {
        ResourceTemplateDTO created = templateService.createTemplate(dto);
        // return 201 with Location header
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

}

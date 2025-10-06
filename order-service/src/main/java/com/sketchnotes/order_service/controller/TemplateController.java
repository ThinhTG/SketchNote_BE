package com.sketchnotes.order_service.controller;

import com.sketchnotes.order_service.dtos.ResourceTemplateDTO;
import com.sketchnotes.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<ResourceTemplateDTO>> getAllTemplates() {
        return ResponseEntity.ok(orderService.getAllTemplates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceTemplateDTO> getTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getTemplateById(id));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<ResourceTemplateDTO>> getTemplatesByType(@PathVariable String type) {
        return ResponseEntity.ok(orderService.getTemplatesByType(type));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResourceTemplateDTO>> searchTemplates(@RequestParam String keyword) {
        return ResponseEntity.ok(orderService.searchTemplates(keyword));
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<ResourceTemplateDTO>> getTemplatesByPriceRange(
            @RequestParam BigDecimal minPrice, 
            @RequestParam BigDecimal maxPrice) {
        return ResponseEntity.ok(orderService.getTemplatesByPriceRange(minPrice, maxPrice));
    }
}

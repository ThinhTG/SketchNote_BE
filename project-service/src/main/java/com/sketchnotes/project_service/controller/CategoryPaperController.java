package com.sketchnotes.project_service.controller;

import com.sketchnotes.project_service.dtos.ApiResponse;
import com.sketchnotes.project_service.dtos.request.CategoryPaperRequest;
import com.sketchnotes.project_service.dtos.response.CategoryPaperResponse;
import com.sketchnotes.project_service.enums.PaperType;
import com.sketchnotes.project_service.service.ICategoryPaperService;
import com.sketchnotes.project_service.utils.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/category-papers")
@RequiredArgsConstructor
@Tag(name = "Category Paper", description = "Category Paper Management APIs")
public class CategoryPaperController {
    
    private final ICategoryPaperService categoryPaperService;
    
    @PostMapping
    @Operation(summary = "Create a new category paper")
    public ApiResponse<CategoryPaperResponse> create(@Valid @RequestBody CategoryPaperRequest request) {
        CategoryPaperResponse response = categoryPaperService.create(request);
        return ApiResponse.<CategoryPaperResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Category paper created successfully")
                .result(response)
                .build();
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update a category paper by ID")
    public ApiResponse<CategoryPaperResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryPaperRequest request) {
        CategoryPaperResponse response = categoryPaperService.update(id, request);
        return ApiResponse.<CategoryPaperResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Category paper updated successfully")
                .result(response)
                .build();
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category paper by ID")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryPaperService.delete(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Category paper deleted successfully")
                .build();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get a category paper by ID")
    public ApiResponse<CategoryPaperResponse> getById(@PathVariable Long id) {
        CategoryPaperResponse response = categoryPaperService.getById(id);
        return ApiResponse.<CategoryPaperResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Category paper retrieved successfully")
                .result(response)
                .build();
    }
    
    @GetMapping
    @Operation(summary = "Get all category papers with pagination and filtering")
    public ApiResponse<PagedResponse<CategoryPaperResponse>> getAll(
            @RequestParam(required = false) PaperType paperType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<CategoryPaperResponse> response = categoryPaperService.getAll(paperType, keyword, page, size);
        return ApiResponse.<PagedResponse<CategoryPaperResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Category papers retrieved successfully")
                .result(response)
                .build();
    }
}

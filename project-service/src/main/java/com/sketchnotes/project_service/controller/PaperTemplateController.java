package com.sketchnotes.project_service.controller;

import com.sketchnotes.project_service.dtos.ApiResponse;
import com.sketchnotes.project_service.dtos.request.PaperTemplateRequest;
import com.sketchnotes.project_service.dtos.response.PaperTemplateResponse;
import com.sketchnotes.project_service.enums.PaperSize;
import com.sketchnotes.project_service.service.IPaperTemplateService;
import com.sketchnotes.project_service.utils.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/paper-templates")
@RequiredArgsConstructor
@Tag(name = "Paper Template", description = "Paper Template Management APIs")
public class PaperTemplateController {
    
    private final IPaperTemplateService paperTemplateService;
    
    @PostMapping
    @Operation(summary = "Create a new paper template")
    public ApiResponse<PaperTemplateResponse> create(@Valid @RequestBody PaperTemplateRequest request) {
        PaperTemplateResponse response = paperTemplateService.create(request);
        return ApiResponse.<PaperTemplateResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Paper template created successfully")
                .result(response)
                .build();
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update a paper template by ID")
    public ApiResponse<PaperTemplateResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PaperTemplateRequest request) {
        PaperTemplateResponse response = paperTemplateService.update(id, request);
        return ApiResponse.<PaperTemplateResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Paper template updated successfully")
                .result(response)
                .build();
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a paper template by ID")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        paperTemplateService.delete(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Paper template deleted successfully")
                .build();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get a paper template by ID")
    public ApiResponse<PaperTemplateResponse> getById(@PathVariable Long id) {
        PaperTemplateResponse response = paperTemplateService.getById(id);
        return ApiResponse.<PaperTemplateResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Paper template retrieved successfully")
                .result(response)
                .build();
    }
    
    @GetMapping
    @Operation(summary = "Get all paper templates with pagination and filtering")
    public ApiResponse<PagedResponse<PaperTemplateResponse>> getAll(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) PaperSize paperSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<PaperTemplateResponse> response = paperTemplateService.getAll(categoryId, paperSize, keyword, page, size);
        return ApiResponse.<PagedResponse<PaperTemplateResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Paper templates retrieved successfully" )
                .result(response)
                .build();
    }
}

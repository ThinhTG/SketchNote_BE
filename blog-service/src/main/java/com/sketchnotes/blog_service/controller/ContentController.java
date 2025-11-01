package com.sketchnotes.blog_service.controller;

import com.sketchnotes.blog_service.Service.ContentService;
import com.sketchnotes.blog_service.dtos.request.ContentRequest;
import com.sketchnotes.blog_service.dtos.response.ApiResponse;
import com.sketchnotes.blog_service.dtos.response.ContentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {
    private final ContentService contentService;
    @PostMapping(path = "/{blogId}")
    public ResponseEntity<ApiResponse<ContentResponse>> createContent(
            @PathVariable Long blogId,
            @Valid @RequestBody ContentRequest request) {
        ContentResponse response = contentService.createContent(blogId, request);
        return ResponseEntity.ok(ApiResponse.success( response,"create successful"));
    }

    @PutMapping(path = "/{contentId}")
    public ResponseEntity<ApiResponse<ContentResponse>> updateContent(
            @PathVariable Long contentId,
            @Valid @RequestBody ContentRequest request) {
        ContentResponse response = contentService.UpdateContent(contentId, request);
        return ResponseEntity.ok(ApiResponse.success( response,"update successful"));

    }

    @DeleteMapping("/{contentId}")
    public ResponseEntity<ApiResponse<String>> deleteContent(@PathVariable Long contentId) {
        contentService.deleteContent(contentId);
        return ResponseEntity.ok(ApiResponse.success( null,"Delete successful"));
    }
}

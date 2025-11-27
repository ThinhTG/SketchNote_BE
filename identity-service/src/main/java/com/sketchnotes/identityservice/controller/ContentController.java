package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.dtos.request.ContentRequest;
import com.sketchnotes.identityservice.dtos.response.ContentResponse;
import com.sketchnotes.identityservice.service.interfaces.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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

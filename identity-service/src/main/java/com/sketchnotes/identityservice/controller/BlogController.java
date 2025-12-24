package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.client.IdentityClient;
import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.dtos.request.BlogRequest;
import com.sketchnotes.identityservice.dtos.request.PublishRequest;
import com.sketchnotes.identityservice.dtos.request.UpdateBlogRequest;
import com.sketchnotes.identityservice.dtos.response.BlogModerationHistoryResponse;
import com.sketchnotes.identityservice.dtos.response.BlogResponse;
import com.sketchnotes.identityservice.dtos.response.ImageSafetyCheckResponse;
import com.sketchnotes.identityservice.enums.BlogStatus;
import com.sketchnotes.identityservice.service.implement.ContentModerationService;
import com.sketchnotes.identityservice.service.interfaces.BlogService;
import com.sketchnotes.identityservice.ultils.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogController {
    private final BlogService postService;
    private final ContentModerationService contentModerationService;

    @PostMapping
    public ResponseEntity<ApiResponse<BlogResponse>> create(@RequestBody BlogRequest req){
        BlogResponse response = postService.createBlog(req);
        return ResponseEntity.ok(ApiResponse.success( response,"create successful"));
    }

    @GetMapping("/my-blog")
    public ResponseEntity<ApiResponse<List<BlogResponse>>> get(){
        List<BlogResponse> response = postService.getMyBlogs();
        return ResponseEntity.ok(ApiResponse.success( response,"Get data successful"));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogResponse>> getBlogById(@PathVariable Long id){
        BlogResponse response = postService.getBlog(id);
        return ResponseEntity.ok(ApiResponse.success( response,"Get data successful"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<BlogResponse>>> list(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "PUBLISHED") BlogStatus status){
        PagedResponse<BlogResponse> response = postService.getAll(pageNo, pageSize, status);
        return ResponseEntity.ok(ApiResponse.success( response,"Get data successful"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogResponse>> update(@PathVariable Long id,  @RequestBody UpdateBlogRequest req){
        BlogResponse response = postService.updateBlog(id, req);
        return ResponseEntity.ok(ApiResponse.success( response,"Update successful"));
    }

    @Operation(
            summary = "Publish a blog",
            description = "Change blog status to PUBLISHED, DRAFT, or ARCHIVED"
    )
    @PutMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<BlogResponse>> publish(@PathVariable Long id,   @RequestBody PublishRequest request){
        BlogResponse response = postService.publishBlog(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success( response,"Publish blog successful"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id){
        postService.deleteBlog(id);
        return ResponseEntity.ok(ApiResponse.success( null,"Delete successful"));
    }

    @Operation(
            summary = "Get latest moderation history",
            description = "Get the most recent AI moderation check result for a blog"
    )
    @GetMapping("/{id}/moderation/latest")
    public ResponseEntity<ApiResponse<BlogModerationHistoryResponse>> getLatestModerationHistory(@PathVariable Long id) {
        BlogModerationHistoryResponse response = postService.getLatestModerationHistory(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Get latest moderation history successful"));
    }

    @Operation(
            summary = "Get all moderation history",
            description = "Get all AI moderation check history for a blog, ordered by most recent first"
    )
    @GetMapping("/{id}/moderation/history")
    public ResponseEntity<ApiResponse<List<BlogModerationHistoryResponse>>> getAllModerationHistory(@PathVariable Long id) {
        List<BlogModerationHistoryResponse> response = postService.getAllModerationHistory(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Get moderation history successful"));
    }

    @Operation(
            summary = "[TEST] Check image safety using Google Vision SafeSearch",
            description = "Test endpoint to upload an image file and check if it contains adult, violent, racy, or medical content using Google Cloud Vision API"
    )
    @PostMapping(value = "/test/image-safety", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ImageSafetyCheckResponse>> testImageSafety(
            @RequestParam("file") MultipartFile file) {
        ImageSafetyCheckResponse response = contentModerationService.testImageSafety(file);
        return ResponseEntity.ok(ApiResponse.success(response, "Image safety check completed successfully"));
    }
}

package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.client.IdentityClient;
import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.dtos.request.BlogRequest;
import com.sketchnotes.identityservice.dtos.request.UpdateBlogRequest;
import com.sketchnotes.identityservice.dtos.response.BlogResponse;
import com.sketchnotes.identityservice.enums.BlogStatus;
import com.sketchnotes.identityservice.service.interfaces.BlogService;
import com.sketchnotes.identityservice.ultils.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogController {
    private final BlogService postService;

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

    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<BlogResponse>> publish(@PathVariable Long id){
        BlogResponse response = postService.publishBlog(id);
        return ResponseEntity.ok(ApiResponse.success( response,"Publish blog successful"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id){
        postService.deleteBlog(id);
        return ResponseEntity.ok(ApiResponse.success( null,"Delete successful"));
    }
}

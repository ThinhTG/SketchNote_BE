package com.sketchnotes.blog_service.controller;


import com.sketchnotes.blog_service.Service.BlogService;
import com.sketchnotes.blog_service.client.IdentityClient;
import com.sketchnotes.blog_service.dtos.request.BlogRequest;
import com.sketchnotes.blog_service.dtos.request.UpdateBlogRequest;
import com.sketchnotes.blog_service.dtos.response.ApiResponse;
import com.sketchnotes.blog_service.dtos.response.BlogResponse;
import com.sketchnotes.blog_service.ultils.PagedResponse;
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
    private final IdentityClient  identityClient;

    @PostMapping
    public ResponseEntity<ApiResponse<BlogResponse>> create(@RequestBody BlogRequest req){
        var userId =  identityClient.getCurrentUser().getResult().getId();
        BlogResponse response = postService.createBlog(req,userId);
        return ResponseEntity.ok(ApiResponse.success( response,"create successful"));
    }

    @GetMapping("/my-blog")
    public ResponseEntity<ApiResponse<List<BlogResponse>>> get(){
        var user =  identityClient.getCurrentUser();
        List<BlogResponse> response = postService.getBlogsByUserId(user.getResult().getId());
        return ResponseEntity.ok(ApiResponse.success( response,"Get data successful"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<BlogResponse>>> list(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize){
        PagedResponse<BlogResponse> response = postService.getAll(pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success( response,"Get data successful"));
    }

    @PutMapping("/{blogid}")
    public ResponseEntity<BlogResponse> update(@PathVariable Long blogid,  @RequestBody UpdateBlogRequest req){
        var userId =  identityClient.getCurrentUser().getResult().getId();
        return ResponseEntity.ok(postService.updateBlog(blogid, req, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id){
        postService.deleteBlog(id);
        return ResponseEntity.ok(ApiResponse.success( null,"Delete successful"));
    }
}

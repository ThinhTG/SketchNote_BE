package com.sketchnotes.blog_service.controller;


import com.sketchnotes.blog_service.Service.BlogService;
import com.sketchnotes.blog_service.client.IdentityClient;
import com.sketchnotes.blog_service.dtos.BlogRequest;
import com.sketchnotes.blog_service.dtos.BlogResponse;
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
    public ResponseEntity<BlogResponse> create(@RequestBody BlogRequest req){
        var userId =  identityClient.getCurrentUser().getResult().getId();
        return ResponseEntity.ok(postService.createBlog(req,userId));
    }

    @GetMapping("/my-blog")
    public ResponseEntity<List<BlogResponse>> get(){
        var user =  identityClient.getCurrentUser();
        return ResponseEntity.ok(postService.getBlogsByUserId(user.getResult().getId()));
    }

    @GetMapping
    public ResponseEntity<List<BlogResponse>> list(){
        return ResponseEntity.ok(postService.getAll());
    }

    @PutMapping("/{blogid}")
    public ResponseEntity<BlogResponse> update(@PathVariable Long blogid,  @RequestBody BlogRequest req){
        var userId =  identityClient.getCurrentUser().getResult().getId();
        return ResponseEntity.ok(postService.updateBlog(blogid, req, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        postService.deleteBlog(id);
        return ResponseEntity.noContent().build();
    }
}

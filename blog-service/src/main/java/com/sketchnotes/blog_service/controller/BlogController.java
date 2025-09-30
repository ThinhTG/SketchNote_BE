package com.sketchnotes.blog_service.controller;


import com.sketchnotes.blog_service.Service.BlogService;
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

    @PostMapping
    public ResponseEntity<BlogResponse> create(@RequestBody BlogRequest req){
        return ResponseEntity.ok(postService.createBlog(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogResponse> get(@PathVariable Long id){
        return ResponseEntity.ok(postService.getBlog(id));
    }

    @GetMapping
    public ResponseEntity<List<BlogResponse>> list(){
        return ResponseEntity.ok(postService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BlogResponse> update(@PathVariable Long id,  @RequestBody BlogRequest req){
        return ResponseEntity.ok(postService.updateBlog(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        postService.deleteBlog(id);
        return ResponseEntity.noContent().build();
    }
}

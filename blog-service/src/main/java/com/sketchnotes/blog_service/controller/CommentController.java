package com.sketchnotes.blog_service.controller;


import com.sketchnotes.blog_service.Service.CommentService;
import com.sketchnotes.blog_service.dtos.request.CommentRequest;
import com.sketchnotes.blog_service.dtos.response.CommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long postId, @RequestBody CommentRequest req){
        return ResponseEntity.ok(commentService.addComment(postId, req));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId){
        return ResponseEntity.ok(commentService.getCommentsForPost(postId));
    }

    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<List<CommentResponse>> getReplies(@PathVariable Long commentId){
        return ResponseEntity.ok(commentService.getReplies(commentId));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId){
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
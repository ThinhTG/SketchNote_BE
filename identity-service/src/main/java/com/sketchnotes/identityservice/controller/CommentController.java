package com.sketchnotes.identityservice.controller;



import com.sketchnotes.identityservice.dtos.request.CommentRequest;
import com.sketchnotes.identityservice.dtos.request.UpdateCommentRequest;
import com.sketchnotes.identityservice.dtos.response.ApiResponse;
import com.sketchnotes.identityservice.dtos.response.CommentResponse;
import com.sketchnotes.identityservice.service.interfaces.CommentService;
import com.sketchnotes.identityservice.ultils.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    /**
     * Create a new comment (root comment or reply)
     * POST /api/blogs/{blogId}/comments
     */
    @PostMapping("/{blogId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long blogId, 
            @Valid @RequestBody CommentRequest req) {
        CommentResponse response = commentService.addComment(blogId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Comment created successfully"));
    }

    /**
     * Get paginated root comments for a blog
     * GET /api/blogs/{blogId}/comments?page=0&size=10
     */
    @GetMapping("/{blogId}/comments")
    public ResponseEntity<ApiResponse<PagedResponse<CommentResponse>>> getCommentsForBlog(
            @PathVariable Long blogId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<CommentResponse> response = commentService.getCommentsForBlog(blogId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "Get comments successful"));
    }

    /**
     * Get paginated replies for a parent comment
     * GET /api/blogs/comments/{parentId}/replies?page=0&size=5
     */
    @GetMapping("/comments/{parentId}/replies")
    public ResponseEntity<ApiResponse<PagedResponse<CommentResponse>>> getRepliesByParentId(
            @PathVariable Long parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<CommentResponse> response = commentService.getRepliesByParentId(parentId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "Get replies successful"));
    }

    /**
     * Update comment content
     * PUT /api/blogs/comments/{commentId}
     */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest req) {
        CommentResponse response = commentService.updateComment(commentId, req);
        return ResponseEntity.ok(ApiResponse.success(response, "Comment updated successfully"));
    }

    /**
     * Soft delete a comment
     * DELETE /api/blogs/comments/{commentId}
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<String>> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(ApiResponse.success(null, "Comment deleted successfully"));
    }

}
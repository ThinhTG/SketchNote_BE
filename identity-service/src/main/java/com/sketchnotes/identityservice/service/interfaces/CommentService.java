package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.dtos.request.CommentRequest;
import com.sketchnotes.identityservice.dtos.request.UpdateCommentRequest;
import com.sketchnotes.identityservice.dtos.response.CommentResponse;
import com.sketchnotes.identityservice.ultils.PagedResponse;

import java.util.List;

public interface CommentService {
    // Create comment (root or reply)
    CommentResponse addComment(Long blogId, CommentRequest request);
    
    // Get paginated root comments for a blog
    PagedResponse<CommentResponse> getCommentsForBlog(Long blogId, int page, int size);
    
    // Get paginated replies for a parent comment
    PagedResponse<CommentResponse> getRepliesByParentId(Long parentId, int page, int size);
    
    // Update comment content
    CommentResponse updateComment(Long commentId, UpdateCommentRequest request);
    
    // Soft delete comment
    void deleteComment(Long commentId);
    
    // Get reply count for a comment
    Long getReplyCount(Long commentId);
    
    // Legacy methods - kept for backward compatibility
    List<CommentResponse> getCommentsForPost(Long postId);
    List<CommentResponse> getReplies(Long commentId);
}




package com.sketchnotes.blog_service.Service;

import com.sketchnotes.blog_service.dtos.CommentRequest;
import com.sketchnotes.blog_service.dtos.*;

import java.util.List;

public interface CommentService {
    CommentResponse addComment(Long postId, CommentRequest request);
    List<CommentResponse> getCommentsForPost(Long postId);
    List<CommentResponse> getReplies(Long commentId);
    void deleteComment(Long commentId);

}


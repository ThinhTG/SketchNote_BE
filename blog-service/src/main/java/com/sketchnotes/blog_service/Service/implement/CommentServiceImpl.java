package com.sketchnotes.blog_service.Service.implement;

import com.sketchnotes.blog_service.Repository.BlogRepository;
import com.sketchnotes.blog_service.Repository.CommentRepository;
import com.sketchnotes.blog_service.Service.CommentService;
import com.sketchnotes.blog_service.client.UserClient;
import com.sketchnotes.blog_service.dtos.CommentRequest;
import com.sketchnotes.blog_service.dtos.CommentResponse;
import com.sketchnotes.blog_service.entity.Blog;
import com.sketchnotes.blog_service.entity.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final BlogRepository postRepository;
    private final UserClient userClient;

    @Override
    public CommentResponse addComment(Long postId, CommentRequest request) {
        Blog post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        String authorDisplay = fetchUserDisplay(request.getAuthorId());
        Comment c = Comment.builder()
                .blog(post)
                .content(request.getContent())
                .authorId(request.getAuthorId())
                .authorDisplay(authorDisplay)
                .parentCommentId(request.getParentCommentId())
                .build();
        Comment saved = commentRepository.save(c);
        return toDto(saved);
    }

    @Override
    public List<CommentResponse> getCommentsForPost(Long postId) {
        return commentRepository.findByBlogIdAndParentCommentIdIsNull(postId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<CommentResponse> getReplies(Long commentId) {
        return commentRepository.findByParentCommentId(commentId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    private String fetchUserDisplay(Long userId) {
        if (userId == null) return "Unknown";
        try {
            var user = userClient.getUserById(userId);
            if (user == null) return "Unknown";
            return user.fullName()!=null && !user.fullName().isEmpty() ? user.fullName() : user.username();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private CommentResponse toDto(Comment c){
        return CommentResponse.builder()
                .id(c.getId())
                .postId(c.getBlog().getId())
                .content(c.getContent())
                .authorId(c.getAuthorId())
                .authorDisplay(c.getAuthorDisplay())
                .parentCommentId(c.getParentCommentId())
                .createdAt(c.getCreatedAt())
                .build();
    }
}

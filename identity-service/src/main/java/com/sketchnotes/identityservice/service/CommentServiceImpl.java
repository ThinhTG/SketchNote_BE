package com.sketchnotes.identityservice.service;

import com.sketchnotes.identityservice.dtos.request.CommentRequest;
import com.sketchnotes.identityservice.dtos.request.UpdateCommentRequest;
import com.sketchnotes.identityservice.dtos.response.CommentResponse;
import com.sketchnotes.identityservice.model.Blog;
import com.sketchnotes.identityservice.model.Comment;
import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.repository.BlogRepository;
import com.sketchnotes.identityservice.repository.CommentRepository;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.service.interfaces.CommentService;
import com.sketchnotes.identityservice.ultils.PagedResponse;
import com.sketchnotes.identityservice.ultils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final BlogRepository postRepository;
    private final IUserRepository userRepository;


    @Override
    @Transactional
    public CommentResponse addComment(Long blogId, CommentRequest request) {
        Blog blog = postRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + blogId));

        User currentUser = userRepository.findByKeycloakId(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate parent comment exists if this is a reply
        if (request.getParentCommentId() != null) {
            commentRepository.findByIdAndDeletedAtIsNull(request.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found or has been deleted"));
        }

        Comment comment = Comment.builder()
                .blog(blog)
                .content(request.getContent())
                .author(currentUser)
                .parentCommentId(request.getParentCommentId())
                .build();
        
        Comment saved = commentRepository.save(comment);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CommentResponse> getCommentsForBlog(Long blogId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> commentPage = commentRepository.findByBlogIdAndParentCommentIdIsNull(blogId, pageable);
        
        List<CommentResponse> comments = commentPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                comments,
                commentPage.getNumber(),
                commentPage.getSize(),
                commentPage.getTotalElements(),
                commentPage.getTotalPages(),
                commentPage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CommentResponse> getRepliesByParentId(Long parentId, int page, int size) {
        // Verify parent comment exists
        commentRepository.findByIdAndDeletedAtIsNull(parentId)
                .orElseThrow(() -> new RuntimeException("Parent comment not found or has been deleted"));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Comment> repliesPage = commentRepository.findByParentCommentId(parentId, pageable);
        
        List<CommentResponse> replies = repliesPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                replies,
                repliesPage.getNumber(),
                repliesPage.getSize(),
                repliesPage.getTotalElements(),
                repliesPage.getTotalPages(),
                repliesPage.isLast()
        );
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, UpdateCommentRequest request) {
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found or has been deleted"));
        
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (!comment.getAuthor().getKeycloakId().equals(currentUserId)) {
            throw new RuntimeException("You are not authorized to update this comment");
        }
        
        comment.setContent(request.getContent());
        Comment updated = commentRepository.save(comment);
        return toDto(updated);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found or has been deleted"));
        
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (!comment.getAuthor().getKeycloakId().equals(currentUserId)) {
            throw new RuntimeException("You are not authorized to delete this comment");
        }
        
        // Soft delete - set deletedAt timestamp
        comment.setDeletedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getReplyCount(Long commentId) {
        return commentRepository.countRepliesByParentId(commentId);
    }

    // Legacy methods - kept for backward compatibility
    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsForPost(Long postId) {
        return commentRepository.findByBlogIdAndParentCommentIdIsNull(postId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getReplies(Long commentId) {
        return commentRepository.findByParentCommentId(commentId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    private CommentResponse toDto(Comment c) {
        Long replyCount = commentRepository.countRepliesByParentId(c.getId());
        
        return CommentResponse.builder()
                .id(c.getId())
                .postId(c.getBlog().getId())
                .content(c.getContent())
                .authorId(c.getAuthor().getId())
                .authorDisplay(c.getAuthor().getFirstName() + " " + c.getAuthor().getLastName())
                .authorAvatarUrl(c.getAuthor().getAvatarUrl())
                .parentCommentId(c.getParentCommentId())
                .replyCount(replyCount)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .isDeleted(c.getDeletedAt() != null)
                .build();
    }
}


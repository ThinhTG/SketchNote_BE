package com.sketchnotes.identityservice.service;

import com.sketchnotes.identityservice.dtos.request.CommentRequest;
import com.sketchnotes.identityservice.dtos.response.CommentResponse;
import com.sketchnotes.identityservice.model.Blog;
import com.sketchnotes.identityservice.model.Comment;
import com.sketchnotes.identityservice.repository.BlogRepository;
import com.sketchnotes.identityservice.repository.CommentRepository;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.service.interfaces.CommentService;
import com.sketchnotes.identityservice.ultils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final BlogRepository postRepository;
    private final IUserRepository userRepository;


    @Override
    public CommentResponse addComment(Long postId, CommentRequest request) {
        Blog post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));

        Comment c = Comment.builder()
                .blog(post)
                .content(request.getContent())
                .author(userRepository.findByKeycloakId(SecurityUtils.getCurrentUserId())
                        .orElseThrow(() -> new RuntimeException("User not found")))
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


    private CommentResponse toDto(Comment c){
        return CommentResponse.builder()
                .id(c.getId())
                .postId(c.getBlog().getId())
                .content(c.getContent())
                .authorId(c.getAuthor().getId())
                .authorDisplay(c.getAuthor().getFirstName() + " " + c.getAuthor().getLastName())
                .parentCommentId(c.getParentCommentId())
                .createdAt(c.getCreatedAt())
                .build();
    }
}

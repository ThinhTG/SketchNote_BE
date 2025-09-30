package com.sketchnotes.blog_service.Service.implement;

import com.sketchnotes.blog_service.Repository.BlogRepository;
import com.sketchnotes.blog_service.Service.BlogService;
import com.sketchnotes.blog_service.client.UserClient;
import com.sketchnotes.blog_service.dtos.*;
import com.sketchnotes.blog_service.entity.Blog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogRepository postRepository;
    private final UserClient userClient;

    @Override
    public BlogResponse createBlog(BlogRequest request) {
        String authorDisplay = fetchUserDisplay(request.getAuthorId());
        Blog p = Blog.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .authorId(request.getAuthorId())
                .authorDisplay(authorDisplay)
                .build();
        Blog saved = postRepository.save(p);
        return toDto(saved);
    }

    @Override
    public BlogResponse getBlog(Long id) {
        return postRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    @Override
    public List<BlogResponse> getAll() {
        return postRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public BlogResponse updateBlog(Long id, BlogRequest request) {
        Blog post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        // optionally update author snapshot if authorId changed
        if (request.getAuthorId() != null && !request.getAuthorId().equals(post.getAuthorId())) {
            post.setAuthorId(request.getAuthorId());
            post.setAuthorDisplay(fetchUserDisplay(request.getAuthorId()));
        }
        return toDto(postRepository.save(post));
    }

    @Override
    public void deleteBlog(Long id) {
        postRepository.deleteById(id);
    }

    private String fetchUserDisplay(Long userId) {
        if (userId == null) return "Unknown";
        try {
            var user = userClient.getUserById(userId);
            if (user == null) return "Unknown";
            return user.fullName()!= null && !user.fullName().isEmpty() ? user.fullName() : user.username();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private BlogResponse toDto(Blog p){
        return BlogResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .content(p.getContent())
                .authorId(p.getAuthorId())
                .authorDisplay(p.getAuthorDisplay())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
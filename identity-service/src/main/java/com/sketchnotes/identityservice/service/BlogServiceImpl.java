package com.sketchnotes.identityservice.service;

import com.sketchnotes.identityservice.dtos.request.BlogRequest;
import com.sketchnotes.identityservice.dtos.request.ContentRequest;
import com.sketchnotes.identityservice.dtos.request.UpdateBlogRequest;
import com.sketchnotes.identityservice.dtos.response.BlogResponse;
import com.sketchnotes.identityservice.dtos.response.ContentResponse;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.model.Blog;
import com.sketchnotes.identityservice.model.Content;
import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.repository.BlogRepository;
import com.sketchnotes.identityservice.repository.ContentRepository;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.service.interfaces.BlogService;
import com.sketchnotes.identityservice.ultils.PagedResponse;
import com.sketchnotes.identityservice.ultils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final IUserRepository userRepository;
    private final ContentRepository contentRepository;

    @Override
    public BlogResponse createBlog(BlogRequest request) {
        User user =  userRepository.findByKeycloakId(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Blog p = Blog.builder()
                .title(request.getTitle())
                .summary(request.getSummary())
                .author(user)
                .imageUrl(request.getImageUrl())
                .build();
        Blog saved = blogRepository.save(p);
        for(ContentRequest cr : request.getContents()){
             contentRepository.save(
                    Content.builder()
                            .blog(saved)
                            .index(cr.getIndex())
                            .contentUrl(cr.getContentUrl())
                            .sectionTitle(cr.getSectionTitle())
                            .content(cr.getContent())
                            .build()
            );
        }
        return toDto(saved);
    }

    @Override
    public BlogResponse getBlog(Long id) {
        return blogRepository.findByIdAndDeletedAtIsNull(id).map(this::toDto)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_FOUND));
    }

    @Override
    public PagedResponse<BlogResponse> getAll(int pageNo, int pageSize)  {
        Pageable pageable =  PageRequest.of(pageNo, pageSize);
        Page<Blog> blogs = blogRepository.findBlogsByDeletedAtIsNull(pageable);
        List<BlogResponse> responses = blogs.stream().map(this::toDto).collect(Collectors.toList());
        return new PagedResponse<>(
                responses,
                blogs.getNumber(),
                blogs.getSize(),
                (int) blogs.getTotalElements(),
                blogs.getTotalPages(),
                blogs.isLast()
        );
    }

    @Override
    public BlogResponse updateBlog(Long id, UpdateBlogRequest request) {
        Blog post = blogRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_FOUND));
        post.setTitle(request.getTitle());
        post.setSummary(request.getSummary());
        post.setImageUrl(request.getImageUrl());
        return toDto(blogRepository.save(post));
    }

    @Override
    public void deleteBlog(Long id) {
        Blog blog = blogRepository.findBlogsByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_FOUND));
        blog.setDeletedAt(java.time.LocalDateTime.now());
        List<Content> contents = contentRepository.findByBlogIdOrderByIndexAsc(blog.getId());
        for(Content c : contents){
            c.setDeletedAt(java.time.LocalDateTime.now());
            contentRepository.save(c);
        }
        blogRepository.save(blog);
    }

    private BlogResponse toDto(Blog p){
        List<ContentResponse> contents = contentRepository.findByBlogIdOrderByIndexAsc(p.getId())
                .stream().map(this::toDto).collect(Collectors.toList());
        String userName =p.getAuthor().getFirstName() + " " + p.getAuthor().getLastName();
        return BlogResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .summary(p.getSummary())
                .imageUrl(p.getImageUrl())
                .authorId(p.getAuthor().getId())
                .authorDisplay(userName)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .contents(contents)
                .build();
    }
    private ContentResponse toDto(Content p){
        return ContentResponse.builder()
                .id(p.getId())
                .sectionTitle(p.getSectionTitle())
                .contentUrl(p.getContentUrl())
                .index(p.getIndex())
                .content(p.getContent())
                .build();
    }

    @Override
    public List<BlogResponse> getBlogsByUserId(Long userId) {
        List<Blog> blogs = blogRepository.findByAuthorIdAndDeletedAtIsNull(userId);
        return blogs.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    @Override
    public List<BlogResponse> getMyBlogs() {
        User user =  userRepository.findByKeycloakId(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        List<Blog> blogs = blogRepository.findByAuthorIdAndDeletedAtIsNull(user.getId());
        return blogs.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
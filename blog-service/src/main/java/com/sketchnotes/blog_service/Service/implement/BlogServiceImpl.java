package com.sketchnotes.blog_service.Service.implement;

import com.sketchnotes.blog_service.Repository.BlogRepository;
import com.sketchnotes.blog_service.Repository.ContentRepository;
import com.sketchnotes.blog_service.Service.BlogService;
import com.sketchnotes.blog_service.client.IdentityClient;
import com.sketchnotes.blog_service.dtos.request.BlogRequest;
import com.sketchnotes.blog_service.dtos.request.ContentRequest;
import com.sketchnotes.blog_service.dtos.request.UpdateBlogRequest;
import com.sketchnotes.blog_service.dtos.response.ApiResponse;
import com.sketchnotes.blog_service.dtos.response.BlogResponse;
import com.sketchnotes.blog_service.dtos.response.ContentResponse;
import com.sketchnotes.blog_service.dtos.response.UserResponse;
import com.sketchnotes.blog_service.entity.Blog;
import com.sketchnotes.blog_service.entity.Content;
import com.sketchnotes.blog_service.exception.AppException;
import com.sketchnotes.blog_service.exception.ErrorCode;
import com.sketchnotes.blog_service.ultils.PagedResponse;
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
    private final IdentityClient userClient;
    private final ContentRepository contentRepository;

    @Override
    public BlogResponse createBlog(BlogRequest request, Long userId) {
        Blog p = Blog.builder()
                .title(request.getTitle())
                .summary(request.getSummary())
                .authorId(userId)
                .imageUrl(request.getImageUrl())
                .build();
        Blog saved = blogRepository.save(p);
        for(ContentRequest cr : request.getContents()){
             contentRepository.save(
                    com.sketchnotes.blog_service.entity.Content.builder()
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
    public BlogResponse updateBlog(Long id, UpdateBlogRequest request, Long userId) {
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
        ApiResponse<UserResponse> user = userClient.getUserById(p.getAuthorId());
        String userName = user.getResult().getFirstName() + user.getResult().getLastName();
        return BlogResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .summary(p.getSummary())
                .imageUrl(p.getImageUrl())
                .authorId(p.getAuthorId())
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
        List<Blog> blogs = blogRepository.findByAuthorId(userId);
        return blogs.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
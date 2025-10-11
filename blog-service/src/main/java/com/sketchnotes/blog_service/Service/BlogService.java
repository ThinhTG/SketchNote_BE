package com.sketchnotes.blog_service.Service;

import com.sketchnotes.blog_service.dtos.*;

import java.util.List;

public interface BlogService {
    BlogResponse createBlog(BlogRequest request,Long userId);
    BlogResponse getBlog(Long id);
    List<BlogResponse> getAll();
    BlogResponse updateBlog(Long id, BlogRequest request, Long userId);
    void deleteBlog(Long id);
    List<BlogResponse> getBlogsByUserId(Long userId);
}
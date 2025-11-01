package com.sketchnotes.blog_service.Service;

import com.sketchnotes.blog_service.dtos.request.BlogRequest;
import com.sketchnotes.blog_service.dtos.request.UpdateBlogRequest;
import com.sketchnotes.blog_service.dtos.response.BlogResponse;
import com.sketchnotes.blog_service.ultils.PagedResponse;

import java.util.List;

public interface BlogService {
    BlogResponse createBlog(BlogRequest request, Long userId);
    BlogResponse getBlog(Long id);
    PagedResponse<BlogResponse> getAll(int pageNo, int pageSize) ;
    BlogResponse updateBlog(Long id, UpdateBlogRequest request, Long userId);
    void deleteBlog(Long id);
    List<BlogResponse> getBlogsByUserId(Long userId);
}
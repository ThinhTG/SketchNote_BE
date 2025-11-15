package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.dtos.request.BlogRequest;
import com.sketchnotes.identityservice.dtos.request.UpdateBlogRequest;
import com.sketchnotes.identityservice.dtos.response.BlogResponse;
import com.sketchnotes.identityservice.enums.BlogStatus;
import com.sketchnotes.identityservice.ultils.PagedResponse;

import java.util.List;

public interface BlogService {
    BlogResponse createBlog(BlogRequest request);
    BlogResponse getBlog(Long id);
    PagedResponse<BlogResponse> getAll(int pageNo, int pageSize, BlogStatus status);
    BlogResponse updateBlog(Long id, UpdateBlogRequest request);
    BlogResponse publishBlog(Long id, BlogStatus status);
    void deleteBlog(Long id);
    List<BlogResponse> getBlogsByUserId(Long userId);
    List<BlogResponse> getMyBlogs();
}
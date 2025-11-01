package com.sketchnotes.blog_service.Service;

import com.sketchnotes.blog_service.dtos.request.ContentRequest;
import com.sketchnotes.blog_service.dtos.response.ContentResponse;

public interface ContentService {
    ContentResponse createContent(Long blogId, ContentRequest content);
    ContentResponse UpdateContent(Long contentId, ContentRequest content);
    void deleteContent(Long contentId);
}

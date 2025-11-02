package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.dtos.request.ContentRequest;
import com.sketchnotes.identityservice.dtos.response.ContentResponse;

public interface ContentService {
    ContentResponse createContent(Long blogId, ContentRequest content);
    ContentResponse UpdateContent(Long contentId, ContentRequest content);
    void deleteContent(Long contentId);
}

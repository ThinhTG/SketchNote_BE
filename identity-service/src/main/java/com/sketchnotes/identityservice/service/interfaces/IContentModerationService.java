package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.dtos.response.ContentCheckResponse;
import com.sketchnotes.identityservice.dtos.response.ImageSafetyCheckResponse;
import com.sketchnotes.identityservice.model.Blog;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for content moderation operations.
 * Provides AI-based content moderation using Google Vision and Gemini.
 */
public interface IContentModerationService {
    
    /**
     * Check blog content using AI moderation.
     * @param blog Blog entity to check
     * @return ContentCheckResponse with moderation results
     */
    ContentCheckResponse checkBlogContent(Blog blog);
    
    /**
     * Check image safety using Google Vision SafeSearch.
     * @param imageFile Image file to check
     * @return ImageSafetyCheckResponse with safety check results
     */
    ImageSafetyCheckResponse testImageSafety(MultipartFile imageFile);
    
    /**
     * Scheduled task to moderate pending blogs.
     */
    void moderatePendingBlogs();
}

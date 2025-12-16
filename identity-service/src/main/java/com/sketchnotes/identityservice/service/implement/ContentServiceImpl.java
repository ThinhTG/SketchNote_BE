package com.sketchnotes.identityservice.service.implement;


import com.sketchnotes.identityservice.dtos.request.ContentRequest;
import com.sketchnotes.identityservice.dtos.response.ContentResponse;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.model.Blog;
import com.sketchnotes.identityservice.model.Content;
import com.sketchnotes.identityservice.repository.BlogRepository;
import com.sketchnotes.identityservice.repository.ContentRepository;
import com.sketchnotes.identityservice.service.interfaces.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {
    private final ContentRepository contentRepository;
    private final BlogRepository blogRepository;
    @Override
    public ContentResponse createContent(Long blogId, ContentRequest content) {
        Blog blog = blogRepository.findByIdAndDeletedAtIsNull(blogId).orElseThrow(()
                -> new AppException(ErrorCode.BLOG_NOT_FOUND));
        var newContent = contentRepository.save(
                        Content.builder()
                        .blog(blog)
                        .index(content.getIndex())
                        .contentUrl(content.getContentUrl())
                        .sectionTitle(content.getSectionTitle())
                        .content(content.getContent())
                        .build()
        );
        return ContentResponse.builder()
                .id(newContent.getId())
                .index(newContent.getIndex())
                .content(newContent.getContent())
                .contentUrl(newContent.getContentUrl())
                .sectionTitle(newContent.getSectionTitle())
                .build();
    }

    @Override
    public ContentResponse UpdateContent(Long contentId, ContentRequest content) {
        Content con = contentRepository.findByIdAndDeletedAtIsNull(contentId).orElseThrow(()
                -> new AppException(ErrorCode.CONTENT_NOT_FOUND));
        con.setIndex(content.getIndex());
        con.setContent(content.getContent());
        con.setContentUrl(content.getContentUrl());
        con.setSectionTitle(content.getSectionTitle());
        var newContent = contentRepository.save(con);
        return ContentResponse.builder()
                .id(newContent.getId())
                .index(newContent.getIndex())
                .content(newContent.getContent())
                .contentUrl(newContent.getContentUrl())
                .sectionTitle(newContent.getSectionTitle())
                .build();
    }

    @Override
    public void deleteContent(Long contentId) {
        Content con = contentRepository.findByIdAndDeletedAtIsNull(contentId).orElseThrow(()
                -> new AppException(ErrorCode.CONTENT_NOT_FOUND));
        con.setDeletedAt(java.time.LocalDateTime.now());
        contentRepository.save(con);
    }
}

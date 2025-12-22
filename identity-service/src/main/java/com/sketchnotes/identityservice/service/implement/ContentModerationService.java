package com.sketchnotes.identityservice.service.implement;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.sketchnotes.identityservice.config.GeminiProperties;
import com.sketchnotes.identityservice.dtos.response.BlogModerationHistoryResponse;
import com.sketchnotes.identityservice.dtos.response.ContentCheckResponse;
import com.sketchnotes.identityservice.enums.BlogStatus;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.model.Blog;
import com.sketchnotes.identityservice.model.BlogModerationHistory;
import com.sketchnotes.identityservice.model.Content;
import com.sketchnotes.identityservice.repository.BlogModerationHistoryRepository;
import com.sketchnotes.identityservice.repository.BlogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentModerationService {

    private final BlogRepository blogRepository;
    private final BlogModerationHistoryRepository moderationHistoryRepository;
    private final GeminiProperties geminiProperties;


    /**
     * Scheduled task runs every 15 minutes to check blogs that need moderation
     * Only moderates blogs that have been published for more than 15 minutes
     */
    @Scheduled(fixedRate =15*  60 * 1000) // Run every 15 minutes (15 * 60 * 1000 = 900000 ms)
    @Transactional
    public void moderatePendingBlogs() {
        try {
            // Validate configuration before processing
            validateConfiguration();
            
            List<Blog> pendingBlogs = blogRepository.findBlogsForModeration(BlogStatus.PENDING_REVIEW);
            log.info("Checking {} pending blogs for moderation", pendingBlogs.size());

            for (Blog blog : pendingBlogs) {
                log.info("Checking blog {} for moderation", blog.getId());
                try {
                    ContentCheckResponse checkResult = checkBlogContent(blog);
                    updateBlogStatus(blog, checkResult);
                    log.info("Successfully moderated blog {}: isSafe={}, score={}", 
                            blog.getId(), checkResult.isSafe(), checkResult.getSafetyScore());
                } catch (AppException e) {
                    // Re-throw AppException as-is
                    log.error("Failed to moderate blog {}: {} - {}", blog.getId(), e.getErrorCode(), e.getMessage());
                    // Don't throw here - continue processing other blogs
                } catch (Exception e) {
                    log.error("Unexpected error moderating blog {}: {}", blog.getId(), e.getMessage(), e);
                    // Don't throw here - continue processing other blogs
                }
            }
            
            log.info("Completed moderation check for {} blogs", pendingBlogs.size());
        } catch (Exception e) {
            log.error("Error in scheduled moderation task: {}", e.getMessage(), e);
            // Don't re-throw - let the scheduler continue
        }
    }

    /**
     * Validate Gemini configuration
     */
    private void validateConfiguration() {
        if (geminiProperties.getProjectId() == null || geminiProperties.getProjectId().trim().isEmpty()) {
            log.error("Google Cloud Project ID is not configured. Please set GOOGLE_CLOUD_PROJECT_ID environment variable");
            throw new AppException(ErrorCode.AI_CONFIG_MISSING);
        }
        if (geminiProperties.getLocation() == null || geminiProperties.getLocation().trim().isEmpty()) {
            log.error("Google Cloud location is not configured");
            throw new AppException(ErrorCode.AI_CONFIG_MISSING);
        }
        if (geminiProperties.getModel() == null || geminiProperties.getModel().trim().isEmpty()) {
            log.error("Gemini model is not configured");
            throw new AppException(ErrorCode.AI_CONFIG_MISSING);
        }
        log.debug("Gemini configuration validated: projectId={}, location={}, model={}", 
                geminiProperties.getProjectId(), geminiProperties.getLocation(), geminiProperties.getModel());
    }

    /**
     * Check blog content using Gemini AI
     *
     * @param blog Blog to check
     * @return ContentCheckResponse containing check results
     */
    public ContentCheckResponse checkBlogContent(Blog blog) {
        log.info("Starting content check for blog {}", blog.getId());
        
        try {
            // Validate configuration
            validateConfiguration();
            
            // Build content for AI moderation
            log.debug("Building content for moderation for blog {}", blog.getId());
            String contentToCheck = buildContentForModeration(blog);
            log.debug("Content built, length: {} characters", contentToCheck.length());

            // Call Gemini AI for analysis
            log.info("Calling Gemini AI for blog {} analysis", blog.getId());
            String aiResponse = analyzeContentWithGemini(contentToCheck);
            log.debug("Received AI response for blog {}: {}", blog.getId(), aiResponse);

            // Parse AI response
            log.debug("Parsing AI response for blog {}", blog.getId());
            ContentCheckResponse response = parseAIResponse(aiResponse);
            log.info("Successfully checked blog {}: isSafe={}, score={}", 
                    blog.getId(), response.isSafe(), response.getSafetyScore());
            
            return response;

        } catch (AppException e) {
            // Re-throw AppException as-is
            log.error("AppException while checking blog {}: {}", blog.getId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            // Log the full exception details
            log.error("Unexpected error checking blog content for blog {}: {} - {}", 
                    blog.getId(), e.getClass().getName(), e.getMessage(), e);
            
            // Throw with more context
            throw new AppException(ErrorCode.AI_MODERATION_FAILED);
        }
    }

    /**
     * Build content string for moderation from blog
     */
    private String buildContentForModeration(Blog blog) {
        StringBuilder content = new StringBuilder();

        content.append("TITLE: ").append(blog.getTitle() != null ? blog.getTitle() : "").append("\n\n");
        content.append("SUMMARY: ").append(blog.getSummary() != null ? blog.getSummary() : "").append("\n\n");

        if (blog.getContents() != null && !blog.getContents().isEmpty()) {
            content.append("CONTENTS:\n");
            for (Content c : blog.getContents()) {
                content.append("- Section ").append(c.getIndex()).append(": ");
                if (c.getSectionTitle() != null) {
                    content.append(c.getSectionTitle()).append("\n");
                }
                if (c.getContent() != null) {
                    content.append(c.getContent()).append("\n");
                }
                content.append("\n");
            }
        }

        return content.toString();
    }

    /**
     * Call Gemini AI to analyze content
     */
    private String analyzeContentWithGemini(String content) throws Exception {
        VertexAI vertexAI = null;
        try {
            log.debug("Initializing VertexAI with projectId: {}, location: {}", 
                    geminiProperties.getProjectId(), geminiProperties.getLocation());
            
            vertexAI = new VertexAI(geminiProperties.getProjectId(), geminiProperties.getLocation());
            GenerativeModel model = new GenerativeModel(geminiProperties.getModel(), vertexAI);

            String prompt = String.format("""
                    You are an AI content moderation specialist. Analyze the following blog content and evaluate if it contains:
                    - Profanity, offensive language, vulgar words
                    - Adult content, pornography (18+)
                    - Drugs, illegal substances
                    - Violence, terrorism
                    - Fraud, hacking, phishing
                    - Spam or junk advertising
                    - Hate speech
                    - Dangerous misinformation
                    Content to check:
                    ---
                    %s
                    ---
                                    
                    Respond EXACTLY in the following JSON format (no markdown, pure JSON only):
                    {
                      "isSafe": true/false,
                      "safetyScore": <number from 0-100>,
                      "violations": ["violation 1", "violation 2", ...],
                      "reason": "Detailed explanation"
                    }
                                    
                    Where:
                    - isSafe: true if content is completely safe, false if violations found
                    - safetyScore: 100 is safest, 0 is most dangerous
                    - violations: list of specific violations (empty [] if safe)
                    - reason: brief explanation in English
                                    
                    RETURN ONLY JSON, NO OTHER TEXT.
                    """, content);
            
            log.debug("Sending request to Gemini AI...");
            GenerateContentResponse response = model.generateContent(prompt);
            
            String responseText = ResponseHandler.getText(response);
            log.debug("Received response from Gemini AI, length: {} characters", responseText.length());
            
            return responseText;
            
        } catch (com.google.api.gax.rpc.UnauthenticatedException e) {
            log.error("Authentication failed with Google Cloud. Please check your credentials and GOOGLE_CLOUD_PROJECT_ID environment variable: {}", e.getMessage());
            throw new Exception("Google Cloud authentication failed. Please verify your credentials are properly configured.", e);
        } catch (com.google.api.gax.rpc.PermissionDeniedException e) {
            log.error("Permission denied accessing Vertex AI. Please check if Vertex AI API is enabled and you have proper permissions: {}", e.getMessage());
            throw new Exception("Permission denied to access Vertex AI. Please enable the API and verify permissions.", e);
        } catch (com.google.api.gax.rpc.NotFoundException e) {
            log.error("Resource not found in Vertex AI. Please check project ID and location: {}", e.getMessage());
            throw new Exception("Vertex AI resource not found. Please verify project ID and location configuration.", e);
        } catch (com.google.api.gax.rpc.ApiException e) {
            log.error("Vertex AI API error (status: {}): {}", e.getStatusCode(), e.getMessage(), e);
            throw new Exception("Vertex AI API error: " + e.getMessage(), e);
        } catch (java.io.IOException e) {
            log.error("Network/IO error connecting to Vertex AI: {}", e.getMessage(), e);
            throw new Exception("Network error connecting to Vertex AI: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error calling Gemini AI: {} - {}", e.getClass().getName(), e.getMessage(), e);
            throw new Exception("Unexpected error calling Gemini AI: " + e.getMessage(), e);
        } finally {
            if (vertexAI != null) {
                try {
                    vertexAI.close();
                    log.debug("VertexAI connection closed");
                } catch (Exception e) {
                    log.warn("Error closing VertexAI connection: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Parse AI response into ContentCheckResponse
     */
    private ContentCheckResponse parseAIResponse(String aiResponse) {
        try {
            // Remove markdown code block if present
            String jsonResponse = aiResponse.trim();
            if (jsonResponse.startsWith("```json")) {
                jsonResponse = jsonResponse.substring(7);
            }
            if (jsonResponse.startsWith("```")) {
                jsonResponse = jsonResponse.substring(3);
            }
            if (jsonResponse.endsWith("```")) {
                jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 3);
            }
            jsonResponse = jsonResponse.trim();

            // Simple JSON parsing
            boolean isSafe = jsonResponse.contains("\"isSafe\": true") ||
                    jsonResponse.contains("\"isSafe\":true");

            int safetyScore = extractSafetyScore(jsonResponse);
            String reason = extractReason(jsonResponse);

            return ContentCheckResponse.builder()
                    .isSafe(isSafe)
                    .safetyScore(safetyScore)
                    .reason(reason)
                    .build();

        } catch (Exception e) {
            return ContentCheckResponse.builder()
                    .isSafe(false)
                    .safetyScore(50)
                    .reason("Unable to parse AI response. Manual review required.")
                    .build();
        }
    }

    private int extractSafetyScore(String json) {
        try {
            String scorePattern = "\"safetyScore\":\\s*(\\d+)";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(scorePattern);
            java.util.regex.Matcher matcher = pattern.matcher(json);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
        }
        return 50; // Default
    }

    private String extractReason(String json) {
        try {
            String reasonPattern = "\"reason\":\\s*\"([^\"]+)\"";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(reasonPattern);
            java.util.regex.Matcher matcher = pattern.matcher(json);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            throw new AppException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
        return "No detailed information from AI";
    }


    /**
     * Update blog status based on moderation result
     *
     * @param blog        Blog to update
     * @param checkResult Moderation result
     */
    private void updateBlogStatus(Blog blog, ContentCheckResponse checkResult) {
        BlogStatus previousStatus = blog.getStatus();
        BlogStatus newStatus = checkResult.isSafe() ? BlogStatus.PUBLISHED : BlogStatus.AI_REJECTED;
        if (newStatus == BlogStatus.AI_REJECTED) {
            BlogModerationHistory history = BlogModerationHistory.builder()
                    .blog(blog)
                    .previousStatus(previousStatus)
                    .newStatus(newStatus)
                    .isSafe(checkResult.isSafe())
                    .safetyScore(checkResult.getSafetyScore())
                    .reason(checkResult.getReason())
                    .build();
            moderationHistoryRepository.save(history);

            // Update blog status
            blog.setStatus(newStatus);
            blogRepository.save(blog);
        }
    }

    /**
     * Check a specific blog immediately (can be called from controller if needed)
     *
     * @param blogId ID of the blog to check
     * @return Check result
     */
    @Transactional(readOnly = true)
    public ContentCheckResponse checkBlogById(Long blogId) {
        Blog blog = blogRepository.findByIdAndDeletedAtIsNull(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + blogId));

        return checkBlogContent(blog);
    }

    /**
     * Get the latest moderation history for a blog
     *
     * @param blogId ID of the blog
     * @return Latest moderation history or null if not found
     */
    @Transactional(readOnly = true)
    public BlogModerationHistoryResponse getLatestModerationHistory(Long blogId) {
        // Verify blog exists
        blogRepository.findByIdAndDeletedAtIsNull(blogId)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_FOUND));

        return moderationHistoryRepository.findLatestByBlogId(blogId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    /**
     * Get all moderation history for a blog
     *
     * @param blogId ID of the blog
     * @return List of moderation history
     */
    @Transactional(readOnly = true)
    public List<BlogModerationHistoryResponse> getAllModerationHistory(Long blogId) {
        // Verify blog exists
        blogRepository.findByIdAndDeletedAtIsNull(blogId)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_FOUND));

        return moderationHistoryRepository.findByBlogIdOrderByCheckedAtDesc(blogId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Map BlogModerationHistory entity to response DTO
     */
    private BlogModerationHistoryResponse mapToResponse(BlogModerationHistory history) {
        return BlogModerationHistoryResponse.builder()
                .id(history.getId())
                .blogId(history.getBlog().getId())
                .previousStatus(history.getPreviousStatus())
                .newStatus(history.getNewStatus())
                .isSafe(history.getIsSafe())
                .safetyScore(history.getSafetyScore())
                .reason(history.getReason())
                .checkedAt(history.getCheckedAt())
                .build();
    }
}

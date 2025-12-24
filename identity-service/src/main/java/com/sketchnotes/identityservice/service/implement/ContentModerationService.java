package com.sketchnotes.identityservice.service.implement;

import com.google.api.gax.rpc.ApiException;
import com.google.api.gax.rpc.PermissionDeniedException;
import com.google.api.gax.rpc.UnauthenticatedException;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.google.cloud.vision.v1.*;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.protobuf.ByteString;
import com.sketchnotes.identityservice.config.GeminiProperties;
import com.sketchnotes.identityservice.dtos.response.BlogModerationHistoryResponse;
import com.sketchnotes.identityservice.dtos.response.ContentCheckResponse;
import com.sketchnotes.identityservice.dtos.response.ImageSafetyCheckResponse;
import com.sketchnotes.identityservice.enums.BlogStatus;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.exception.NotFoundException;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentModerationService {

    private final BlogRepository blogRepository;
    private final BlogModerationHistoryRepository moderationHistoryRepository;
    private final GeminiProperties geminiProperties;
    private final ImageAnnotatorClient imageAnnotatorClient;


    /**
     * Scheduled task runs every 15 minutes to check blogs that need moderation
     * Only moderates blogs that have been published for more than 15 minutes
     */
    @Scheduled(fixedRate = 15 * 60 * 1000) // Run every 15 minutes (15 * 60 * 1000 = 900000 ms)
    @Transactional
    public void moderatePendingBlogs() {
        validateConfiguration();
        List<Blog> pendingBlogs = blogRepository.findBlogsForModeration(BlogStatus.PENDING_REVIEW);
        for (Blog blog : pendingBlogs) {
            ContentCheckResponse checkResult = checkBlogContent(blog);
            updateBlogStatus(blog, checkResult);
        }
    }

    /**
     * Validate Gemini configuration
     */
    private void validateConfiguration() {
        if (geminiProperties.getProjectId() == null || geminiProperties.getProjectId().trim().isEmpty()) {
            throw new AppException(ErrorCode.AI_CONFIG_MISSING);
        }
        if (geminiProperties.getLocation() == null || geminiProperties.getLocation().trim().isEmpty()) {
            throw new AppException(ErrorCode.AI_CONFIG_MISSING);
        }
        if (geminiProperties.getModel() == null || geminiProperties.getModel().trim().isEmpty()) {
            throw new AppException(ErrorCode.AI_CONFIG_MISSING);
        }
    }

    /**
     * Check blog content using Gemini AI
     *
     * @param blog Blog to check
     * @return ContentCheckResponse containing check results
     */
    public ContentCheckResponse checkBlogContent(Blog blog) {
        try {
            // Validate configuration
            validateConfiguration();

            String contentToCheck = buildContentForModeration(blog);

            String aiResponse = analyzeContentWithGemini(contentToCheck);

            ContentCheckResponse response = parseAIResponse(aiResponse);


            return response;

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.AI_MODERATION_FAILED);
        }
    }

    /**
     * Build content string for moderation from blog INCLUDING IMAGE ANALYSIS
     */
    private String buildContentForModeration(Blog blog) {
        StringBuilder content = new StringBuilder();

        content.append("TITLE: ").append(blog.getTitle() != null ? blog.getTitle() : "").append("\n\n");
        content.append("SUMMARY: ").append(blog.getSummary() != null ? blog.getSummary() : "").append("\n\n");

        // --- IMAGE SAFETY ANALYSIS REPORT ---
        content.append("=== IMAGE SAFETY ANALYSIS REPORT ===\n");
        // 1. Check cover image
        if (blog.getImageUrl() != null && !blog.getImageUrl().isEmpty()) {
            String analysis = analyzeSingleImage(blog.getImageUrl(), "Cover Image");
            content.append(analysis).append("\n");
        }

        // 2. Check content images
        if (blog.getContents() != null && !blog.getContents().isEmpty()) {
            content.append("\nTEXT CONTENTS & INLINE IMAGES:\n");
            for (Content c : blog.getContents()) {
                content.append("- Section ").append(c.getIndex()).append(": ");
                if (c.getSectionTitle() != null) {
                    content.append("Title: ").append(c.getSectionTitle()).append(" | ");
                }
                if (c.getContent() != null) {
                    content.append("Text: ").append(c.getContent()).append(" | ");
                }

                // If section has image, scan and include result
                if (c.getContentUrl() != null && !c.getContentUrl().isEmpty()) {
                    String imgAnalysis = analyzeSingleImage(c.getContentUrl(), "Section " + c.getIndex() + " Image");
                    content.append("\n  [Image Analysis]: ").append(imgAnalysis);
                }
                content.append("\n");
            }
        }

        return content.toString();
    }


    private String analyzeSingleImage(String imageUrl, String imageLabel) {
        try {
            // Create request to Vision API
            ImageSource imgSource = ImageSource.newBuilder().setImageUri(imageUrl).build();
            Image img = Image.newBuilder().setSource(imgSource).build();
            Feature feature = Feature.newBuilder().setType(Type.SAFE_SEARCH_DETECTION).build();

            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feature)
                    .setImage(img)
                    .build();

            // Call API (Synchronous)
            BatchAnnotateImagesResponse response = imageAnnotatorClient.batchAnnotateImages(List.of(request));

            if (response.getResponsesCount() > 0) {
                AnnotateImageResponse res = response.getResponses(0);
                if (res.hasError()) {
                    return imageLabel + ": ERROR checking image (" + res.getError().getMessage() + ")";
                }

                SafeSearchAnnotation annotation = res.getSafeSearchAnnotation();

                // Format result for Gemini to read
                // Only flag if violation likelihood is POSSIBLE, LIKELY or VERY_LIKELY
                boolean isSuspicious = isLikely(annotation.getAdult()) ||
                        isLikely(annotation.getViolence()) ||
                        isLikely(annotation.getRacy()) ||
                        isLikely(annotation.getMedical());

                if (isSuspicious) {
                    return String.format("%s: WARNING DETECTED [Adult: %s, Violence: %s, Racy: %s, Medical: %s]",
                            imageLabel, annotation.getAdult(), annotation.getViolence(),
                            annotation.getRacy(), annotation.getMedical());
                } else {
                    return imageLabel + ": SAFE";
                }
            }
            return imageLabel + ": No analysis result";

        } catch (Exception e) {
            return imageLabel + ": FAILED to analyze (Error: " + e.getMessage() + ")";
        }
    }

    private boolean isLikely(Likelihood likelihood) {
        return likelihood == Likelihood.POSSIBLE ||
                likelihood == Likelihood.LIKELY ||
                likelihood == Likelihood.VERY_LIKELY;
    }


    private String analyzeContentWithGemini(String content) throws Exception {
        VertexAI vertexAI = null;
        try {
            vertexAI = new VertexAI(geminiProperties.getProjectId(), geminiProperties.getLocation());
            GenerativeModel model = new GenerativeModel(geminiProperties.getModel(), vertexAI);

            String prompt = String.format("""
                    You are a content moderator. Analyze this blog (text + images) for violations.
                    IMAGE SAFETY RULES:
                    Images pre-checked by Google Vision API with 5 categories:
                    - Adult: Pornography, sexual content
                    - Racy: Suggestive, revealing content
                    - Violence: Gore, blood, terrorism
                    - Medical: Sensitive medical content, surgery
                    - Spoof: Fake, manipulated, deepfake
                    STRICT LEVELS:
                    ✅VERY_UNLIKELY, UNLIKELY = SAFE (isSafe can be true); POSSIBLE, LIKELY, VERY_LIKELY = REJECT (isSafe MUST be false)
                    CRITICAL RULE: If ANY image shows POSSIBLE/LIKELY/VERY_LIKELY in ANY category → isSafe = false immediately. No exceptions.
                    TEXT VIOLATIONS:
                    Profanity, adult content, drugs, violence, fraud, spam, hate speech, misinformation, personal attacks.
                    CONTENT:
                    ---
                    %s
                    ---
                    RESPONSE (JSON only, no markdown):
                    {
                      "isSafe": true/false,
                      "safetyScore": 0-100,
                      "violations": ["violation1", "violation2"],
                      "reason": "Brief explanation with \\n- bullet points"
                    }
                    Scoring: Start 100. Deduct 20-30 for POSSIBLE, 40-50 for LIKELY, 60-80 for VERY_LIKELY.
                                        
                    IMPORTANT: 
                    - Only list violations in "reason". Do NOT list safe items.
                    - Use exact image labels: "Cover Image" or "Section X Image"
                    - If SAFE: "All content passed safety checks. No violations detected."
                    - If VIOLATIONS: "Violations detected:\\n- Cover Image: Adult content (POSSIBLE)\\n- Section 2 Image: Violence (LIKELY)\\n- Safety score: 55/100"
                    """, content);

            GenerateContentResponse response = model.generateContent(prompt);

            String responseText = ResponseHandler.getText(response);

            return responseText;

        } catch (UnauthenticatedException e) {
            throw new Exception("Google Cloud authentication failed. Please verify your credentials are properly configured.", e);
        } catch (PermissionDeniedException e) {
            throw new Exception("Permission denied to access Vertex AI. Please enable the API and verify permissions.", e);
        } catch (NotFoundException e) {
            throw new Exception("Vertex AI resource not found. Please verify project ID and location configuration.", e);
        } catch (ApiException e) {
            throw new Exception("Vertex AI API error: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new Exception("Network error connecting to Vertex AI: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new Exception("Unexpected error calling Gemini AI: " + e.getMessage(), e);
        } finally {
            if (vertexAI != null) {
                vertexAI.close();
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
            Pattern pattern = Pattern.compile(reasonPattern);
            Matcher matcher = pattern.matcher(json);
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
        }
        blog.setStatus(newStatus);
        blogRepository.save(blog);
    }

    /**
     * TEST METHOD: Check image safety using Google Vision API SafeSearch
     * This is a public method for testing purposes
     *
     * @param file Image file to check
     * @return ImageSafetyCheckResponse with SafeSearch results
     */
    public ImageSafetyCheckResponse testImageSafety(MultipartFile file) {
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                log.error("No file provided for image safety check");
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }

            log.info("Testing image safety for file: {} (size: {} bytes)",
                    file.getOriginalFilename(), file.getSize());

            // Convert file to bytes
            byte[] imageBytes = file.getBytes();
            ByteString imgBytes = ByteString.copyFrom(imageBytes);

            // Create Vision API request
            Image img = Image.newBuilder()
                    .setContent(imgBytes)
                    .build();

            Feature feature = Feature.newBuilder()
                    .setType(Type.SAFE_SEARCH_DETECTION)
                    .build();

            AnnotateImageRequest visionRequest = AnnotateImageRequest.newBuilder()
                    .addFeatures(feature)
                    .setImage(img)
                    .build();

            // Call Vision API
            BatchAnnotateImagesResponse response = imageAnnotatorClient.batchAnnotateImages(List.of(visionRequest));

            if (response.getResponsesCount() == 0) {
                log.error("No response from Vision API for file: {}", file.getOriginalFilename());
                throw new AppException(ErrorCode.AI_MODERATION_FAILED);
            }

            AnnotateImageResponse imageResponse = response.getResponses(0);

            // Check for errors
            if (imageResponse.hasError()) {
                log.error("Vision API error for file {}: {}",
                        file.getOriginalFilename(),
                        imageResponse.getError().getMessage());
                throw new AppException(ErrorCode.AI_MODERATION_FAILED);
            }

            SafeSearchAnnotation annotation = imageResponse.getSafeSearchAnnotation();

            // Build response
            ImageSafetyCheckResponse.SafeSearchDetails details = ImageSafetyCheckResponse.SafeSearchDetails.builder()
                    .adult(annotation.getAdult().name())
                    .violence(annotation.getViolence().name())
                    .racy(annotation.getRacy().name())
                    .medical(annotation.getMedical().name())
                    .spoof(annotation.getSpoof().name())
                    .build();

            // Determine if image is safe (reuse existing isLikely method)
            boolean isSafe = !isLikely(annotation.getAdult()) &&
                    !isLikely(annotation.getViolence()) &&
                    !isLikely(annotation.getRacy()) &&
                    !isLikely(annotation.getMedical());

            String summary = buildTestSummary(annotation, isSafe);

            log.info("Image safety test completed for {}: isSafe={}", file.getOriginalFilename(), isSafe);

            return ImageSafetyCheckResponse.builder()
                    .imageUrl(file.getOriginalFilename())
                    .isSafe(isSafe)
                    .safeSearchDetails(details)
                    .summary(summary)
                    .build();

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error testing image safety: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.AI_MODERATION_FAILED);
        }
    }

    /**
     * Build human-readable summary for test results
     */
    private String buildTestSummary(SafeSearchAnnotation annotation, boolean isSafe) {
        if (isSafe) {
            return "Image is safe - no violations detected";
        }

        StringBuilder summary = new StringBuilder("Potential violations detected: ");
        boolean first = true;

        if (isLikely(annotation.getAdult())) {
            summary.append("Adult content (").append(annotation.getAdult().name()).append(")");
            first = false;
        }

        if (isLikely(annotation.getViolence())) {
            if (!first) summary.append(", ");
            summary.append("Violence (").append(annotation.getViolence().name()).append(")");
            first = false;
        }

        if (isLikely(annotation.getRacy())) {
            if (!first) summary.append(", ");
            summary.append("Racy content (").append(annotation.getRacy().name()).append(")");
            first = false;
        }

        if (isLikely(annotation.getMedical())) {
            if (!first) summary.append(", ");
            summary.append("Medical content (").append(annotation.getMedical().name()).append(")");
        }

        return summary.toString();
    }
}

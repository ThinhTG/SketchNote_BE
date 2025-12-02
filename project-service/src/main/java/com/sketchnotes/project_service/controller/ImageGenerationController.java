package com.sketchnotes.project_service.controller;

import com.sketchnotes.project_service.client.CreditClient;
import com.sketchnotes.project_service.dtos.ApiResponse;
import com.sketchnotes.project_service.dtos.request.ImageGenerationRequest;
import com.sketchnotes.project_service.dtos.request.UseCreditRequest;
import com.sketchnotes.project_service.dtos.response.CreditBalanceResponse;
import com.sketchnotes.project_service.dtos.response.ImageGenerationResponse;
import com.sketchnotes.project_service.exception.AppException;
import com.sketchnotes.project_service.exception.ErrorCode;
import com.sketchnotes.project_service.service.IAiImageService;
import com.sketchnotes.project_service.service.IImageGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller xử lý các yêu cầu liên quan đến việc tạo ảnh AI (Imagen/Vertex AI).
 * Base path: /api/images
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
@Slf4j
public class ImageGenerationController {

    private final IImageGenerationService imageGenerationService;
    private final IAiImageService aiImageService;
    private final CreditClient creditClient;
    
    // Cost configuration
    private static final int CREDITS_PER_IMAGE = 10; // 10 credits per generated image
    private static final int CREDITS_PER_BACKGROUND_REMOVAL = 5; // 5 credits per background removal
    
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<ImageGenerationResponse>> generateImage(
            Authentication authentication,
            @RequestBody @Valid ImageGenerationRequest request) {
        
        Long userId = extractUserId(authentication);
        log.info("User {} requesting image generation", userId);
        
        // Calculate required credits (base cost * number of images requested)
        int requiredCredits = CREDITS_PER_IMAGE;
        
        // Check if user has enough credits
        try {
            ResponseEntity<ApiResponse<Boolean>> checkResponse = creditClient.checkCredits(userId, requiredCredits);
            Boolean hasEnoughCredits = checkResponse.getBody().getResult();
            
            if (hasEnoughCredits == null || !hasEnoughCredits) {
                log.warn("User {} has insufficient credits. Required: {}", userId, requiredCredits);
                throw new AppException(ErrorCode.INSUFFICIENT_CREDITS);
            }
        } catch (Exception e) {
            log.error("Failed to check credits for user {}", userId, e);
            throw new AppException(ErrorCode.CREDIT_CHECK_FAILED);
        }
        
        // Generate image
        ImageGenerationResponse response = imageGenerationService.generateAndUploadImage(request);
        
        // Deduct credits after successful generation
        try {
            UseCreditRequest useCreditRequest = UseCreditRequest.builder()
                    .userId(userId)
                    .amount(requiredCredits)
                    .description("Generated " + response.getImageUrls().size() + " image(s)")
                    .referenceId(response.getImageUrls().get(0)) // Use first image URL as reference
                    .build();
            
            ResponseEntity<ApiResponse<CreditBalanceResponse>> useResponse = creditClient.useCredits(useCreditRequest);
            CreditBalanceResponse creditBalance = useResponse.getBody().getResult();
            
            log.info("Deducted {} credits from user {}. Remaining balance: {}", 
                    requiredCredits, userId, creditBalance.getCurrentBalance());
                    
        } catch (Exception e) {
            log.error("Failed to deduct credits for user {} after image generation", userId, e);
            // Image already generated, so we log the error but don't fail the request
        }
        
        return ResponseEntity.ok(ApiResponse.success(response, "Image generated successfully"));
    }

    @PostMapping(value = "/remove-background", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> removeBackground(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        try {
            Long userId = extractUserId(authentication);
            log.info("User {} requesting background removal", userId);
            
            // Check if user has enough credits
            try {
                ResponseEntity<ApiResponse<Boolean>> checkResponse = creditClient.checkCredits(userId, CREDITS_PER_BACKGROUND_REMOVAL);
                Boolean hasEnoughCredits = checkResponse.getBody().getResult();
                
                if (hasEnoughCredits == null || !hasEnoughCredits) {
                    log.warn("User {} has insufficient credits for background removal", userId);
                    return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).build();
                }
            } catch (Exception e) {
                log.error("Failed to check credits for user {}", userId, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            
            byte[] processedImage = aiImageService.removeBackground(file);
            
            // Deduct credits after successful processing
            try {
                UseCreditRequest useCreditRequest = UseCreditRequest.builder()
                        .userId(userId)
                        .amount(CREDITS_PER_BACKGROUND_REMOVAL)
                        .description("Background removal")
                        .build();
                
                creditClient.useCredits(useCreditRequest);
                log.info("Deducted {} credits from user {} for background removal", 
                        CREDITS_PER_BACKGROUND_REMOVAL, userId);
            } catch (Exception e) {
                log.error("Failed to deduct credits for user {} after background removal", userId, e);
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(processedImage);

        } catch (Exception e) {
            log.error("Error processing background removal", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Helper method để extract userId từ JWT token
     */
    private Long extractUserId(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return Long.parseLong(jwt.getClaim("userId"));
    }

}
package com.sketchnotes.project_service.controller;

import com.sketchnotes.project_service.service.IAiImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller for AI-powered image processing
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Image Processing", description = "APIs for AI-powered image processing")
public class AiImageController {

    private final IAiImageService aiImageService;

    @PostMapping(value = "/remove-background", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Remove background from image",
        description = "Upload an image and get back the same image with background removed (transparent PNG)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Background removed successfully",
            content = @Content(
                mediaType = "image/png",
                schema = @Schema(type = "string", format = "binary")
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid file format or empty file"),
        @ApiResponse(responseCode = "500", description = "Internal server error during processing")
    })
    public ResponseEntity<byte[]> removeBackground(
            @RequestParam("file") MultipartFile file
    ) {
        try {
            log.info("Received request to remove background from file: {}", file.getOriginalFilename());
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity
                    .badRequest()
                    .body(null);
            }
            
            // Process image
            byte[] processedImage = aiImageService.removeBackground(file);
            
            // Return processed image
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", 
                "no-bg-" + file.getOriginalFilename().replaceAll("\\.[^.]+$", ".png"));
            
            return new ResponseEntity<>(processedImage, headers, HttpStatus.OK);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error processing image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

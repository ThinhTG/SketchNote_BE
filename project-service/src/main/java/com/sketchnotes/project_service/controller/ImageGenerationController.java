package com.sketchnotes.project_service.controller;

import com.sketchnotes.project_service.dtos.ApiResponse;
import com.sketchnotes.project_service.dtos.request.ImageGenerationRequest;
import com.sketchnotes.project_service.dtos.response.ImageGenerationResponse;
import com.sketchnotes.project_service.dtos.response.ImagePromptResponse;
import com.sketchnotes.project_service.service.IAiImageService;
import com.sketchnotes.project_service.service.IImageGenerationService;
import com.sketchnotes.project_service.utils.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller xử lý các yêu cầu liên quan đến việc tạo ảnh AI (Imagen/Vertex AI).
 * Base path: /api/images
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageGenerationController {

    private final IImageGenerationService imageGenerationService;
    private final IAiImageService aiImageService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<ImageGenerationResponse>> generateImage(@RequestBody @Valid ImageGenerationRequest request) {

        ImageGenerationResponse response = imageGenerationService.generateAndUploadImage(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Image generated successfully"));
    }
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PagedResponse<ImagePromptResponse>>> getImageHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<ImagePromptResponse> history = imageGenerationService.getImageGenerations(page, size);
        return ResponseEntity.ok(ApiResponse.success(history, "Image history retrieved successfully"));
    }

    @PostMapping(value = "/remove-background", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> removeBackground(@RequestParam("file") MultipartFile file) {
        try {

            byte[] processedImage = aiImageService.removeBackground(file);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(processedImage);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
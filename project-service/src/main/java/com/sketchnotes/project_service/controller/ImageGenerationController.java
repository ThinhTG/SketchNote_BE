package com.sketchnotes.project_service.controller;

import com.sketchnotes.project_service.dtos.request.ImageGenerationRequest;
import com.sketchnotes.project_service.dtos.response.ImageGenerationResponse;
import com.sketchnotes.project_service.service.IImageGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các yêu cầu liên quan đến việc tạo ảnh AI (Imagen/Vertex AI).
 * Base path: /api/image
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageGenerationController {

    private final IImageGenerationService imageGenerationService;
    @PostMapping("/generate")
    public ResponseEntity<ImageGenerationResponse> generateImage(@RequestBody ImageGenerationRequest request) {
        log.info("Yêu cầu tạo ảnh mới được nhận. Prompt: {}", request.getPrompt());

        if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt không được để trống.");
        }

        ImageGenerationResponse response = imageGenerationService.generateAndUploadImage(request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
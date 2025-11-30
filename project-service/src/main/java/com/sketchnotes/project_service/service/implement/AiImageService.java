package com.sketchnotes.project_service.service.implement;

import com.sketchnotes.project_service.client.AiClient;
import com.sketchnotes.project_service.service.IAiImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation of AI Image Service
 * Handles communication with AI Background Remover service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiImageService implements IAiImageService {

    private final AiClient aiClient;

    @Override
    public byte[] removeBackground(MultipartFile file) throws Exception {
        try {
            log.info("Removing background from image: {}", file.getOriginalFilename());
            
            // Validate file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }
            
            // Call AI service
            ResponseEntity<byte[]> response = aiClient.removeBackground(file);
            
            if (response.getBody() == null) {
                throw new Exception("AI service returned empty response");
            }
            
            log.info("Successfully removed background from image: {}", file.getOriginalFilename());
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error removing background from image: {}", file.getOriginalFilename(), e);
            throw new Exception("Failed to remove background: " + e.getMessage(), e);
        }
    }
}

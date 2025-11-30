package com.sketchnotes.project_service.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for AI-powered image processing
 */
public interface IAiImageService {
    
    /**
     * Remove background from an image
     * @param file Image file to process
     * @return Processed image as byte array (PNG format)
     * @throws Exception if processing fails
     */
    byte[] removeBackground(MultipartFile file) throws Exception;
}

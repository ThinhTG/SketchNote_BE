package com.sketchnotes.project_service.service;

import com.sketchnotes.project_service.dtos.request.ImageGenerationRequest;
import com.sketchnotes.project_service.dtos.response.ImageGenerationResponse;

/**
 * Interface định nghĩa hợp đồng cho dịch vụ tạo ảnh.
 */
public interface IImageGenerationService {
    ImageGenerationResponse generateAndUploadImage(ImageGenerationRequest request);
}
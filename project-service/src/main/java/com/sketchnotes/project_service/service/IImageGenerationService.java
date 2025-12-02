package com.sketchnotes.project_service.service;

import com.sketchnotes.project_service.dtos.request.ImageGenerationRequest;
import com.sketchnotes.project_service.dtos.response.ImageGenerationResponse;
import com.sketchnotes.project_service.dtos.response.ImagePromptResponse;
import com.sketchnotes.project_service.utils.PagedResponse;

/**
 * Interface định nghĩa hợp đồng cho dịch vụ tạo ảnh.
 */
public interface IImageGenerationService {
    ImageGenerationResponse generateAndUploadImage(ImageGenerationRequest request);
    PagedResponse<ImagePromptResponse> getImageGenerations(int page, int size);
}
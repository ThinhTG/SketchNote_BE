package com.sketchnotes.project_service.service.implement;

import com.sketchnotes.project_service.client.AiClient;
import com.sketchnotes.project_service.exception.AppException;
import com.sketchnotes.project_service.exception.ErrorCode;
import com.sketchnotes.project_service.service.IAiImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation của AI Image Service sử dụng AiClient để gọi AI background remover
 */
@Service
@RequiredArgsConstructor
public class AiImageService implements IAiImageService {
    
    private final AiClient aiClient;
    
    @Override
    public byte[] removeBackground(MultipartFile file) throws Exception {
        
        ResponseEntity<byte[]> response = aiClient.removeBackground(file);
        
        if (response.getBody() == null) {
            throw new AppException(ErrorCode.IMAGE_REMOVAL_FAILED);
        }
        return response.getBody();
    }
}

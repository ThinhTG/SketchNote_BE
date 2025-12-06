package com.sketchnotes.project_service.service.implement;

import com.sketchnotes.project_service.config.S3Properties;
import com.sketchnotes.project_service.enums.FileContentType;
import com.sketchnotes.project_service.exception.AppException;
import com.sketchnotes.project_service.exception.ErrorCode;
import com.sketchnotes.project_service.service.IStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService implements IStorageService {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Override
    public Map<String, String> generatePresignedUrl(String fileName, FileContentType contentType) {
        String key = "notes/" + UUID.randomUUID() + "/" + fileName;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(key)
                .contentType(contentType.getMimeType())
                .build();

        PresignedPutObjectRequest preSigned = s3Presigner.presignPutObject(r -> r
                .signatureDuration(Duration.ofMinutes(s3Properties.getPresignExpiration()))
                .putObjectRequest(objectRequest)
        );

        return Map.of(
                "uploadUrl", preSigned.url().toString(),
                "strokeUrl", "https://" + s3Properties.getBucketName() + ".s3." +
                        s3Properties.getRegion() + ".amazonaws.com/" + key
        );
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            // Extract key from URL
            // URL format: https://bucket-name.s3.region.amazonaws.com/key
            String key = extractKeyFromUrl(fileUrl);
            
            if (key != null && !key.isEmpty()) {
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(s3Properties.getBucketName())
                        .key(key)
                        .build();
                
                s3Client.deleteObject(deleteRequest);
            }
        } catch (Exception e) {
            // Don't throw exception to avoid blocking the version cleanup
        }
    }

    @Override
    public String copyFile(String sourceFileUrl) {
        try {
            // Extract source key from URL
            String sourceKey = extractKeyFromUrl(sourceFileUrl);
            
            if (sourceKey == null || sourceKey.isEmpty()) {
                throw new IllegalArgumentException("Invalid source file URL");
            }
            
            // Extract filename from source key
            String fileName = sourceKey.substring(sourceKey.lastIndexOf("/") + 1);
            
            // Generate new key with UUID
            String destinationKey = "notes/" + UUID.randomUUID() + "/" + fileName;
            
            // Copy object in S3
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                    .sourceBucket(s3Properties.getBucketName())
                    .sourceKey(sourceKey)
                    .destinationBucket(s3Properties.getBucketName())
                    .destinationKey(destinationKey)
                    .build();
            
            s3Client.copyObject(copyRequest);
            
            // Return new file URL
            return "https://" + s3Properties.getBucketName() + ".s3." +
                    s3Properties.getRegion() + ".amazonaws.com/" + destinationKey;
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy file: " + e.getMessage(), e);
        }
    }

    private String extractKeyFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        try {
            // Escape the dots in the regex to match literally
            String[] parts = fileUrl.split("\\.amazonaws\\.com/");
            if (parts.length > 1) {
                // URL decode the key to handle spaces and special characters
                return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new AppException(ErrorCode.FILE_URL_INVALID);
        }
        
        return null;
    }
}

package com.sketchnotes.project_service.service.implement;

import com.sketchnotes.project_service.config.S3Properties;
import com.sketchnotes.project_service.enums.FileContentType;
import com.sketchnotes.project_service.service.IStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService implements IStorageService {

    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    @Override
    public Map<String, String> generatePresignedUrl(String fileName, FileContentType contentType) {
        String key = "notes/" + UUID.randomUUID() + "/" + fileName;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(key)
                .contentType(contentType.getMimeType())
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(r -> r
                .signatureDuration(Duration.ofMinutes(s3Properties.getPresignExpiration()))
                .putObjectRequest(objectRequest)
        );

        return Map.of(
                "uploadUrl", presigned.url().toString(),
                "strokeUrl", "https://" + s3Properties.getBucketName() + ".s3." +
                        s3Properties.getRegion() + ".amazonaws.com/" + key
        );
    }
}

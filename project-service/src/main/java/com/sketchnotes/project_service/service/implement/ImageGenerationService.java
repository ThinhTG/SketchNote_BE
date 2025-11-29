package com.sketchnotes.project_service.service.implement;

// Imports cần thiết
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sketchnotes.project_service.config.GeminiProperties;
import com.sketchnotes.project_service.config.S3Properties;
import com.sketchnotes.project_service.dtos.request.ImageGenerationRequest;
import com.sketchnotes.project_service.dtos.response.ImageGenerationResponse;
import com.sketchnotes.project_service.enums.ImageType;
import com.sketchnotes.project_service.exception.AppException;
import com.sketchnotes.project_service.exception.ErrorCode;
import com.sketchnotes.project_service.service.IImageGenerationService;
// Imports cho Vertex AI SDK
import com.google.cloud.aiplatform.v1.EndpointName;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
// Imports S3
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Triển khai dịch vụ tạo ảnh sử dụng Imagen 3.0 trên Vertex AI.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageGenerationService implements IImageGenerationService {

    // Inject các dependency cần thiết
    private final GeminiProperties geminiProperties; // Chứa project-id, location, model
    private final S3Properties s3Properties; // Cấu hình S3
    private final S3Client s3Client; // Client S3
    private final PredictionServiceClient predictionServiceClient; // Client Vertex AI đã cấu hình
    private final ObjectMapper objectMapper = new ObjectMapper(); // Dùng để parse JSON

    /**
     * Phương thức chính: Tạo ảnh bằng Imagen 3.0 và Upload lên S3.
     */
    @Override
    public ImageGenerationResponse generateAndUploadImage(ImageGenerationRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("Bắt đầu tạo ảnh bằng Imagen 3.0 với prompt: {}", request.getPrompt());

            // Bước 1: Tạo ảnh bằng Vertex AI
            List<byte[]> imagesBytes = generateImagesWithImagen(request);

            // Bước 2: Upload tất cả ảnh lên S3
            ImageType imageType = request.getImageType() != null ? request.getImageType() : ImageType.JPEG; // Imagen thường dùng JPEG
            List<String> s3Urls = new ArrayList<>();
            String primaryFileName = null;

            for (byte[] imageBytes : imagesBytes) {
                String fileName = generateFileName(imageType);
                if (primaryFileName == null) primaryFileName = fileName;

                String s3Url = uploadToS3(imageBytes, fileName, imageType);
                s3Urls.add(s3Url);
            }

            long generationTime = System.currentTimeMillis() - startTime;
            log.info("Tạo và upload thành công {} ảnh trong {}ms", s3Urls.size(), generationTime);

            // Bước 3: Trả về DTO phản hồi
            return ImageGenerationResponse.builder()
                    .imageUrls(s3Urls)
                    .prompt(request.getPrompt())
                    .generationTime(generationTime)
                    .fileName(primaryFileName)
                    .build();

        } catch (Exception e) {
            log.error("Lỗi khi tạo ảnh bằng Imagen: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.IMAGE_GENERATION_FAILED);
        }
    }

    /**
     * Gọi API Imagen 3.0 trên Vertex AI để tạo ảnh.
     */
    private List<byte[]> generateImagesWithImagen(ImageGenerationRequest request) {
        try {
            String enhancedPrompt = buildEnhancedPrompt(request);

            // 1. Xây dựng Resource Name (Endpoint)
            // Dùng định dạng chuẩn của mô hình Google Publisher trên Vertex AI
            EndpointName endpointName = EndpointName.ofProjectLocationPublisherModelName(
                    geminiProperties.getProjectId(),
                    geminiProperties.getLocation(),
                    "google",
                    geminiProperties.getModel() // Phải là "imagen-3.0-generate-002"
            );

            log.info("Gọi Imagen 3.0 tại endpoint: {}", endpointName.toString());

            String requestJson = buildImagenRequestJson(enhancedPrompt, geminiProperties.getNumImages(), request.getWidth(), request.getHeight());

            // 3. Chuyển JSON string thành Protobuf Value (định dạng SDK Vertex AI sử dụng)
            Value.Builder instanceBuilder = Value.newBuilder();
            // Sử dụng JsonFormat.parser() để parse JSON thành Protobuf Value
            JsonFormat.parser().merge(requestJson, instanceBuilder);
            Value instance = instanceBuilder.build();

            // 4. Gọi API Vertex AI (phương thức predict)
            // Tham số thứ 3 (parameters) để trống hoặc dùng cho các cấu hình bổ sung khác
            com.google.cloud.aiplatform.v1.PredictResponse response = predictionServiceClient.predict(
                    endpointName,
                    java.util.Collections.singletonList(instance),
                    Value.newBuilder().build()
            );

            // 5. Trích xuất ảnh Base64 từ phản hồi
            return extractImagesFromVertexAIResponse(response.getPredictionsList());

        } catch (Exception e) {
            log.error("Lỗi khi gọi Imagen API: {}", e.getMessage(), e);
            throw new RuntimeException("Thất bại khi tạo ảnh bằng Imagen: " + e.getMessage(), e);
        }
    }

    /**
     * Xây dựng chuỗi JSON Request Body cho API Imagen 3.0.
     */
    private String buildImagenRequestJson(String prompt, int sampleCount, Integer width, Integer height) {
        String aspectRatio = "1:1";

        // Cần phải escape các dấu quote (") trong prompt để JSON không bị lỗi
        return String.format(
                "{\"prompt\": \"%s\", \"number_of_images\": %d, \"output_mime_type\": \"image/jpeg\", \"aspect_ratio\": \"%s\"}",
                prompt.replace("\"", "\\\""),
                sampleCount,
                aspectRatio
        );
    }

    /**
     * Trích xuất Base64 Images từ danh sách Predictions (Protobuf Value) của Vertex AI.
     */
    private List<byte[]> extractImagesFromVertexAIResponse(List<Value> predictions) {
        List<byte[]> images = new ArrayList<>();

        for (Value prediction : predictions) {
            try {
                // Chuyển Protobuf Value thành JSON string để dễ dàng parse bằng Jackson ObjectMapper
                String predictionJson = JsonFormat.printer().print(prediction);
                JsonNode root = objectMapper.readTree(predictionJson);

                // Tìm kiếm chuỗi base64EncodedImage
                JsonNode bytesNode = root.get("bytesBase64Encoded");
                if (bytesNode != null && !bytesNode.asText().isEmpty()) {
                    byte[] imageBytes = Base64.getDecoder().decode(bytesNode.asText());
                    images.add(imageBytes);
                }
            } catch (Exception e) {
                log.error("Lỗi khi phân tích phản hồi Vertex AI", e);
            }
        }

        if (images.isEmpty()) {
            throw new RuntimeException("Không tìm thấy dữ liệu ảnh trong phản hồi Vertex AI");
        }
        return images;
    }


    /**
     * Xây dựng Prompt nâng cao, thêm style và yêu cầu icon/xóa phông.
     */
    private String buildEnhancedPrompt(ImageGenerationRequest request) {
        StringBuilder prompt = new StringBuilder(request.getPrompt());

        if (request.getStyle() != null && !request.getStyle().isEmpty()) {
            prompt.append(", ").append(request.getStyle()).append(" style");
        }

        prompt.append(", high quality, detailed, professional");

        // Yêu cầu tạo icon
        prompt.append(", icon concept, ");

        // Yêu cầu xóa phông (dựa trên tham số removeBackground)
        if (request.getRemoveBackground() != null && request.getRemoveBackground()) {
            prompt.append("transparent background");
        } else {
            prompt.append("isolated on white background");
        }

        return prompt.toString();
    }

    /**
     * Upload image bytes lên AWS S3.
     */
    private String uploadToS3(byte[] imageBytes, String fileName, ImageType imageType) {
        try {
            String key = "generated-images/" + fileName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .contentType(imageType.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));

            // Xây dựng URL S3
            String s3Url = String.format("https://%s.s3.%s.amazonaws.com/%s",
                    s3Properties.getBucketName(),
                    s3Properties.getRegion(),
                    key);

            log.info("Ảnh đã được upload lên S3: {}", s3Url);
            return s3Url;

        } catch (Exception e) {
            log.error("Lỗi khi upload lên S3: {}", e.getMessage(), e);
            throw new RuntimeException("Thất bại khi upload ảnh lên S3: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo tên file duy nhất dựa trên timestamp và UUID.
     */
    private String generateFileName(ImageType imageType) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("imagen_%s_%s%s", timestamp, uuid, imageType.getExtension());
    }
}
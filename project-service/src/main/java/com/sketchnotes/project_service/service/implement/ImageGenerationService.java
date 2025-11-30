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
import com.sketchnotes.project_service.service.IAiImageService;
import com.sketchnotes.project_service.service.IImageGenerationService;
import com.sketchnotes.project_service.utils.ByteArrayMultipartFile;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Triển khai dịch vụ tạo ảnh sử dụng Imagen 3.0 trên Vertex AI.
 * Với icon: Gen ảnh → Xóa background bằng AI
 * Với ảnh thường: Chỉ gen ảnh
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
    private final IAiImageService aiImageService; // Service xóa background
    private final ObjectMapper objectMapper = new ObjectMapper(); // Dùng để parse JSON

    /**
     * Phương thức chính: Tạo ảnh bằng Imagen 3.0 và Upload lên S3.
     * Nếu là icon, sẽ xóa background trước khi upload.
     */
    @Override
    public ImageGenerationResponse generateAndUploadImage(ImageGenerationRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            boolean isIcon = request.getIsIcon() != null && request.getIsIcon();
            log.info("Bắt đầu tạo {} bằng Imagen 3.0 với prompt: {}", 
                    isIcon ? "icon" : "ảnh", request.getPrompt());

            // Bước 1: Tạo ảnh bằng Vertex AI
            List<byte[]> imagesBytes = generateImagesWithImagen(request);
            log.info("Vertex AI đã tạo {} ảnh", imagesBytes.size());

            // Bước 2: Xử lý ảnh (xóa background nếu là icon)
            if (isIcon) {
                log.info("Đang xóa background cho {} icon...", imagesBytes.size());
                imagesBytes = removeBackgroundFromImages(imagesBytes);
                log.info("Đã xóa xong background, có {} ảnh đã xử lý", imagesBytes.size());
            }

            // Bước 3: Upload tất cả ảnh lên S3
            List<String> s3Urls = new ArrayList<>();
            String primaryFileName = null;

            for (byte[] imageBytes : imagesBytes) {
                String fileName = generateFileName(ImageType.PNG);
                if (primaryFileName == null) primaryFileName = fileName;

                String s3Url = uploadToS3(imageBytes, fileName, ImageType.PNG);
                s3Urls.add(s3Url);
            }

            long generationTime = System.currentTimeMillis() - startTime;
            log.info("Tạo và upload thành công {} {} trong {}ms", 
                    s3Urls.size(), isIcon ? "icon" : "ảnh", generationTime);

            // Bước 4: Trả về DTO phản hồi
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
     * Xóa background cho TẤT CẢ ảnh trong danh sách sử dụng AI Background Remover
     */
    private List<byte[]> removeBackgroundFromImages(List<byte[]> imagesBytes) {
        List<byte[]> processedImages = new ArrayList<>();
        
        log.info("=== BẮT ĐẦU XÓA BACKGROUND CHO {} ẢNH ===", imagesBytes.size());
        
        for (int i = 0; i < imagesBytes.size(); i++) {
            try {
                byte[] imageBytes = imagesBytes.get(i);
                log.info("Xử lý ảnh {}/{} - Size: {} bytes", i + 1, imagesBytes.size(), imageBytes.length);
                
                // Tạo MultipartFile từ byte array sử dụng custom implementation
                MultipartFile multipartFile = new ByteArrayMultipartFile(
                    imageBytes,
                    "file",
                    "temp_image_" + i + ".png",
                    "image/png"
                );
                
                log.info("Đã tạo MultipartFile, đang gọi AI service...");
                
                // Gọi AI service để xóa background
                byte[] processedImage = aiImageService.removeBackground(multipartFile);
                
                log.info("AI service trả về ảnh đã xóa background - Size: {} bytes", processedImage.length);
                
                processedImages.add(processedImage);
                
                log.info("✓ Đã xóa background cho ảnh {}/{}", i + 1, imagesBytes.size());
                
            } catch (Exception e) {
                log.error("✗ Lỗi khi xóa background cho ảnh {}: {}", i, e.getMessage(), e);
                // Nếu lỗi, giữ nguyên ảnh gốc
                processedImages.add(imagesBytes.get(i));
                log.warn("Sử dụng ảnh gốc cho ảnh {}", i);
            }
        }
        
        log.info("=== HOÀN TẤT XÓA BACKGROUND: {}/{} ảnh thành công ===", processedImages.size(), imagesBytes.size());
        
        return processedImages;
    }

    private List<byte[]> generateImagesWithImagen(ImageGenerationRequest request) {
        try {
            String enhancedPrompt = buildEnhancedPrompt(request);

            // 1. Xây dựng Resource Name (Endpoint)
            EndpointName endpointName = EndpointName.ofProjectLocationPublisherModelName(
                    geminiProperties.getProjectId(),
                    geminiProperties.getLocation(),
                    "google",
                    geminiProperties.getModel()
            );

            log.info("Gọi Imagen 3.0 tại endpoint: {}", endpointName.toString());
            log.info("Yêu cầu tạo {} ảnh", geminiProperties.getNumImages());

            // 2. Xây dựng instance (chỉ chứa prompt)
            String instanceJson = buildInstanceJson(enhancedPrompt);
            Value.Builder instanceBuilder = Value.newBuilder();
            JsonFormat.parser().merge(instanceJson, instanceBuilder);
            Value instance = instanceBuilder.build();

            // 3. Xây dựng parameters (chứa sampleCount)
            String parametersJson = buildParametersJson(geminiProperties.getNumImages());
            Value.Builder parametersBuilder = Value.newBuilder();
            JsonFormat.parser().merge(parametersJson, parametersBuilder);
            Value parameters = parametersBuilder.build();

            // 4. Gọi API Vertex AI với đúng format
            com.google.cloud.aiplatform.v1.PredictResponse response = predictionServiceClient.predict(
                    endpointName,
                    java.util.Collections.singletonList(instance),
                    parameters
            );

            // 5. Trích xuất ảnh Base64 từ phản hồi
            return extractImagesFromVertexAIResponse(response.getPredictionsList());

        } catch (Exception e) {
            log.error("Lỗi khi gọi Imagen API: {}", e.getMessage(), e);
            throw new RuntimeException("Thất bại khi tạo ảnh bằng Imagen: " + e.getMessage(), e);
        }
    }

    private String buildInstanceJson(String prompt) {
        return String.format(
                "{\"prompt\": \"%s\"}",
                prompt.replace("\"", "\\\"")
        );
    }

    private String buildParametersJson(int sampleCount) {
        return String.format(
                "{\"sampleCount\": %d}",
                sampleCount
        );
    }

    private List<byte[]> extractImagesFromVertexAIResponse(List<Value> predictions) {
        List<byte[]> images = new ArrayList<>();

        for (Value prediction : predictions) {
            try {
                String predictionJson = JsonFormat.printer().print(prediction);
                JsonNode root = objectMapper.readTree(predictionJson);

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
        
        log.info("Đã trích xuất {} ảnh từ Vertex AI response", images.size());
        return images;
    }

    private String buildEnhancedPrompt(ImageGenerationRequest request) {
        StringBuilder prompt = new StringBuilder(request.getPrompt());
        
        boolean isIcon = request.getIsIcon() != null && request.getIsIcon();
        
        if (isIcon) {
            prompt.append(", simple icon design, clean lines, minimalist");
            prompt.append(", flat design, vector style");
            prompt.append(", centered composition, white background");
            prompt.append(", high quality, professional icon");
        } else {
            prompt.append(", ").append(ImageType.PNG).append(" style");
            prompt.append(", high quality, detailed, professional");
            prompt.append(", realistic, vibrant colors");
        }
        
        return prompt.toString();
    }

    private String uploadToS3(byte[] imageBytes, String fileName, ImageType imageType) {
        try {
            String key = "generated-images/" + fileName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .contentType(imageType.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));

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

    private String generateFileName(ImageType imageType) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("imagen_%s_%s%s", timestamp, uuid, imageType.getExtension());
    }
}
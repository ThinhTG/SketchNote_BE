package com.sketchnotes.project_service.service.implement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sketchnotes.project_service.client.CreditClient;
import com.sketchnotes.project_service.client.IUserClient;
import com.sketchnotes.project_service.config.GeminiProperties;
import com.sketchnotes.project_service.config.S3Properties;
import com.sketchnotes.project_service.dtos.ApiResponse;
import com.sketchnotes.project_service.dtos.request.ImageGenerationRequest;
import com.sketchnotes.project_service.dtos.request.UseCreditRequest;
import com.sketchnotes.project_service.dtos.response.ImageGenerationResponse;
import com.sketchnotes.project_service.dtos.response.ImagePromptResponse;
import com.sketchnotes.project_service.dtos.response.UserResponse;
import com.sketchnotes.project_service.entity.ImagePrompt;
import com.sketchnotes.project_service.enums.ImageType;
import com.sketchnotes.project_service.exception.AppException;
import com.sketchnotes.project_service.exception.ErrorCode;
import com.sketchnotes.project_service.repository.IImagePromptRepository;
import com.sketchnotes.project_service.service.IAiImageService;
import com.sketchnotes.project_service.service.IImageGenerationService;
import com.sketchnotes.project_service.utils.ByteArrayMultipartFile;
import com.google.cloud.aiplatform.v1.EndpointName;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import com.sketchnotes.project_service.utils.PagedResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import lombok.RequiredArgsConstructor;
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
@Service
@RequiredArgsConstructor
public class ImageGenerationService implements IImageGenerationService {

    private final GeminiProperties geminiProperties;
    private final S3Properties s3Properties;
    private final S3Client s3Client;
    private final PredictionServiceClient predictionServiceClient;
    private final IAiImageService aiImageService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private  final IImagePromptRepository imagePromptRepository;
    private final IUserClient userClient;
    private final CreditClient creditClient;
    private final int CREDIT_COST_PER_IMAGE = 5;
    private final int CREDIT_COST_PER_BACKGROUND_REMOVAL = 10;

    /**
     * Phương thức chính: Tạo ảnh bằng Imagen 3.0 và Upload lên S3.
     * Nếu là icon, sẽ xóa background trước khi upload.
     */
    @Override
    public ImageGenerationResponse generateAndUploadImage(ImageGenerationRequest request) {
        //check xem user co du token hay khong
        if(request.getIsIcon() != null && request.getIsIcon()) {
            ApiResponse<Boolean> creditCheckResponse = creditClient.checkCredits(CREDIT_COST_PER_BACKGROUND_REMOVAL).getBody();
            if (!creditCheckResponse.getResult()) {
                throw new AppException(ErrorCode.INSUFFICIENT_CREDITS);
            }

            creditClient.useCredits(UseCreditRequest.builder()
                    .amount(CREDIT_COST_PER_BACKGROUND_REMOVAL)
                    .description(request.getPrompt() + " - Background removal")
                    .build());
        } else {


            ApiResponse<Boolean> creditCheckResponse = creditClient.checkCredits(CREDIT_COST_PER_IMAGE).getBody();
            if ( !creditCheckResponse.getResult()) {
                throw new AppException(ErrorCode.INSUFFICIENT_CREDITS);
            }

            creditClient.useCredits(UseCreditRequest.builder()
                    .amount(CREDIT_COST_PER_IMAGE)
                    .description(request.getPrompt() + " - Image generation")
                    .build());

        }
        //bat dau gen anh va icon
        long startTime = System.currentTimeMillis();

        try {
            boolean isIcon = request.getIsIcon() != null && request.getIsIcon();

            List<byte[]> imagesBytes = generateImagesWithImagen(request);
            if (isIcon) {
                imagesBytes = removeBackgroundFromImages(imagesBytes);
            }

            // Bước 3: Upload tất cả ảnh lên S3
            List<String> s3Urls = new ArrayList<>();
            String primaryFileName = null;
            ApiResponse<UserResponse> user = userClient.getCurrentUser();
            for (byte[] imageBytes : imagesBytes) {
                String fileName = generateFileName(ImageType.PNG);
                if (primaryFileName == null) primaryFileName = fileName;

                String s3Url = uploadToS3(imageBytes, fileName, ImageType.PNG);
                s3Urls.add(s3Url);
                // Lưu prompt và URL ảnh vào DB
                imagePromptRepository.save(ImagePrompt.builder()
                        .imageUrl(s3Url)
                        .createdAt(LocalDateTime.now())
                        .ownerId(user.getResult().getId())
                        .build());
            }

            long generationTime = System.currentTimeMillis() - startTime;

            // Bước 4: Trả về DTO phản hồi
            return ImageGenerationResponse.builder()
                    .imageUrls(s3Urls)
                    .prompt(request.getPrompt())
                    .generationTime(generationTime)
                    .build();

        } catch (Exception e) {
            throw new AppException(ErrorCode.IMAGE_GENERATION_FAILED);
        }
    }

    @Override
    public PagedResponse<ImagePromptResponse> getImageGenerations(int page, int size) {
        ApiResponse<UserResponse> user = userClient.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        var imagePromptsPage = imagePromptRepository.findByOwnerIdAndDeletedAtIsNullOrderByCreatedAtDesc(user.getResult().getId(), pageable);
        var imageGenerations = imagePromptsPage.getContent().stream()
                .map(imagePrompt -> ImagePromptResponse.builder()
                        .imagePromptId(imagePrompt.getImagePromptId())
                        .imageUrl(imagePrompt.getImageUrl())
                        .createdAt(imagePrompt.getCreatedAt())
                        .build())
                .toList();
        return new PagedResponse<>(
                imageGenerations,
                imagePromptsPage.getNumber(),
                imagePromptsPage.getSize(),
                (int) imagePromptsPage.getTotalElements(),
                imagePromptsPage.getTotalPages(),
                imagePromptsPage.isLast()
        );
    }
    /**
     * Xóa background cho TẤT CẢ ảnh trong danh sách sử dụng AI Background Remover
     */
    private List<byte[]> removeBackgroundFromImages(List<byte[]> imagesBytes) {
        List<byte[]> processedImages = new ArrayList<>();


        for (int i = 0; i < imagesBytes.size(); i++) {
            try {
                byte[] imageBytes = imagesBytes.get(i);

                // Tạo MultipartFile từ byte array sử dụng custom implementation
                MultipartFile multipartFile = new ByteArrayMultipartFile(
                        imageBytes,
                        "file",
                        "temp_image_" + i + ".png",
                        "image/png"
                );


                byte[] processedImage = aiImageService.removeBackground(multipartFile);

                processedImages.add(processedImage);

            } catch (Exception e) {
                throw new AppException(ErrorCode.IMAGE_REMOVAL_FAILED);
            }
        }

        return processedImages;
    }

    private List<byte[]> generateImagesWithImagen(ImageGenerationRequest request) {
        try {
            String enhancedPrompt = buildEnhancedPrompt(request);


            EndpointName endpointName = EndpointName.ofProjectLocationPublisherModelName(
                    geminiProperties.getProjectId(),
                    geminiProperties.getLocation(),
                    "google",
                    geminiProperties.getModel()
            );

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
                throw new AppException(ErrorCode.IMAGE_EXTRACTION_FAILED);
            }
        }

        if (images.isEmpty()) {
            throw new RuntimeException("Không tìm thấy dữ liệu ảnh trong phản hồi Vertex AI");
        }

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

            return s3Url;

        } catch (Exception e) {
            throw new RuntimeException("Thất bại khi upload ảnh lên S3: " + e.getMessage(), e);
        }
    }

    private String generateFileName(ImageType imageType) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("imagen_%s_%s%s", timestamp, uuid, imageType.getExtension());
    }
}
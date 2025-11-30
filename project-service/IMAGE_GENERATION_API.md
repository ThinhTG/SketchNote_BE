# API Gen Ảnh với Tích Hợp Xóa Background

## Tổng quan
API này tạo ảnh bằng Google Vertex AI Imagen 3.0 với 2 chế độ:
1. **Icon Mode** (`isIcon: true`): Gen icon → Xóa background bằng AI → Trả về icon có background trong suốt
2. **Image Mode** (`isIcon: false`): Chỉ gen ảnh thường với chi tiết cao

## Endpoint

### POST `/api/images/generate`

Tạo ảnh hoặc icon bằng AI.

**Request Body:**
```json
{
  "prompt": "a cute cat",
  "width": 1024,
  "height": 1024,
  "isIcon": false
}
```

**Parameters:**
- `prompt` (string, required): Mô tả ảnh/icon cần tạo
- `width` (integer, optional): Chiều rộng ảnh (mặc định: 1024)
- `height` (integer, optional): Chiều cao ảnh (mặc định: 1024)
- `isIcon` (boolean, optional): 
  - `true`: Gen icon và xóa background
  - `false` hoặc không truyền: Gen ảnh thường

**Response:**
```json
{
  "imageUrls": [
    "https://bucket.s3.region.amazonaws.com/generated-images/imagen_20231130_143000_abc123.png"
  ],
  "prompt": "a cute cat",
  "generationTime": 5432,
  "fileName": "imagen_20231130_143000_abc123.png"
}
```

**Status Codes:**
- `200 OK`: Tạo ảnh thành công
- `400 Bad Request`: Prompt không hợp lệ
- `500 Internal Server Error`: Lỗi khi tạo ảnh

## Quy trình xử lý

### 1. Icon Mode (isIcon: true)
```
User Request
    ↓
[1] Gen icon với Imagen 3.0
    - Prompt được tối ưu cho icon: "simple icon design, clean lines, minimalist"
    - Background: white (để dễ xóa)
    ↓
[2] Xóa background bằng AI Background Remover
    - Chuyển byte[] → MultipartFile
    - Gọi AI service xóa background
    - Nhận PNG với background trong suốt
    ↓
[3] Upload lên S3
    ↓
Return URL
```

### 2. Image Mode (isIcon: false)
```
User Request
    ↓
[1] Gen ảnh với Imagen 3.0
    - Prompt được tối ưu cho ảnh: "realistic, vibrant colors, detailed"
    ↓
[2] Upload lên S3 (không xóa background)
    ↓
Return URL
```

## Ví dụ sử dụng

### 1. Tạo Icon (có xóa background)

**Request:**
```bash
curl -X POST "http://localhost:8082/api/images/generate" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "prompt": "shopping cart icon",
    "isIcon": true
  }'
```

**Prompt được gửi đến Imagen:**
```
shopping cart icon, simple icon design, clean lines, minimalist, 
flat design, vector style, centered composition, white background, 
high quality, professional icon
```

**Kết quả:**
- Ảnh được gen với style icon đơn giản
- Background được xóa hoàn toàn (transparent PNG)
- Upload lên S3

### 2. Tạo Ảnh Thường (không xóa background)

**Request:**
```bash
curl -X POST "http://localhost:8082/api/images/generate" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "prompt": "a beautiful sunset over mountains",
    "isIcon": false
  }'
```

**Prompt được gửi đến Imagen:**
```
a beautiful sunset over mountains, PNG style, high quality, detailed, 
professional, realistic, vibrant colors
```

**Kết quả:**
- Ảnh được gen với chi tiết cao, màu sắc sống động
- Giữ nguyên background
- Upload lên S3

### 3. Sử dụng trong JavaScript

```javascript
async function generateImage(prompt, isIcon = false) {
  const response = await fetch('http://localhost:8082/api/images/generate', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer YOUR_TOKEN'
    },
    body: JSON.stringify({
      prompt: prompt,
      isIcon: isIcon
    })
  });
  
  const data = await response.json();
  return data.imageUrls[0]; // URL của ảnh đã gen
}

// Tạo icon
const iconUrl = await generateImage('home icon', true);

// Tạo ảnh
const imageUrl = await generateImage('beautiful landscape', false);
```

### 4. Sử dụng trong Java

```java
@Autowired
private IImageGenerationService imageGenerationService;

// Tạo icon
ImageGenerationRequest iconRequest = new ImageGenerationRequest();
iconRequest.setPrompt("user profile icon");
iconRequest.setIsIcon(true);
ImageGenerationResponse iconResponse = imageGenerationService.generateAndUploadImage(iconRequest);

// Tạo ảnh
ImageGenerationRequest imageRequest = new ImageGenerationRequest();
imageRequest.setPrompt("modern office workspace");
imageRequest.setIsIcon(false);
ImageGenerationResponse imageResponse = imageGenerationService.generateAndUploadImage(imageRequest);
```

## So sánh Icon vs Image

| Tiêu chí | Icon Mode | Image Mode |
|----------|-----------|------------|
| **Prompt Style** | Simple, minimalist, flat design | Realistic, detailed, vibrant |
| **Background** | Transparent (xóa bằng AI) | Giữ nguyên |
| **Xử lý** | Gen → Xóa BG → Upload | Gen → Upload |
| **Thời gian** | Lâu hơn (~2-5s) | Nhanh hơn (~1-3s) |
| **Use Case** | Icons, logos, UI elements | Photos, illustrations, backgrounds |
| **Output** | PNG transparent | PNG/JPEG with background |

## Cấu hình

### Application Properties

```yaml
# Vertex AI Configuration
gemini:
  project-id: your-project-id
  location: us-central1
  model: imagen-3.0-generate-002
  num-images: 1

# S3 Configuration
s3:
  bucket-name: your-bucket
  region: ap-southeast-1

# AI Background Remover Service
ai:
  service:
    url: http://34.126.98.83:8000
```

## Dependencies

Đảm bảo các dependencies sau đã được thêm:

```xml
<!-- Vertex AI -->
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-aiplatform</artifactId>
    <version>3.53.0</version>
</dependency>

<!-- Feign Client -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>

<!-- Feign Form for multipart -->
<dependency>
    <groupId>io.github.openfeign.form</groupId>
    <artifactId>feign-form-spring</artifactId>
    <version>3.8.0</version>
</dependency>

<!-- AWS S3 -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
</dependency>
```

## Error Handling

### Common Errors

1. **IMAGE_GENERATION_FAILED**: Lỗi khi gen ảnh từ Vertex AI
   - Kiểm tra credentials Google Cloud
   - Kiểm tra quota Vertex AI
   - Kiểm tra prompt có hợp lệ không

2. **Background Removal Failed**: Lỗi khi xóa background
   - Kiểm tra AI service có đang chạy không
   - Nếu lỗi, ảnh gốc sẽ được giữ lại (fallback)

3. **S3 Upload Failed**: Lỗi khi upload lên S3
   - Kiểm tra AWS credentials
   - Kiểm tra bucket permissions

## Performance Tips

1. **Icon Generation**: 
   - Thời gian: ~3-7 giây (gen + xóa BG)
   - Nên cache kết quả nếu prompt giống nhau

2. **Image Generation**:
   - Thời gian: ~2-4 giây (chỉ gen)
   - Nhanh hơn icon mode

3. **Batch Processing**:
   - Có thể tăng `num-images` trong config để gen nhiều ảnh cùng lúc
   - Mỗi ảnh sẽ được xử lý tuần tự

## Swagger Documentation

Truy cập Swagger UI để test API:
```
http://localhost:8082/swagger-ui.html
```

Tìm section **Image Generation** để xem chi tiết và test trực tiếp.

## Best Practices

### Prompt Tips cho Icon
```
✅ Good: "shopping cart icon"
✅ Good: "user profile icon, simple"
✅ Good: "settings gear icon, minimalist"

❌ Bad: "a very detailed shopping cart with items inside on a wooden table"
❌ Bad: "realistic user photo"
```

### Prompt Tips cho Image
```
✅ Good: "a beautiful sunset over mountains, realistic, vibrant colors"
✅ Good: "modern office workspace, professional photography"
✅ Good: "abstract geometric pattern, colorful"

❌ Bad: "icon"
❌ Bad: "simple logo"
```

## Troubleshooting

### Icon không có background trong suốt
- Kiểm tra AI Background Remover service có chạy không
- Kiểm tra logs để xem có lỗi khi xóa background không
- Nếu service lỗi, ảnh gốc sẽ được trả về (có background trắng)

### Ảnh gen ra không đúng ý
- Cải thiện prompt: thêm chi tiết, style, màu sắc
- Thử thay đổi `isIcon` parameter
- Kiểm tra logs để xem prompt cuối cùng được gửi đến Imagen

### Thời gian gen quá lâu
- Icon mode sẽ lâu hơn vì phải xóa background
- Kiểm tra network latency đến AI service
- Xem xét cache kết quả cho các prompt phổ biến

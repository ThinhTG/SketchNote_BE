# Image Safety Test API - Quick Guide

## API Endpoint

**POST** `/api/blogs/test/image-safety`

Test API để kiểm tra độ an toàn của hình ảnh bằng cách upload file trực tiếp.

## Request Format

### Headers
```
Content-Type: multipart/form-data
```

### Form Data
- **file**: Image file (JPEG, PNG, GIF, BMP, WEBP, ICO)
- Max size: 10MB

## Response Format

### Success Response (200 OK)
```json
{
  "code": 200,
  "message": "Image safety check completed successfully",
  "result": {
    "imageUrl": "test-image.jpg",
    "isSafe": true,
    "safeSearchDetails": {
      "adult": "UNLIKELY",
      "violence": "VERY_UNLIKELY",
      "racy": "UNLIKELY",
      "medical": "UNLIKELY",
      "spoof": "UNLIKELY"
    },
    "summary": "Image is safe - no violations detected"
  }
}
```

### Unsafe Image Response
```json
{
  "code": 200,
  "message": "Image safety check completed successfully",
  "result": {
    "imageUrl": "unsafe-image.jpg",
    "isSafe": false,
    "safeSearchDetails": {
      "adult": "LIKELY",
      "violence": "UNLIKELY",
      "racy": "POSSIBLE",
      "medical": "UNLIKELY",
      "spoof": "UNLIKELY"
    },
    "summary": "Potential violations detected: Adult content (LIKELY), Racy content (POSSIBLE)"
  }
}
```

## Likelihood Levels

- `UNKNOWN`: Không xác định được
- `VERY_UNLIKELY`: Rất không có khả năng
- `UNLIKELY`: Không có khả năng
- `POSSIBLE`: Có thể ⚠️
- `LIKELY`: Có khả năng ⚠️
- `VERY_LIKELY`: Rất có khả năng ⚠️

## Safety Categories

1. **adult**: Nội dung người lớn (Adult content)
2. **violence**: Bạo lực (Violence)
3. **racy**: Nội dung nhạy cảm (Racy content)
4. **medical**: Nội dung y tế (Medical content)
5. **spoof**: Ảnh giả mạo/chỉnh sửa (Spoofed/manipulated)

## isSafe Logic

Ảnh được coi là **SAFE** khi TẤT CẢ các category sau đều không phải POSSIBLE/LIKELY/VERY_LIKELY:
- adult
- violence
- racy
- medical

Nếu bất kỳ category nào có mức độ >= POSSIBLE → `isSafe = false`

## Testing với cURL

```bash
curl -X POST http://localhost:8080/api/blogs/test/image-safety \
  -F "file=@/path/to/your/image.jpg"
```

## Testing với Postman

1. **Method**: POST
2. **URL**: `http://localhost:8080/api/blogs/test/image-safety`
3. **Body**: 
   - Chọn `form-data`
   - Key: `file` (chọn type là `File`)
   - Value: Chọn file ảnh từ máy tính

![Postman Example](https://i.imgur.com/example.png)

## Testing với Swagger UI

1. Truy cập: `http://localhost:8080/swagger-ui.html`
2. Tìm section **"Blog Controller"**
3. Mở endpoint **POST /api/blogs/test/image-safety**
4. Click **"Try it out"**
5. Click **"Choose File"** và chọn ảnh
6. Click **"Execute"**

## Supported Image Formats

- JPEG (.jpg, .jpeg)
- PNG (.png)
- GIF (.gif)
- BMP (.bmp)
- WEBP (.webp)
- ICO (.ico)

## File Size Limits

- **Maximum**: 10MB
- **Recommended**: < 5MB để tốc độ xử lý nhanh hơn

## Error Responses

### No File Provided (400 Bad Request)
```json
{
  "code": 1001,
  "message": "Invalid request"
}
```

### Vision API Error (500 Internal Server Error)
```json
{
  "code": 1013,
  "message": "AI moderation failed"
}
```

## Implementation Details

### Service Method
```java
// ContentModerationService.java
public ImageSafetyCheckResponse testImageSafety(MultipartFile file)
```

### Controller Endpoint
```java
// BlogController.java
@PostMapping(value = "/test/image-safety", consumes = "multipart/form-data")
public ResponseEntity<ApiResponse<ImageSafetyCheckResponse>> testImageSafety(
        @RequestParam("file") MultipartFile file)
```

## Notes

1. **File Upload**: Ảnh được upload trực tiếp, không cần URL
2. **Processing**: File được convert thành byte array và gửi đến Vision API
3. **No Storage**: File không được lưu trữ, chỉ xử lý tạm thời
4. **Response Time**: Thường mất 1-3 giây tùy kích thước file

## Troubleshooting

### Error: "GOOGLE_APPLICATION_CREDENTIALS not set"
```bash
# Windows
set GOOGLE_APPLICATION_CREDENTIALS=C:\path\to\service-account-key.json

# Linux/Mac
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account-key.json
```

### Error: "Vision API not enabled"
1. Truy cập Google Cloud Console
2. Enable Cloud Vision API
3. Đợi vài phút để API được kích hoạt

### Error: "File too large"
- Giảm kích thước file xuống < 10MB
- Hoặc compress ảnh trước khi upload

## Example Test Images

Bạn có thể test với các loại ảnh sau:
- ✅ Ảnh phong cảnh
- ✅ Ảnh động vật
- ✅ Ảnh đồ vật
- ⚠️ Ảnh có nội dung nhạy cảm (để test detection)

## Quick Test Script

```bash
# Test với ảnh an toàn
curl -X POST http://localhost:8080/api/blogs/test/image-safety \
  -F "file=@safe-image.jpg"

# Kết quả mong đợi: isSafe = true
```

---

**API này chỉ dùng để TEST**. Logic thực tế kiểm duyệt blog sẽ tự động chạy qua scheduled task.

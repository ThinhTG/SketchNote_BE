# Image Safety Check API - Testing Guide

## API Endpoint

**POST** `/api/image-safety/check`

Test API để kiểm tra độ an toàn của hình ảnh sử dụng Google Vision SafeSearch.

## Request Format

### Headers
```
Content-Type: application/json
```

### Body
```json
{
  "imageUrl": "https://example.com/image.jpg"
}
```

**Lưu ý**: Image URL phải là publicly accessible URL (không cần authentication)

## Response Format

### Success Response (200 OK)
```json
{
  "code": 200,
  "message": "Image safety check completed successfully",
  "result": {
    "imageUrl": "https://example.com/image.jpg",
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
    "imageUrl": "https://example.com/unsafe-image.jpg",
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

Google Vision API trả về các mức độ sau:
- `UNKNOWN`: Không xác định được
- `VERY_UNLIKELY`: Rất không có khả năng
- `UNLIKELY`: Không có khả năng
- `POSSIBLE`: Có thể
- `LIKELY`: Có khả năng
- `VERY_LIKELY`: Rất có khả năng

## Safety Categories

1. **adult**: Nội dung người lớn (Adult content)
2. **violence**: Bạo lực (Violence)
3. **racy**: Nội dung nhạy cảm (Racy content)
4. **medical**: Nội dung y tế (Medical content)
5. **spoof**: Ảnh giả mạo/chỉnh sửa (Spoofed/manipulated)

## isSafe Logic

Ảnh được coi là **SAFE** khi:
- `adult` không phải POSSIBLE/LIKELY/VERY_LIKELY
- `violence` không phải POSSIBLE/LIKELY/VERY_LIKELY
- `racy` không phải POSSIBLE/LIKELY/VERY_LIKELY
- `medical` không phải POSSIBLE/LIKELY/VERY_LIKELY

Nếu bất kỳ category nào có mức độ >= POSSIBLE → `isSafe = false`

## Testing với cURL

### Test với ảnh an toàn
```bash
curl -X POST http://localhost:8080/api/image-safety/check \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrl": "https://storage.googleapis.com/cloud-samples-data/vision/label/wakeupcat.jpg"
  }'
```

### Test với Postman

1. **Method**: POST
2. **URL**: `http://localhost:8080/api/image-safety/check`
3. **Headers**: 
   - `Content-Type: application/json`
4. **Body** (raw JSON):
```json
{
  "imageUrl": "YOUR_IMAGE_URL_HERE"
}
```

## Sample Image URLs for Testing

### Safe Images (Google Cloud Samples)
```
https://storage.googleapis.com/cloud-samples-data/vision/label/wakeupcat.jpg
https://storage.googleapis.com/cloud-samples-data/vision/face/faces.jpeg
https://storage.googleapis.com/cloud-samples-data/vision/landmark/eiffel_tower.jpg
```

### Test với ảnh của bạn
- Upload ảnh lên S3, Google Cloud Storage, hoặc bất kỳ public storage nào
- Lấy public URL
- Sử dụng URL đó trong request

## Error Responses

### Invalid URL (400 Bad Request)
```json
{
  "code": 400,
  "message": "Image URL is required"
}
```

### Vision API Error (500 Internal Server Error)
```json
{
  "code": 1013,
  "message": "AI moderation failed"
}
```

## Swagger UI

Sau khi start application, truy cập:
```
http://localhost:8080/swagger-ui.html
```

Tìm section **"Image Safety"** để test trực tiếp trên Swagger UI.

## Notes

1. **Image URL Requirements**:
   - Phải là publicly accessible
   - Hỗ trợ các format: JPEG, PNG, GIF, BMP, WEBP, ICO
   - Kích thước tối đa: 10MB

2. **Rate Limits**:
   - Google Vision API có rate limit
   - Free tier: 1000 requests/month
   - Kiểm tra quota tại: https://console.cloud.google.com/apis/api/vision.googleapis.com/quotas

3. **Authentication**:
   - API sử dụng `GOOGLE_APPLICATION_CREDENTIALS` environment variable
   - Đảm bảo service account có quyền truy cập Vision API

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

### Error: "Permission denied"
- Kiểm tra service account có role `Cloud Vision API User`
- Hoặc role `Editor`/`Owner` cho project

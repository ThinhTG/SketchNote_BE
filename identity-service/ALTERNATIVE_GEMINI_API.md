# ALTERNATIVE SOLUTION: Use Gemini API Instead of Vertex AI

## Vấn Đề Hiện Tại
- Vertex AI API yêu cầu nhiều cấu hình phức tạp
- Cần enable API, cấp quyền service account
- Có thể bị giới hạn quota theo region

## Giải Pháp Thay Thế - Dùng Gemini API

### Ưu Điểm
✅ Đơn giản hơn - chỉ cần API key
✅ Không cần service account JSON file
✅ Không cần enable Vertex AI API
✅ Free tier generous hơn

### Cách Làm

#### 1. Lấy Gemini API Key
1. Truy cập: https://aistudio.google.com/app/apikey
2. Click **"Create API Key"**
3. Chọn project hoặc tạo mới
4. Copy API key

#### 2. Thêm Dependency (nếu chưa có)
```xml
<dependency>
    <groupId>com.google.ai.client.generativeai</groupId>
    <artifactId>generativeai</artifactId>
    <version>0.1.0</version>
</dependency>
```

#### 3. Cập Nhật Code
Thay vì dùng `VertexAI`, dùng `GenerativeModel` với API key:

```java
// Old way (Vertex AI)
VertexAI vertexAI = new VertexAI(projectId, location);
GenerativeModel model = new GenerativeModel(modelName, vertexAI);

// New way (Gemini API)
GenerativeModel model = new GenerativeModel("gemini-1.5-flash", apiKey);
```

#### 4. Thiết Lập Environment Variable
```
GEMINI_API_KEY=your-api-key-here
```

---

## Quyết Định

**Option A**: Tiếp tục với Vertex AI
- Enable Vertex AI API
- Cấp quyền cho service account
- Restart app với region mới (asia-southeast1)

**Option B**: Chuyển sang Gemini API
- Đơn giản hơn
- Cần refactor code một chút
- Tôi có thể giúp bạn làm điều này

Bạn muốn làm theo cách nào?

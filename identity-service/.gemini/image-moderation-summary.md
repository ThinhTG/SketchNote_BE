# Image Moderation Feature - Implementation Summary

## Tổng quan
Đã tích hợp Google Cloud Vision API SafeSearch vào hệ thống kiểm duyệt nội dung blog để tự động phát hiện hình ảnh vi phạm (adult content, violence, racy content, medical content).

## Các thay đổi chính

### 1. Cấu hình Bean (`VertexAIConfig.java`)
- **Đã thêm**: Bean `ImageAnnotatorClient` với `destroyMethod = "close"`
- **Mục đích**: Khởi tạo client để gọi Google Vision API
- **Vị trí**: `f:\Capstone\SketchNote_BE\identity-service\src\main\java\com\sketchnotes\identityservice\config\VertexAIConfig.java`

```java
@Bean(destroyMethod = "close")
public ImageAnnotatorClient imageAnnotatorClient() throws IOException {
    try {
        return ImageAnnotatorClient.create();
    } catch (IOException e) {
        throw e;
    }
}
```

### 2. Content Moderation Service (`ContentModerationService.java`)

#### a. Thêm Dependencies
```java
import com.google.cloud.vision.v1.*;
import com.google.cloud.vision.v1.Feature.Type;
```

#### b. Inject ImageAnnotatorClient
```java
private final ImageAnnotatorClient imageAnnotatorClient;
```

#### c. Cập nhật `buildContentForModeration(Blog blog)`
**Chức năng mới**:
1. Kiểm tra ảnh bìa (`blog.getImageUrl()`)
   - Chỉ kiểm tra nếu URL không null và không rỗng
2. Kiểm tra ảnh trong từng section (`content.getContentUrl()`)
   - Chỉ kiểm tra nếu URL không null và không rỗng
3. Thêm kết quả phân tích hình ảnh vào prompt gửi cho Gemini AI

**Output format**:
```
=== IMAGE SAFETY ANALYSIS REPORT ===
Cover Image: SAFE
hoặc
Cover Image: WARNING DETECTED [Adult: LIKELY, Violence: UNLIKELY, Racy: POSSIBLE, Medical: UNLIKELY]

TEXT CONTENTS & INLINE IMAGES:
- Section 1: Title: ... | Text: ... | 
  [Image Analysis]: Section 1 Image: SAFE
```

#### d. Thêm method `analyzeSingleImage(String imageUrl, String imageLabel)`
**Chức năng**:
- Gọi Google Vision API SafeSearch Detection
- Phân tích 4 loại vi phạm: Adult, Violence, Racy, Medical
- Trả về kết quả dạng text để Gemini AI đọc

**Logic**:
- Nếu bất kỳ loại vi phạm nào có mức độ `POSSIBLE`, `LIKELY`, hoặc `VERY_LIKELY` → Báo cáo WARNING
- Nếu tất cả đều `UNLIKELY` hoặc `VERY_UNLIKELY` → Báo cáo SAFE
- Nếu có lỗi → Báo cáo ERROR hoặc FAILED

#### e. Thêm method `isLikely(Likelihood likelihood)`
**Chức năng**: Helper method để kiểm tra mức độ vi phạm
```java
private boolean isLikely(Likelihood likelihood) {
    return likelihood == Likelihood.POSSIBLE || 
           likelihood == Likelihood.LIKELY || 
           likelihood == Likelihood.VERY_LIKELY;
}
```

#### f. Cập nhật Gemini AI Prompt
**Thêm hướng dẫn cho AI**:
- Cách đọc báo cáo hình ảnh từ Vision API
- Chính sách xử lý các mức độ vi phạm (SAFE, WARNING DETECTED, POSSIBLE)
- Yêu cầu kiểm tra cả text và Image Reports cho Adult content và Violence

## Luồng hoạt động

```
1. Blog được tạo → Status: PENDING_REVIEW
                    ↓
2. Scheduled task chạy mỗi 15 phút
                    ↓
3. buildContentForModeration(blog)
   ├─ Kiểm tra blog.imageUrl (nếu có)
   │  └─ analyzeSingleImage() → Vision API
   └─ Kiểm tra từng content.contentUrl (nếu có)
      └─ analyzeSingleImage() → Vision API
                    ↓
4. Tạo prompt với IMAGE SAFETY ANALYSIS REPORT
                    ↓
5. Gửi đến Gemini AI
                    ↓
6. AI phân tích cả text + image reports
                    ↓
7. Cập nhật blog status:
   - PUBLISHED (nếu safe)
   - AI_REJECTED (nếu có vi phạm)
```

## Điều kiện kiểm duyệt hình ảnh

**Hình ảnh CHỈ được kiểm duyệt khi**:
- `blog.getImageUrl() != null && !blog.getImageUrl().isEmpty()`
- `content.getContentUrl() != null && !content.getContentUrl().isEmpty()`

**Nếu không có hình ảnh**: Hệ thống vẫn hoạt động bình thường, chỉ kiểm duyệt text

## Dependencies đã có sẵn
```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-vision</artifactId>
    <version>3.79.0</version>
</dependency>
```

## Cấu hình cần thiết

### Environment Variables
```bash
# Google Cloud credentials (đã có sẵn cho Vertex AI)
GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account-key.json

# Hoặc sử dụng default credentials nếu chạy trên GCP
```

### Google Cloud APIs cần enable
1. ✅ Vertex AI API (đã có)
2. ✅ Cloud Vision API (cần enable nếu chưa có)

## Testing

### Test case 1: Blog không có hình ảnh
- Input: Blog chỉ có text, không có imageUrl và contentUrl
- Expected: Hệ thống chỉ kiểm duyệt text, không gọi Vision API

### Test case 2: Blog có ảnh bìa
- Input: Blog có imageUrl
- Expected: Vision API được gọi cho ảnh bìa, kết quả được thêm vào prompt

### Test case 3: Blog có ảnh trong content
- Input: Blog có content với contentUrl
- Expected: Vision API được gọi cho từng ảnh, kết quả được thêm vào prompt

### Test case 4: Blog có cả ảnh bìa và ảnh content
- Input: Blog có cả imageUrl và contentUrl
- Expected: Tất cả ảnh đều được kiểm tra

### Test case 5: URL ảnh không hợp lệ
- Input: imageUrl hoặc contentUrl bị lỗi
- Expected: Báo cáo "FAILED to analyze", không làm crash hệ thống

## Lưu ý quan trọng

1. **Performance**: Mỗi ảnh = 1 API call đến Vision API → có thể tốn thời gian nếu blog có nhiều ảnh
2. **Cost**: Google Vision API tính phí theo số request
3. **Error Handling**: Nếu Vision API lỗi, hệ thống vẫn tiếp tục với text moderation
4. **Null Safety**: Đã kiểm tra null/empty cho tất cả URL trước khi gọi API

## Kết luận
Feature đã được tích hợp hoàn chỉnh với:
- ✅ Null/empty check cho tất cả hình ảnh
- ✅ Error handling đầy đủ
- ✅ Integration với Gemini AI
- ✅ Không ảnh hưởng đến luồng hiện tại nếu không có hình ảnh

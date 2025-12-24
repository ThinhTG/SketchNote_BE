# Troubleshooting: Vision API Image URL Errors

## ❌ Lỗi thường gặp

### Error: "Section 1 Image: ERROR checking image"

Lỗi này xảy ra khi Google Vision API không thể truy cập URL ảnh.

## 🔍 Nguyên nhân

### 1. **URL không public** (Phổ biến nhất)
Vision API cần URL **publicly accessible** (không cần authentication).

**Kiểm tra:**
```bash
# Test URL trong browser ẩn danh
# Nếu bắt đăng nhập → URL không public
```

**Giải pháp:**
- Nếu dùng S3: Set bucket/object ACL thành `public-read`
- Nếu dùng Google Cloud Storage: Set permissions thành `allUsers` có quyền `Storage Object Viewer`
- Nếu dùng Firebase Storage: Cấu hình Storage Rules cho phép public read

### 2. **URL bị chặn bởi CORS/Firewall**
Vision API gọi từ Google Cloud servers, có thể bị chặn.

**Kiểm tra:**
```bash
curl -I "YOUR_IMAGE_URL"
# Nếu trả về 403 Forbidden → Bị chặn
```

**Giải pháp:**
- Whitelist Google Cloud IP ranges
- Tắt CORS restrictions cho image URLs
- Kiểm tra firewall rules

### 3. **URL format không hợp lệ**
Vision API chỉ hỗ trợ:
- `http://` hoặc `https://`
- Direct link đến file ảnh
- Không hỗ trợ: data URLs, blob URLs, relative paths

**Ví dụ ĐÚNG:**
```
✅ https://storage.googleapis.com/bucket/image.jpg
✅ https://example.com/images/photo.png
✅ https://s3.amazonaws.com/bucket/image.webp
```

**Ví dụ SAI:**
```
❌ data:image/png;base64,iVBORw0KG...
❌ blob:http://localhost:3000/abc-123
❌ /images/photo.jpg (relative path)
❌ file:///C:/Users/image.jpg (local file)
```

### 4. **Image format không được hỗ trợ**
Vision API hỗ trợ: JPEG, PNG, GIF, BMP, WEBP, ICO

**Không hỗ trợ:**
- SVG
- TIFF (một số trường hợp)
- RAW formats

### 5. **Image quá lớn**
- Max size: 10MB cho URL
- Max size: 20MB cho base64

### 6. **Permissions thiếu**
Service account cần quyền:
- `Cloud Vision API User`
- Hoặc `Editor`/`Owner` role

## 🛠️ Cách debug

### Bước 1: Kiểm tra logs
```bash
# Xem logs trong application
grep "Vision API error" logs/application.log

# Sẽ thấy:
# Vision API error for Section 1 Image: [ERROR_MESSAGE] - URL: https://...
```

### Bước 2: Test URL thủ công
```bash
# Test 1: Kiểm tra URL có accessible không
curl -I "YOUR_IMAGE_URL"

# Test 2: Download ảnh
curl -o test.jpg "YOUR_IMAGE_URL"

# Test 3: Kiểm tra size
curl -sI "YOUR_IMAGE_URL" | grep -i content-length
```

### Bước 3: Test với Google Cloud Console
1. Mở https://console.cloud.google.com/vision
2. Chọn "Try the API"
3. Paste URL ảnh
4. Xem kết quả

### Bước 4: Kiểm tra credentials
```bash
# Verify service account
gcloud auth list

# Test Vision API permissions
gcloud projects get-iam-policy YOUR_PROJECT_ID \
  --flatten="bindings[].members" \
  --filter="bindings.members:serviceAccount:YOUR_SERVICE_ACCOUNT"
```

## ✅ Giải pháp tạm thời

### Option 1: Skip image moderation nếu lỗi
Cập nhật prompt để AI bỏ qua ảnh lỗi:

```java
// Trong analyzeSingleImage
if (res.hasError()) {
    log.warn("Skipping image due to error: {}", imageUrl);
    return imageLabel + ": SKIPPED (unable to verify)";
}
```

### Option 2: Download và upload lại
Nếu URL không public, download ảnh và upload lên storage public:

```java
// Pseudo code
byte[] imageBytes = downloadImage(originalUrl);
String publicUrl = uploadToPublicStorage(imageBytes);
// Sau đó dùng publicUrl cho Vision API
```

### Option 3: Sử dụng base64 thay vì URL
Nếu không thể public URL, convert sang base64:

```java
// Thay vì
ImageSource imgSource = ImageSource.newBuilder()
    .setImageUri(imageUrl)
    .build();

// Dùng
byte[] imageBytes = downloadImage(imageUrl);
ByteString imgBytes = ByteString.copyFrom(imageBytes);
Image img = Image.newBuilder()
    .setContent(imgBytes)
    .build();
```

## 📋 Checklist

Khi gặp lỗi "ERROR checking image", kiểm tra:

- [ ] URL có publicly accessible không? (test trong incognito browser)
- [ ] URL có format đúng không? (https://...)
- [ ] Image format có được hỗ trợ không? (JPEG, PNG, GIF, BMP, WEBP)
- [ ] Image size có < 10MB không?
- [ ] Service account có quyền Vision API không?
- [ ] Firewall/CORS có chặn không?
- [ ] URL có expired/signed URL đã hết hạn không?

## 🔧 Fix cho từng storage provider

### AWS S3
```bash
# Set bucket public
aws s3api put-bucket-acl --bucket YOUR_BUCKET --acl public-read

# Set object public
aws s3api put-object-acl --bucket YOUR_BUCKET --key image.jpg --acl public-read
```

### Google Cloud Storage
```bash
# Set bucket public
gsutil iam ch allUsers:objectViewer gs://YOUR_BUCKET

# Set object public
gsutil acl ch -u AllUsers:R gs://YOUR_BUCKET/image.jpg
```

### Firebase Storage
```javascript
// Storage Rules
service firebase.storage {
  match /b/{bucket}/o {
    match /images/{imageId} {
      allow read: if true; // Public read
    }
  }
}
```

## 💡 Best Practices

1. **Luôn dùng HTTPS** cho image URLs
2. **Set proper CORS headers** nếu cần
3. **Monitor Vision API quota** để tránh rate limit
4. **Cache kết quả** để tránh gọi API nhiều lần cho cùng 1 ảnh
5. **Validate URL format** trước khi gọi Vision API
6. **Handle errors gracefully** - không reject toàn bộ blog vì 1 ảnh lỗi

## 📞 Khi cần help

Cung cấp thông tin sau:
1. Full error message từ logs
2. Sample image URL (nếu có thể share)
3. Storage provider đang dùng (S3, GCS, Firebase, etc.)
4. Có thể access URL trong browser không?
5. Service account permissions

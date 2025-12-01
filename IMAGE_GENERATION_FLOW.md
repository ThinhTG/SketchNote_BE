# Logic Gen Ảnh Với Background Removal

## Flow Tổng Quan

```
User Request (isIcon: true)
    ↓
[1] Vertex AI Gen 2 Ảnh JPEG
    ↓
[2] Convert JPEG → PNG (với alpha channel)
    ↓
[3] Gọi AI Background Remover (xóa background)
    ↓
[4] Upload PNG transparent lên S3
    ↓
Return 2 URLs
```

## Chi Tiết Từng Bước

### Bước 1: Vertex AI Gen Ảnh
```java
// File: ImageGenerationService.java
// Method: generateAndUploadImage()

List<byte[]> imagesBytes = generateImagesWithImagen(request);
// → Vertex AI trả về 2 ảnh JPEG (base64 decoded)
// → imagesBytes = [jpeg1_bytes, jpeg2_bytes]
```

**Output:** 2 ảnh JPEG (không có alpha channel)

---

### Bước 2: Convert JPEG → PNG
```java
// Method: removeBackgroundFromImages()

if (isIcon) {
    imagesBytes = removeBackgroundFromImages(imagesBytes);
}

// Trong removeBackgroundFromImages():
for (byte[] imageBytes : imagesBytes) {
    // 2.1: Convert JPEG → PNG với alpha channel
    byte[] pngBytes = convertToPNG(imageBytes);
    
    // convertToPNG() làm gì:
    // - Read JPEG bytes → BufferedImage
    // - Create new BufferedImage với TYPE_INT_ARGB (32-bit RGBA)
    // - Draw JPEG lên PNG image
    // - Write as PNG bytes
}
```

**Output:** 2 ảnh PNG (có alpha channel, nhưng background vẫn còn)

---

### Bước 3: Xóa Background
```java
// Tiếp tục trong removeBackgroundFromImages():

// 3.1: Tạo MultipartFile từ PNG bytes
MultipartFile multipartFile = new ByteArrayMultipartFile(
    pngBytes,  // ← PNG bytes từ bước 2
    "file",
    "temp_image.png",
    "image/png"
);

// 3.2: Gọi AI Background Remover
byte[] processedImage = aiImageService.removeBackground(multipartFile);

// aiImageService.removeBackground() làm gì:
// - Gọi Feign Client → AI service (http://34.126.98.83:8000/bg/remove)
// - AI service dùng rembg (u2net model) để xóa background
// - Trả về PNG với transparent background
```

**Output:** 2 ảnh PNG transparent (background đã xóa)

---

### Bước 4: Upload Lên S3
```java
// Method: generateAndUploadImage()

for (byte[] imageBytes : imagesBytes) {  // ← imagesBytes đã là PNG transparent
    String fileName = generateFileName(ImageType.PNG);
    String s3Url = uploadToS3(imageBytes, fileName, ImageType.PNG);
    s3Urls.add(s3Url);
}
```

**Output:** 2 URLs trên S3 với PNG transparent

---

## Code Flow Chi Tiết

```java
// ImageGenerationService.java

public ImageGenerationResponse generateAndUploadImage(ImageGenerationRequest request) {
    boolean isIcon = request.getIsIcon();
    
    // BƯỚC 1: Gen ảnh từ Vertex AI
    List<byte[]> imagesBytes = generateImagesWithImagen(request);
    // → [jpeg1, jpeg2]
    
    // BƯỚC 2 + 3: Convert PNG và xóa background
    if (isIcon) {
        imagesBytes = removeBackgroundFromImages(imagesBytes);
        // → [png_transparent1, png_transparent2]
    }
    
    // BƯỚC 4: Upload lên S3
    List<String> s3Urls = new ArrayList<>();
    for (byte[] imageBytes : imagesBytes) {
        String s3Url = uploadToS3(imageBytes, fileName, ImageType.PNG);
        s3Urls.add(s3Url);
    }
    
    return response với s3Urls;
}

private List<byte[]> removeBackgroundFromImages(List<byte[]> imagesBytes) {
    List<byte[]> processedImages = new ArrayList<>();
    
    for (byte[] imageBytes : imagesBytes) {
        // BƯỚC 2: Convert JPEG → PNG
        byte[] pngBytes = convertToPNG(imageBytes);
        
        // BƯỚC 3: Xóa background
        MultipartFile file = new ByteArrayMultipartFile(pngBytes, ...);
        byte[] transparent = aiImageService.removeBackground(file);
        
        processedImages.add(transparent);
    }
    
    return processedImages;
}

private byte[] convertToPNG(byte[] jpegBytes) {
    // Read JPEG
    BufferedImage jpeg = ImageIO.read(new ByteArrayInputStream(jpegBytes));
    
    // Create PNG with alpha channel
    BufferedImage png = new BufferedImage(
        jpeg.getWidth(), 
        jpeg.getHeight(), 
        BufferedImage.TYPE_INT_ARGB  // ← 32-bit RGBA
    );
    
    // Draw JPEG onto PNG
    Graphics2D g = png.createGraphics();
    g.drawImage(jpeg, 0, 0, null);
    g.dispose();
    
    // Write as PNG
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(png, "PNG", baos);
    
    return baos.toByteArray();
}
```

---

## Tại Sao Cần Convert JPEG → PNG?

| Format | Alpha Channel | Transparency | Rembg Support |
|--------|---------------|--------------|---------------|
| JPEG   | ❌ Không      | ❌ Không     | ⚠️ Kém        |
| PNG    | ✅ Có (ARGB)  | ✅ Có        | ✅ Tốt        |

**Rembg cần PNG với alpha channel** để:
1. Phát hiện foreground/background tốt hơn
2. Tạo transparent pixels (alpha = 0)
3. Smooth edges với partial transparency

---

## Logs Mong Đợi

```
Bắt đầu tạo icon bằng Imagen 3.0 với prompt: cat cute
Vertex AI đã tạo 2 ảnh
Đang xóa background cho 2 icon...
=== BẮT ĐẦU XÓA BACKGROUND CHO 2 ẢNH ===

Xử lý ảnh 1/2 - Size: 158242 bytes
Đã convert sang PNG - Size: 245678 bytes  ← JPEG → PNG (size tăng)
Đã tạo MultipartFile, đang gọi AI service...
AI service trả về ảnh đã xóa background - Size: 346546 bytes  ← PNG transparent
✓ Đã xóa background cho ảnh 1/2

Xử lý ảnh 2/2 - Size: 183574 bytes
Đã convert sang PNG - Size: 267890 bytes
Đã tạo MultipartFile, đang gọi AI service...
AI service trả về ảnh đã xóa background - Size: 342234 bytes
✓ Đã xóa background cho ảnh 2/2

=== HOÀN TẤT XÓA BACKGROUND: 2/2 ảnh thành công ===
Ảnh đã được upload lên S3: https://...imagen_xxx.png
Ảnh đã được upload lên S3: https://...imagen_yyy.png
Tạo và upload thành công 2 icon trong 18038ms
```

---

## Nếu Vẫn Không Xóa Được Background

### Kiểm tra:

1. **Logs có "Đã convert sang PNG"?**
   - ✅ Có → Convert OK
   - ❌ Không → Method `convertToPNG()` bị lỗi

2. **Size ảnh sau convert có tăng?**
   - ✅ Tăng → PNG OK
   - ❌ Giảm/giống → Vẫn là JPEG

3. **AI service trả về size có lớn hơn?**
   - ✅ Lớn hơn → Đã xóa background
   - ❌ Nhỏ hơn → Không xóa được

4. **Test thủ công:**
   - Download ảnh từ S3
   - Upload vào Swagger `/bg/remove`
   - Nếu Swagger xóa được → Code Spring Boot sai
   - Nếu Swagger không xóa được → Prompt/ảnh gốc sai

### Debug:

```java
// Thêm log trong convertToPNG():
log.info("Input format: {}", ImageIO.getReaderFormatNames());
log.info("Output format: PNG with alpha channel");

// Thêm log trong removeBackgroundFromImages():
log.info("PNG bytes first 8: {}", Arrays.toString(Arrays.copyOf(pngBytes, 8)));
// PNG signature: [-119, 80, 78, 71, 13, 10, 26, 10]
```

---

## Kết Luận

**Logic đúng:**
1. Vertex AI gen JPEG
2. Convert JPEG → PNG (TYPE_INT_ARGB)
3. Gọi AI service xóa background
4. Upload PNG transparent lên S3

**Nếu vẫn không xóa được:**
- Kiểm tra logs từng bước
- Test ảnh thủ công qua Swagger
- Có thể do prompt gen ảnh quá phức tạp

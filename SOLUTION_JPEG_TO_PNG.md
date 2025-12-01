# Vấn Đề: Ảnh Trên S3 Vẫn Có Background Trắng

## Phát Hiện Quan Trọng

Bạn đã test:
- ✅ Lấy ảnh từ S3 → Swagger `/bg/remove` → **XÓA ĐƯỢC**
- ❌ Gen ảnh qua API → Ảnh trên S3 → **KHÔNG XÓA ĐƯỢC**

→ **AI service hoạt động OK, nhưng code Spring Boot đang UPLOAD NHẦM ẢNH!**

## Nguyên Nhân

Vertex AI trả về **JPEG** (không có alpha channel), nhưng:
1. Code gửi cho AI service với `content-type: image/png` (SAI!)
2. AI service xóa background OK
3. NHƯNG khi upload lên S3, có thể đang upload nhầm ảnh gốc

## Giải Pháp

### Bước 1: Convert JPEG → PNG trước khi xóa background

Thêm method `convertToPNG()` vào `ImageGenerationService.java`:

```java
/**
 * Convert JPEG to PNG with alpha channel support
 */
private byte[] convertToPNG(byte[] imageBytes) throws Exception {
    try {
        // Read image
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(imageBytes);
        java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(bais);
        
        // Create new image with alpha channel (TYPE_INT_ARGB)
        java.awt.image.BufferedImage pngImage = new java.awt.image.BufferedImage(
            image.getWidth(),
            image.getHeight(),
            java.awt.image.BufferedImage.TYPE_INT_ARGB  // ← QUAN TRỌNG!
        );
        
        // Draw original image
        java.awt.Graphics2D g = pngImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        
        // Write as PNG
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(pngImage, "PNG", baos);
        
        return baos.toByteArray();
    } catch (Exception e) {
        log.error("Lỗi convert PNG: {}", e.getMessage());
        return imageBytes; // Fallback
    }
}
```

### Bước 2: Sửa `removeBackgroundFromImages()`

```java
private List<byte[]> removeBackgroundFromImages(List<byte[]> imagesBytes) {
    List<byte[]> processedImages = new ArrayList<>();
    
    for (int i = 0; i < imagesBytes.size(); i++) {
        try {
            byte[] imageBytes = imagesBytes.get(i);
            
            // THÊM: Convert JPEG → PNG trước
            byte[] pngBytes = convertToPNG(imageBytes);
            log.info("Đã convert sang PNG - Size: {} bytes", pngBytes.length);
            
            // Tạo MultipartFile từ PNG bytes
            MultipartFile multipartFile = new ByteArrayMultipartFile(
                pngBytes,  // ← Dùng pngBytes thay vì imageBytes
                "file",
                "temp_image_" + i + ".png",
                "image/png"
            );
            
            // Gọi AI service
            byte[] processedImage = aiImageService.removeBackground(multipartFile);
            processedImages.add(processedImage);
            
        } catch (Exception e) {
            log.error("Lỗi xóa background: {}", e.getMessage());
            processedImages.add(imagesBytes.get(i));
        }
    }
    
    return processedImages;
}
```

## Tại Sao Cần Convert?

1. **JPEG không hỗ trợ transparency** (alpha channel)
2. **Rembg cần PNG với alpha channel** để tạo transparent background
3. **TYPE_INT_ARGB** = PNG with 32-bit RGBA (Red, Green, Blue, Alpha)

## Test Sau Khi Sửa

1. Restart project-service
2. Gen icon với `isIcon: true`
3. Check ảnh trên S3 → Phải có background transparent!

## Nếu Vẫn Không Được

Kiểm tra logs:
- "Đã convert sang PNG" → OK
- "AI service trả về ảnh đã xóa background" → Size phải > ảnh gốc
- Nếu size nhỏ hơn → AI service không xóa được

Thử đổi prompt sang: `"gray background"` thay vì `"white background"`

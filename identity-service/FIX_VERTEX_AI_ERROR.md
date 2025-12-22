# 🔧 Hướng Dẫn Khắc Phục Lỗi Vertex AI Credentials

## ❌ Lỗi Gặp Phải
```
java.io.IOException: Error reading credential file from environment variable 
GOOGLE_APPLICATION_CREDENTIALS, value 'vertex-ai-key.json': File does not exist.
```

## ✅ Nguyên Nhân
- File `vertex-ai-key.json` đã được copy vào thư mục `identity-service` ✓
- Nhưng biến môi trường `GOOGLE_APPLICATION_CREDENTIALS` chưa được thiết lập đúng

## 🚀 Giải Pháp - Chọn 1 trong 2 cách

### **Cách 1: Chạy từ IntelliJ IDEA (Khuyến nghị)**

1. Mở **Run** → **Edit Configurations...**
2. Chọn configuration **IdentityServiceApplication**
3. Tìm mục **Environment variables** (hoặc click **Modify options** → **Environment variables**)
4. Thêm 2 biến môi trường:
   ```
   GOOGLE_APPLICATION_CREDENTIALS=F:\Capstone\SketchNote_BE\identity-service\vertex-ai-key.json
   GOOGLE_CLOUD_PROJECT_ID=<your-google-cloud-project-id>
   ```
5. Click **Apply** → **OK**
6. **Stop** và **Start** lại application

### **Cách 2: Chạy từ Terminal/PowerShell**

1. Mở file `run-local.ps1`
2. Thay `YOUR_PROJECT_ID_HERE` bằng Google Cloud Project ID thật của bạn
3. Chạy script:
   ```powershell
   cd F:\Capstone\SketchNote_BE\identity-service
   .\run-local.ps1
   ```

## 📋 Checklist

- [x] File `vertex-ai-key.json` đã được copy vào `identity-service/`
- [ ] Biến môi trường `GOOGLE_APPLICATION_CREDENTIALS` đã được thiết lập
- [ ] Biến môi trường `GOOGLE_CLOUD_PROJECT_ID` đã được thiết lập
- [ ] Application đã được restart

## 🔍 Kiểm Tra

Sau khi thiết lập xong, khi chạy application bạn sẽ thấy log:
```
Gemini configuration validated: projectId=<your-project-id>, location=us-central1, model=gemini-1.5-flash-001
```

Thay vì lỗi:
```
ERROR - Unexpected error calling Gemini AI: java.io.IOException...
```

## 💡 Lưu Ý

- **Project Service** đã chạy OK vì trong `docker-compose-services.yml` đã có cấu hình:
  ```yaml
  volumes:
    - ./vertex-ai-key.json:/app/vertex-ai-key.json
  environment:
    - GOOGLE_APPLICATION_CREDENTIALS=/app/vertex-ai-key.json
  ```

- **Identity Service** cần cấu hình tương tự khi chạy local

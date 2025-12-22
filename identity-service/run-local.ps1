# ============================================================================
# Script chạy identity-service LOCAL với Vertex AI credentials
# ============================================================================
# 
# HƯỚNG DẪN:
# 1. Mở file này và thay "YOUR_PROJECT_ID_HERE" bằng Google Cloud Project ID thật
# 2. Chạy script: .\run-local.ps1
#
# LƯU Ý: Nếu bạn chạy từ IntelliJ IDEA, không cần script này.
#        Chỉ cần thêm environment variables vào Run Configuration.
# ============================================================================

# Thiết lập biến môi trường
$env:GOOGLE_APPLICATION_CREDENTIALS = "F:\Capstone\SketchNote_BE\identity-service\vertex-ai-key.json"
$env:GOOGLE_CLOUD_PROJECT_ID = "YOUR_PROJECT_ID_HERE"  # ⚠️ THAY BẰNG PROJECT ID THẬT!

# Kiểm tra file credentials có tồn tại không
if (-Not (Test-Path $env:GOOGLE_APPLICATION_CREDENTIALS)) {
    Write-Host "❌ ERROR: Không tìm thấy file credentials!" -ForegroundColor Red
    Write-Host "   File cần: $env:GOOGLE_APPLICATION_CREDENTIALS" -ForegroundColor Yellow
    Write-Host "   Vui lòng copy file vertex-ai-key.json vào thư mục identity-service" -ForegroundColor Yellow
    exit 1
}

# Kiểm tra Project ID đã được thay chưa
if ($env:GOOGLE_CLOUD_PROJECT_ID -eq "YOUR_PROJECT_ID_HERE") {
    Write-Host "⚠️  WARNING: Bạn chưa thay GOOGLE_CLOUD_PROJECT_ID!" -ForegroundColor Yellow
    Write-Host "   Vui lòng mở file run-local.ps1 và thay YOUR_PROJECT_ID_HERE bằng project ID thật" -ForegroundColor Yellow
    Write-Host ""
    $continue = Read-Host "Bạn có muốn tiếp tục chạy không? (y/n)"
    if ($continue -ne "y") {
        exit 1
    }
}

Write-Host "✅ Environment variables đã được thiết lập:" -ForegroundColor Green
Write-Host "   GOOGLE_APPLICATION_CREDENTIALS = $env:GOOGLE_APPLICATION_CREDENTIALS" -ForegroundColor Cyan
Write-Host "   GOOGLE_CLOUD_PROJECT_ID = $env:GOOGLE_CLOUD_PROJECT_ID" -ForegroundColor Cyan
Write-Host ""

# Chạy Maven
Write-Host "🚀 Starting identity-service..." -ForegroundColor Yellow
Write-Host ""
mvn spring-boot:run

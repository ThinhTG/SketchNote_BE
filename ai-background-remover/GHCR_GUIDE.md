# 📦 Push Image lên GitHub Container Registry (GHCR)

## 🔑 Bước 1: Tạo Personal Access Token (PAT)

1. Vào GitHub → **Settings** → **Developer settings** → **Personal access tokens** → **Tokens (classic)**
2. Click **Generate new token (classic)**
3. Đặt tên: `ghcr-token`
4. Chọn scopes:
   - ✅ `write:packages` (upload packages)
   - ✅ `read:packages` (download packages)
   - ✅ `delete:packages` (xóa packages - optional)
5. Click **Generate token**
6. **Copy token** (chỉ hiện 1 lần!)

## 🔐 Bước 2: Login vào GHCR

```bash
# Lưu token vào biến
export CR_PAT=YOUR_TOKEN_HERE

# Login
echo $CR_PAT | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin
```

**Hoặc trên Windows PowerShell:**
```powershell
$env:CR_PAT="YOUR_TOKEN_HERE"
$env:CR_PAT | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin
```

## 🏗️ Bước 3: Build Image

```bash
# Build với tag ghcr.io
docker build -t ghcr.io/YOUR_GITHUB_USERNAME/ai-background-remover:latest .

# Hoặc build với version cụ thể
docker build -t ghcr.io/YOUR_GITHUB_USERNAME/ai-background-remover:v1.0.0 .

# Build cả 2 tags
docker build \
  -t ghcr.io/YOUR_GITHUB_USERNAME/ai-background-remover:latest \
  -t ghcr.io/YOUR_GITHUB_USERNAME/ai-background-remover:v1.0.0 \
  .
```

## 📤 Bước 4: Push Image

```bash
# Push latest
docker push ghcr.io/YOUR_GITHUB_USERNAME/ai-background-remover:latest

# Push version cụ thể
docker push ghcr.io/YOUR_GITHUB_USERNAME/ai-background-remover:v1.0.0

# Push tất cả tags
docker push ghcr.io/YOUR_GITHUB_USERNAME/ai-background-remover --all-tags
```

## 🌐 Bước 5: Set Image Public (Optional)

1. Vào GitHub → **Packages** → Chọn package `ai-background-remover`
2. Click **Package settings**
3. Scroll xuống **Danger Zone**
4. Click **Change visibility** → **Public**

## 📥 Bước 6: Pull Image từ GHCR

### Trên VM hoặc máy khác:

**Public image (không cần login):**
```bash
docker pull ghcr.io/YOUR_GITHUB_USERNAME/ai-background-remover:latest
```

**Private image (cần login):**
```bash
# Login trước
echo $CR_PAT | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin

# Pull image
docker pull ghcr.io/YOUR_GITHUB_USERNAME/ai-background-remover:latest
```

## 🚀 Bước 7: Run Image từ GHCR

### Cách 1: Docker Run
```bash
docker run -d \
  --name ai-bg-remover \
  -p 8000:8000 \
  ghcr.io/YOUR_GITHUB_USERNAME/ai-background-remover:latest
```

### Cách 2: Docker Compose

Tạo file `docker-compose.yml`:
```yaml
version: '3.8'

services:
  ai-background-remover:
    image: ghcr.io/YOUR_GITHUB_USERNAME/ai-background-remover:latest
    container_name: ai-background-remover
    ports:
      - "8000:8000"
    restart: unless-stopped
```

Chạy:
```bash
docker-compose up -d
```

## 🔄 Workflow Hoàn Chỉnh

### Lần đầu setup:
```bash
# 1. Login
export CR_PAT=YOUR_TOKEN
echo $CR_PAT | docker login ghcr.io -u YOUR_USERNAME --password-stdin

# 2. Build và push
docker build -t ghcr.io/YOUR_USERNAME/ai-background-remover:latest .
docker push ghcr.io/YOUR_USERNAME/ai-background-remover:latest
```

### Update version mới:
```bash
# 1. Build với version mới
docker build \
  -t ghcr.io/YOUR_USERNAME/ai-background-remover:latest \
  -t ghcr.io/YOUR_USERNAME/ai-background-remover:v1.0.1 \
  .

# 2. Push
docker push ghcr.io/YOUR_USERNAME/ai-background-remover:latest
docker push ghcr.io/YOUR_USERNAME/ai-background-remover:v1.0.1
```

### Deploy trên VM:
```bash
# 1. Pull image mới nhất
docker pull ghcr.io/YOUR_USERNAME/ai-background-remover:latest

# 2. Stop container cũ
docker stop ai-bg-remover
docker rm ai-bg-remover

# 3. Run container mới
docker run -d \
  --name ai-bg-remover \
  -p 8000:8000 \
  --restart unless-stopped \
  ghcr.io/YOUR_USERNAME/ai-background-remover:latest
```

## 📝 Script Tự Động

### Build và Push Script (`build-push.sh`)
```bash
#!/bin/bash

# Config
GITHUB_USERNAME="YOUR_USERNAME"
IMAGE_NAME="ai-background-remover"
VERSION="v1.0.0"

# Build
echo "Building image..."
docker build \
  -t ghcr.io/$GITHUB_USERNAME/$IMAGE_NAME:latest \
  -t ghcr.io/$GITHUB_USERNAME/$IMAGE_NAME:$VERSION \
  .

# Push
echo "Pushing to GHCR..."
docker push ghcr.io/$GITHUB_USERNAME/$IMAGE_NAME:latest
docker push ghcr.io/$GITHUB_USERNAME/$IMAGE_NAME:$VERSION

echo "Done! Image pushed to:"
echo "  ghcr.io/$GITHUB_USERNAME/$IMAGE_NAME:latest"
echo "  ghcr.io/$GITHUB_USERNAME/$IMAGE_NAME:$VERSION"
```

Chạy:
```bash
chmod +x build-push.sh
./build-push.sh
```

## 🤖 GitHub Actions (CI/CD Tự Động)

Tạo file `.github/workflows/docker-publish.yml`:

```yaml
name: Build and Push Docker Image

on:
  push:
    branches: [ main ]
    tags: [ 'v*' ]
  pull_request:
    branches: [ main ]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  build-and-push:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Log in to GHCR
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Extract metadata
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
          tags: |
            type=ref,event=branch
            type=ref,event=pr
            type=semver,pattern={{version}}
            type=semver,pattern={{major}}.{{minor}}

      - name: Build and push
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}
```

**Cách hoạt động:**
- Push code lên GitHub → Tự động build và push image
- Tag version (vd: `v1.0.0`) → Tự động tạo image với tag đó

## 📊 Quản Lý Images

### Xem images đã push
```bash
# List local images
docker images | grep ghcr.io

# Xem trên GitHub
# Vào: https://github.com/YOUR_USERNAME?tab=packages
```

### Xóa image cũ
```bash
# Xóa local
docker rmi ghcr.io/YOUR_USERNAME/ai-background-remover:v1.0.0

# Xóa trên GHCR: vào GitHub Packages → Delete
```

## 💡 Tips

1. **Versioning**: Luôn tag version cụ thể (`v1.0.0`) ngoài `latest`
2. **Security**: Không commit PAT vào code
3. **Size**: Optimize Dockerfile để giảm image size
4. **Cache**: Dùng GitHub Actions cache để build nhanh hơn
5. **Multi-arch**: Build cho cả AMD64 và ARM64 nếu cần

## 🔍 Troubleshooting

### Login failed
```bash
# Kiểm tra token có đúng không
echo $CR_PAT

# Thử login lại
docker logout ghcr.io
echo $CR_PAT | docker login ghcr.io -u YOUR_USERNAME --password-stdin
```

### Push denied
- Kiểm tra PAT có quyền `write:packages`
- Kiểm tra username có đúng không
- Kiểm tra image name format: `ghcr.io/username/image:tag`

### Image không public
- Vào GitHub Packages → Change visibility → Public

## 📚 Tham Khảo

- [GitHub Container Registry Docs](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)
- [Docker Build Docs](https://docs.docker.com/engine/reference/commandline/build/)

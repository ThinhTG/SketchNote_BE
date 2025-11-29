# 🚀 Quick Deploy Guide

## Deploy lên VM với Docker

### Bước 1: Chuẩn bị VM
```bash
# Cài Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Cài Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### Bước 2: Upload code lên VM
```bash
# Option 1: Git
git clone your-repo-url
cd ai-background-remover

# Option 2: SCP từ local
scp -r /path/to/ai-background-remover user@vm-ip:/home/user/
```

### Bước 3: Build và Run
```bash
# Build image
docker-compose build

# Run container
docker-compose up -d

# Xem logs
docker-compose logs -f
```

### Bước 4: Mở port
```bash
# Ubuntu
sudo ufw allow 8000/tcp
sudo ufw reload

# CentOS
sudo firewall-cmd --permanent --add-port=8000/tcp
sudo firewall-cmd --reload
```

### Bước 5: Truy cập
```
http://vm-ip:8000
http://vm-ip:8000/docs
```

## Quản lý

### Xem logs
```bash
docker-compose logs -f
```

### Restart
```bash
docker-compose restart
```

### Stop
```bash
docker-compose down
```

### Update code
```bash
git pull
docker-compose up -d --build
```

## Troubleshooting

### Container không start
```bash
# Xem logs
docker-compose logs

# Kiểm tra container
docker ps -a
```

### Port bị chiếm
```bash
# Kiểm tra port
sudo netstat -tulpn | grep 8000

# Đổi port trong docker-compose.yml
ports:
  - "8001:8000"  # Đổi 8000 thành 8001
```

### Out of memory
```bash
# Kiểm tra RAM
free -h

# Tăng swap
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

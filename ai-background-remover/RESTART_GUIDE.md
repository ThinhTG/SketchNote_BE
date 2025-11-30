# AI Background Remover Service - Restart Guide

## Changes Made:
- Upgraded to `u2net` model (better quality than default)
- Added **alpha matting** for smoother edges
- Optimized parameters for better background removal

## How to Restart Service:

### If running with Docker:
```bash
cd f:\Capstone\SketchNote_BE\ai-background-remover
docker-compose down
docker-compose up -d --build
```

### If running directly with Python:
```bash
cd f:\Capstone\SketchNote_BE\ai-background-remover

# Stop current process (Ctrl+C if running in terminal)
# Or kill the process

# Restart
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

### If deployed on VM:
```bash
# SSH to VM
ssh user@34.126.98.83

# Navigate to project
cd /path/to/ai-background-remover

# Pull latest code
git pull

# Restart service (if using systemd)
sudo systemctl restart ai-background-remover

# Or restart Docker container
docker-compose down && docker-compose up -d --build
```

## What Changed:

### Before:
```python
output_bytes = remove(input_bytes)  # Default model, no optimization
```

### After:
```python
output_bytes = remove(
    input_bytes,
    session=session,              # u2net model
    alpha_matting=True,           # Smooth edges
    alpha_matting_foreground_threshold=240,
    alpha_matting_background_threshold=10,
    alpha_matting_erode_size=10
)
```

## Expected Result:
- **Better edge quality** (no white halos)
- **More accurate background detection**
- **Transparent PNG output**

## Test After Restart:
Call the image generation API again with `isIcon: true` and check if background is properly removed.

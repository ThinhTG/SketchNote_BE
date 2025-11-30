from rembg import remove, new_session
from PIL import Image
import io

# Sử dụng model u2net cho kết quả tốt hơn
session = new_session("u2net")

async def remove_background_service(file):
    """
    Remove background from image using rembg with u2net model
    Returns PNG image with transparent background
    """
    # Đọc file input
    input_bytes = await file.read()
    
    # Xóa background với alpha matting để edge mượt hơn
    output_bytes = remove(
        input_bytes,
        session=session,
        alpha_matting=True,
        alpha_matting_foreground_threshold=240,
        alpha_matting_background_threshold=10,
        alpha_matting_erode_size=10
    )
    
    return output_bytes

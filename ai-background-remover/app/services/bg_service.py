from rembg import remove, new_session
from PIL import Image
import io

# Model u2net (mặc định) hoặc isnet-general-use là tốt nhất cho icon
session = new_session("u2net") 

async def remove_background_service(file):
    """
    Xóa background tối ưu cho Icon/Logo
    """
    try:
        # 1. Đọc bytes từ file upload
        input_bytes = await file.read()
        
        # 2. Chuyển bytes sang PIL Image để dễ xử lý
        input_image = Image.open(io.BytesIO(input_bytes))
        
        # 3. QUAN TRỌNG: Xóa background
        # Với Icon/Logo nền trắng, KHÔNG CẦN alpha_matting.
        # Hàm mặc định của rembg làm việc này cực tốt và sạch.
        output_image = remove(input_image, session=session)

        # 4. QUAN TRỌNG: Đảm bảo ảnh đầu ra có kênh Alpha (Trong suốt)
        # Bước này fix lỗi "ảnh vẫn còn nền trắng/đen"
        if output_image.mode != 'RGBA':
            output_image = output_image.convert('RGBA')

        # 5. Xuất ra bytes định dạng PNG
        output_buffer = io.BytesIO()
        output_image.save(output_buffer, format='PNG')
        
        return output_buffer.getvalue()

    except Exception as e:
        print(f"Error in remove_background_service: {str(e)}")
        # Fallback: Trả về ảnh gốc nếu lỗi (để debug)
        return input_bytes
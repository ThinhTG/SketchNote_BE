from fastapi import HTTPException

def validate_uploaded_file(file):
    if not file:
        raise HTTPException(status_code=400, detail="File không hợp lệ!")

    if not file.filename.lower().endswith((".jpg", ".jpeg", ".png")):
        raise HTTPException(status_code=400, detail="Chỉ hỗ trợ JPG, JPEG, PNG!")

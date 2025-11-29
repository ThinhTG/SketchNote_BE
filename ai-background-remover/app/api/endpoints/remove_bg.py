from fastapi import APIRouter, UploadFile, File, HTTPException
from fastapi.responses import Response
from services.bg_service import remove_background_service
from utils.file_validator import validate_uploaded_file

router = APIRouter()

@router.post("/remove")
async def remove_background(file: UploadFile = File(...)):
    validate_uploaded_file(file)

    try:
        output_bytes = await remove_background_service(file)
        return Response(content=output_bytes, media_type="image/png")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

from fastapi import APIRouter
from .endpoints import remove_bg

api_router = APIRouter()

api_router.include_router(remove_bg.router, prefix="/bg", tags=["Background Remover"])

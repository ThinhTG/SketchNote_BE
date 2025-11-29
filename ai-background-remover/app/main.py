from fastapi import FastAPI
from api.router import api_router

app = FastAPI(
    title="AI Background Remover",
    description="API xóa phông nền sử dụng rembg",
    version="1.0.0"
)

app.include_router(api_router)

@app.get("/")
def health_check():
    return {"status": "Running"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="localhost", port=8000, reload=True)

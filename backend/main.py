import os

from dotenv import load_dotenv
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from routers.auth import router as auth_router
from routers.rooms import router as rooms_router, user_router
from routers.admin import router as admin_router
from routers.maintenance import (
    router as maintenance_router,
    admin_router as admin_maintenance_router,
)
from routers.move_out import (
    router as move_out_router,
    admin_router as admin_move_out_router,
)

load_dotenv()

APP_NAME = os.getenv(
    "APP_NAME",
    "Room Rental Management System",
)

API_PREFIX = os.getenv("API_PREFIX", "/api/v1")

app = FastAPI(
    title=APP_NAME,
    version="1.0.0",
)

cors_origins = [
    origin.strip()
    for origin in os.getenv(
        "CORS_ORIGINS",
        "http://localhost:5173,http://127.0.0.1:5173,http://localhost:4173,http://127.0.0.1:4173",
    ).split(",")
    if origin.strip()
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(
    auth_router,
    prefix=API_PREFIX,
)

app.include_router(
    rooms_router,
    prefix=API_PREFIX,
)

app.include_router(
    user_router,
    prefix=API_PREFIX,
)

app.include_router(
    admin_router,
    prefix=API_PREFIX,
)

# Yêu cầu sửa chữa (người thuê + admin)
app.include_router(
    maintenance_router,
    prefix=API_PREFIX,
)

app.include_router(
    admin_maintenance_router,
    prefix=API_PREFIX,
)

# Yêu cầu trả phòng (người thuê + admin)
app.include_router(
    move_out_router,
    prefix=API_PREFIX,
)

app.include_router(
    admin_move_out_router,
    prefix=API_PREFIX,
)

@app.get("/")
def root():
    return {
        "message": "Room Rental Management API is running"
    }


@app.get("/health")
def health():
    return {
        "status": "ok"
    }
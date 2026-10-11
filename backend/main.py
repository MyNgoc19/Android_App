from fastapi import FastAPI, Header, HTTPException
from firebase_admin import auth
from pydantic import BaseModel, Field
from firebase_config import initialize_firebase
from database import (
    create_room_in_database,
    get_rooms_from_database,
    initialize_database,
)
from redis_client import get_redis
import json

initialize_firebase()
initialize_database()
app = FastAPI()
redis_client = get_redis()
ROOMS_CACHE_KEY = "cache:rooms_list"


@app.get("/")
def home():
    return {"message": "Essay API with Firebase Auth & Redis is running"}


# 1. API Xác thực & Session Storage với Redis
@app.get("/protected")
def protected(authorization: str = Header(None)):
    if authorization is None or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Unauthorized")

    token = authorization.split(" ", 1)[1]
    cache_key = f"session_token:{token}"

    # Check Redis Cache (Session Storage) để tăng Performance
    cached_session = redis_client.get(cache_key)
    if cached_session:
        return {
            "verified": True,
            "source": "redis_cache",
            "user": json.loads(cached_session)
        }

    try:
        # Nếu chưa có trong Redis, xác thực với Firebase
        decoded_token = auth.verify_id_token(token)
        user_data = {
            "uid": decoded_token.get("uid"),
            "email": decoded_token.get("email"),
            "name": decoded_token.get("name", "User"),
            "provider": decoded_token.get("firebase", {}).get("sign_in_provider")
        }

        # Lưu session vào Redis trong 1 giờ (3600 giây)
        redis_client.setex(cache_key, 3600, json.dumps(user_data))

        return {
            "verified": True,
            "source": "firebase_auth",
            "user": user_data
        }

    except Exception as e:
        raise HTTPException(status_code=401, detail=f"Invalid token: {str(e)}")


# 2. API Ví dụ ứng dụng Caching để Performance Improvement
@app.get("/rooms")
def get_rooms():
    cached_rooms = redis_client.get(ROOMS_CACHE_KEY)
    if cached_rooms:
        return {
            "source": "redis_cache",
            "rooms": json.loads(cached_rooms)
        }

    rooms_from_db = get_rooms_from_database()
    redis_client.setex(ROOMS_CACHE_KEY, 300, json.dumps(rooms_from_db))

    return {
        "source": "sqlite",
        "rooms": rooms_from_db
    }


class RoomCreate(BaseModel):
    name: str = Field(min_length=1, max_length=100)
    members: int = Field(default=0, ge=0)


@app.post("/rooms")
def create_room(room_request: RoomCreate):
    room = create_room_in_database(room_request.name.strip(), room_request.members)
    redis_client.delete(ROOMS_CACHE_KEY)
    return {"source": "sqlite", "room": room}
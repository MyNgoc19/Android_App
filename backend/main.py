from fastapi import FastAPI, Header, HTTPException
from firebase_admin import auth
from firebase_config import initialize_firebase
from redis_client import get_redis
import json

initialize_firebase()
app = FastAPI()
redis_client = get_redis()


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
    cache_key = "cache:rooms_list"

    # Bước 1: Kiểm tra Redis Cache
    cached_rooms = redis_client.get(cache_key)
    if cached_rooms:
        return {
            "source": "redis_cache",
            "rooms": json.loads(cached_rooms)
        }

    # Bước 2: Nếu chưa có, giả lập lấy dữ liệu từ Database (hoặc xử lý nặng)
    # (Trong thực tế bạn sẽ query từ PostgreSQL / MongoDB / Firebase Firestore)
    rooms_from_db = [
        {"id": 1, "name": "Phòng ôn thi Toán", "members": 12},
        {"id": 2, "name": "Phòng luyện viết Essay", "members": 8},
        {"id": 3, "name": "Phòng IELTS Speaking", "members": 15},
        {"id": 4, "name": "Phòng ôn thi Toán", "members": 12},
        {"id": 5, "name": "Phòng luyện viết Essay", "members": 8},
        {"id": 6, "name": "Phòng IELTS Speaking", "members": 15},
        {"id": 7, "name": "Phòng ôn thi Toán", "members": 12},
        {"id": 8, "name": "Phòng luyện viết Essay", "members": 8},
        {"id": 9, "name": "Phòng IELTS Speaking", "members": 15}
    ]

    # Bước 3: Lưu vào Redis Cache trong 5 phút (300 giây)
    redis_client.setex(cache_key, 300, json.dumps(rooms_from_db))

    return {
        "source": "database",
        "rooms": rooms_from_db
    }
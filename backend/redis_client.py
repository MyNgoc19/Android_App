import redis
import os

# Kết nối Redis (chạy local trên máy tính hoặc qua Docker/Redis Cloud)
REDIS_HOST = os.getenv("REDIS_HOST", "localhost")
REDIS_PORT = int(os.getenv("REDIS_PORT", 6379))

# Khởi tạo Redis client
redis_client = redis.Redis(
    host=REDIS_HOST, 
    port=REDIS_PORT, 
    decode_responses=True # Để dữ liệu trả về dạng string thay vì bytes
)

def get_redis():
    return redis_client
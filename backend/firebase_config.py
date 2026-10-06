"File này có nhiệm vụ kết nối backend với Firebase project bằng Firebase Admin SDK"

import firebase_admin
from firebase_admin import credentials

def initialize_firebase():
    # Chỉ khởi tạo Firebase một lần
    if not firebase_admin._apps:
        cred = credentials.Certificate("service.json")
        firebase_admin.initialize_app(cred)
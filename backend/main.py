from fastapi import FastAPI, Header, HTTPException
from firebase_admin import auth
from firebase_config import initialize_firebase

# Kết nối Firebase
initialize_firebase()
app = FastAPI()


@app.get("/")
def home(): return {"message": "Firebase Authentication is running"}


# Kiểm tra JWT
@app.get("/protected")
def protected(authorization: str = Header(None)):

    # Kiểm tra Authorization
    if authorization is None or not authorization.startswith("Bearer "):
        raise HTTPException(
            status_code=401,
            detail="Unauthorized"
        )

    # Lấy JWT
    token = authorization.split(" ", 1)[1]

    try:
        # Firebase xác minh JWT
        user = auth.verify_id_token(token)

        return {
            "verified": True,
            "uid": user.get("uid"),
            "email": user.get("email"),
            "name": user.get("name"),
            "provider": user.get("firebase", {}).get("sign_in_provider")
        }

    except Exception:
        raise HTTPException(
            status_code=401,
            detail="Invalid token"
        )
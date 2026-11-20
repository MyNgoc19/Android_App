
from fastapi import APIRouter, Depends, HTTPException
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from firebase_admin.exceptions import FirebaseError
from pydantic import BaseModel
from sqlalchemy import text
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from database import get_db
from firebase_config import verify_id_token
from services.auth_service import sync_user

router = APIRouter(prefix="/auth", tags=["Authentication"])

security = HTTPBearer(auto_error=False)


class UserResponse(BaseModel):
    id: int
    firebase_uid: str
    email: str
    full_name: str
    phone: str | None = None
    avatar_url: str | None = None
    gender: str | None = None
    role: str
    status: str


def get_current_user(
    credentials: HTTPAuthorizationCredentials | None = Depends(security),
    db: Session = Depends(get_db),
) -> dict:
    if credentials is None:
        raise HTTPException(
            status_code=401,
            detail="Missing authentication token.",
        )

    try:
        firebase_user = verify_id_token(credentials.credentials)
    except (FirebaseError, ValueError):
        raise HTTPException(
            status_code=401,
            detail="Invalid or expired Firebase token.",
        )

    user = db.execute(
        text("""
            SELECT
                id,
                firebase_uid,
                email,
                full_name,
                phone,
                avatar_url,
                gender,
                role,
                status
            FROM users
            WHERE firebase_uid = :firebase_uid
        """),
        {"firebase_uid": firebase_user["uid"]},
    ).mappings().one_or_none()

    if user is None:
        raise HTTPException(
            status_code=401,
            detail="Account has not been synchronized. Call POST /auth/session first.",
        )

    return dict(user)


def require_active_user(
    current_user: dict = Depends(get_current_user),
) -> dict:
    """Allow only active accounts."""
    if current_user["status"] != "ACTIVE":
        raise HTTPException(
            status_code=403,
            detail="Your account has been blocked.",
        )

    return current_user


def require_user(
    current_user: dict = Depends(require_active_user),
) -> dict:
    """Allow only USER accounts."""
    if current_user["role"] != "USER":
        raise HTTPException(
            status_code=403,
            detail="User permission required.",
        )

    return current_user


def require_admin(
    current_user: dict = Depends(require_active_user),
) -> dict:
    """Allow only ADMIN accounts."""
    if current_user["role"] != "ADMIN":
        raise HTTPException(
            status_code=403,
            detail="Administrator permission required.",
        )

    return current_user


@router.post("/session", response_model=UserResponse)
def create_session(
    credentials: HTTPAuthorizationCredentials | None = Depends(security),
    db: Session = Depends(get_db),
):
    if credentials is None:
        raise HTTPException(
            status_code=401,
            detail="Missing authentication token.",
        )

    try:
        firebase_user = verify_id_token(credentials.credentials)
    except (FirebaseError, ValueError):
        raise HTTPException(
            status_code=401,
            detail="Invalid or expired Firebase token.",
        )

    try:
        return sync_user(db, firebase_user)
    except ValueError as error:
        db.rollback()
        raise HTTPException(status_code=400, detail=str(error))
    except IntegrityError:
        db.rollback()
        raise HTTPException(
            status_code=409,
            detail="This email is already associated with another account.",
        )


@router.get("/me", response_model=UserResponse)
def get_me(
    current_user: dict = Depends(get_current_user),
):
    return current_user

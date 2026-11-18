import json
from urllib.parse import urlparse
import os
import time
import cloudinary

from fastapi import APIRouter, Depends, HTTPException, Query
from pydantic import BaseModel, Field
from sqlalchemy import text
from sqlalchemy.orm import Session
from datetime import date
from urllib.parse import unquote, urlparse
from cloudinary import uploader

from database import get_db
from routers.auth import require_admin


router = APIRouter(prefix="/admin", tags=["Admin"])

cloudinary.config(
    cloud_name=os.getenv("CLOUDINARY_CLOUD_NAME"),
    api_key=os.getenv("CLOUDINARY_API_KEY"),
    api_secret=os.getenv("CLOUDINARY_API_SECRET")
)

# REQUEST MODELS

class RoomCreate(BaseModel):
    title: str = Field(min_length=1, max_length=200)
    description: str | None = None
    address: str = Field(min_length=1)
    ward: str | None = Field(default=None, max_length=120)
    district: str | None = Field(default=None, max_length=120)
    city: str = Field(min_length=1, max_length=120)
    monthly_price: int = Field(ge=0)
    area_m2: float | None = Field(default=None, gt=0)
    capacity: int = Field(default=1, gt=0)
    amenities: list[str] = Field(default_factory=list)


class RoomUpdate(BaseModel):
    title: str | None = Field(default=None, min_length=1, max_length=200)
    description: str | None = None
    address: str | None = Field(default=None, min_length=1)
    ward: str | None = Field(default=None, max_length=120)
    district: str | None = Field(default=None, max_length=120)
    city: str | None = Field(default=None, min_length=1, max_length=120)
    monthly_price: int | None = Field(default=None, ge=0)
    area_m2: float | None = Field(default=None, gt=0)
    capacity: int | None = Field(default=None, gt=0)
    amenities: list[str] | None = None


class RoomStatusUpdate(BaseModel):
    status: str = Field(pattern="^(AVAILABLE|INACTIVE)$")


class RoomImageCreate(BaseModel):
    image_url: str = Field(min_length=1)
    sort_order: int = Field(default=0, ge=0)

class UserStatusUpdate(BaseModel):
    status: str = Field(pattern="^(ACTIVE|BLOCKED)$")

class UserRoleUpdate(BaseModel):
    role: str = Field(pattern="^(USER|ADMIN)$")

class RentalRequestStatusUpdate(BaseModel):
    status: str = Field(pattern="^(APPROVED|REJECTED)$")

class ContractCreate(BaseModel):
    request_id: int = Field(gt=0)
    start_date: date
    end_date: date | None = None
    monthly_price: int = Field(ge=0)
    deposit_amount: int = Field(default=0, ge=0)


class ContractStatusUpdate(BaseModel):
    status: str = Field(pattern="^(ENDED|CANCELLED)$")

class PaymentCreate(BaseModel):
    amount: int = Field(gt=0)
    payment_type: str = Field(pattern="^(DEPOSIT|RENT|OTHER)$")
    payment_period: str | None = Field(
        default=None,
        pattern=r"^\d{4}-(0[1-9]|1[0-2])$",
    )
    note: str | None = None


class PaymentStatusUpdate(BaseModel):
    status: str = Field(
        pattern="^(PENDING|PAID|FAILED|REFUNDED)$"
    )
    
# SHARED ROOM QUERY

ROOM_SELECT = """
    SELECT
        r.id,
        r.title,
        r.description,
        r.address,
        r.ward,
        r.district,
        r.city,
        r.monthly_price,
        r.area_m2,
        r.capacity,
        r.amenities,
        r.status,
        COALESCE(
            (
                SELECT json_agg(
                    ri.image_url
                    ORDER BY ri.sort_order, ri.id
                )
                FROM room_images ri
                WHERE ri.room_id = r.id
            ),
            '[]'::json
        ) AS images,
        COALESCE(
            (
                SELECT json_agg(
                    json_build_object(
                        'id', ri.id,
                        'image_url', ri.image_url,
                        'sort_order', ri.sort_order
                    )
                    ORDER BY ri.sort_order, ri.id
                )
                FROM room_images ri
                WHERE ri.room_id = r.id
            ),
            '[]'::json
        ) AS image_records
    FROM rooms r
"""


# GET /admin/rooms
# List all rooms

@router.get("/rooms")
def get_admin_rooms(
    page: int = Query(default=1, ge=1),
    limit: int = Query(default=20, ge=1, le=100),
    room_status: str | None = Query(default=None, alias="status"),
    keyword: str | None = Query(default=None),
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    offset = (page - 1) * limit

    filters = """
        WHERE
            (CAST(:status AS TEXT) IS NULL OR r.status = :status)
            AND (
                CAST(:keyword AS TEXT) IS NULL
                OR r.title ILIKE '%' || :keyword || '%'
                OR r.address ILIKE '%' || :keyword || '%'
            )
    """

    params = {
        "status": room_status,
        "keyword": keyword,
        "limit": limit,
        "offset": offset,
    }

    total = db.execute(
        text(f"""
            SELECT COUNT(*)
            FROM rooms r
            {filters}
        """),
        params,
    ).scalar_one()

    result = db.execute(
        text(f"""
            {ROOM_SELECT}
            {filters}
            ORDER BY r.created_at DESC, r.id DESC
            LIMIT :limit OFFSET :offset
        """),
        params,
    )

    return {
        "items": [dict(row) for row in result.mappings().all()],
        "total": total,
        "page": page,
        "limit": limit,
    }


# GET /admin/rooms/{room_id}
# Get room details

@router.get("/rooms/{room_id}")
def get_admin_room(
    room_id: int,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    result = db.execute(
        text(f"""
            {ROOM_SELECT}
            WHERE r.id = :room_id
        """),
        {"room_id": room_id},
    ).mappings().one_or_none()

    if result is None:
        raise HTTPException(
            status_code=404,
            detail="Room not found.",
        )

    return dict(result)


# POST /admin/rooms
# Create a room

@router.post("/rooms", status_code=201)
def create_room(
    room: RoomCreate,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    result = db.execute(
        text("""
            INSERT INTO rooms (
                title,
                description,
                address,
                ward,
                district,
                city,
                monthly_price,
                area_m2,
                capacity,
                amenities,
                status,
                created_by
            )
            VALUES (
                :title,
                :description,
                :address,
                :ward,
                :district,
                :city,
                :monthly_price,
                :area_m2,
                :capacity,
                CAST(:amenities AS JSONB),
                'AVAILABLE',
                :created_by
            )
            RETURNING id
        """),
        {
            "title": room.title,
            "description": room.description,
            "address": room.address,
            "ward": room.ward,
            "district": room.district,
            "city": room.city,
            "monthly_price": room.monthly_price,
            "area_m2": room.area_m2,
            "capacity": room.capacity,
            "amenities": json.dumps(room.amenities),
            "created_by": current_user["id"],
        },
    )

    room_id = result.scalar_one()
    db.commit()

    return {
        "message": "Room created successfully.",
        "room_id": room_id,
        "status": "AVAILABLE",
    }


# PATCH /admin/rooms/{room_id}
# Update room information

@router.patch("/rooms/{room_id}")
def update_room(
    room_id: int,
    room: RoomUpdate,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    data = room.model_dump(exclude_unset=True)

    if not data:
        raise HTTPException(
            status_code=400,
            detail="No fields provided for update.",
        )

    required_fields = {
        "title",
        "address",
        "city",
        "monthly_price",
        "capacity",
        "amenities",
    }

    for field in required_fields:
        if field in data and data[field] is None:
            raise HTTPException(
                status_code=422,
                detail=f"{field} cannot be null.",
            )

    existing_room = db.execute(
        text("SELECT id FROM rooms WHERE id = :room_id"),
        {"room_id": room_id},
    ).scalar_one_or_none()

    if existing_room is None:
        raise HTTPException(
            status_code=404,
            detail="Room not found.",
        )

    assignments = []

    for field in data:
        if field == "amenities":
            data[field] = json.dumps(data[field])
            assignments.append(
                "amenities = CAST(:amenities AS JSONB)"
            )
        else:
            assignments.append(f"{field} = :{field}")

    data["room_id"] = room_id

    query = f"""
        UPDATE rooms
        SET {", ".join(assignments)},
            updated_at = CURRENT_TIMESTAMP
        WHERE id = :room_id
    """

    try:
        db.execute(text(query), data)
        db.commit()
    except Exception:
        db.rollback()
        raise

    return {
        "message": "Room updated successfully.",
        "room_id": room_id,
    }


# PATCH /admin/rooms/{room_id}/status
# Change room status

@router.patch("/rooms/{room_id}/status")
def update_room_status(
    room_id: int,
    body: RoomStatusUpdate,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    try:
        room = db.execute(
            text("""
                SELECT id, status
                FROM rooms
                WHERE id = :room_id
                FOR UPDATE
            """),
            {"room_id": room_id},
        ).mappings().one_or_none()

        if room is None:
            raise HTTPException(
                status_code=404,
                detail="Room not found.",
            )

        active_contract = db.execute(
            text("""
                SELECT id
                FROM rental_contracts
                WHERE room_id = :room_id
                  AND status = 'ACTIVE'
                LIMIT 1
            """),
            {"room_id": room_id},
        ).scalar_one_or_none()

        if active_contract is not None:
            raise HTTPException(
                status_code=409,
                detail=(
                    "Cannot change status because this room "
                    "has an active rental contract."
                ),
            )

        db.execute(
            text("""
                UPDATE rooms
                SET status = :status,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = :room_id
            """),
            {
                "status": body.status,
                "room_id": room_id,
            },
        )

        db.commit()

    except Exception:
        db.rollback()
        raise

    return {
        "message": "Room status updated successfully.",
        "room_id": room_id,
        "old_status": room["status"],
        "new_status": body.status,
    }


def validate_cloudinary_url(image_url: str) -> bool:
    cloud_name = os.getenv("CLOUDINARY_CLOUD_NAME")

    if not cloud_name:
        raise HTTPException(
            status_code=500,
            detail="Cloudinary is not configured.",
        )

    parsed = urlparse(image_url)

    if parsed.scheme != "https":
        return False

    expected_prefix = f"/{cloud_name}/image/upload/"

    return (
        parsed.hostname == "res.cloudinary.com"
        and parsed.path.startswith(expected_prefix)
    )

# CLOUDINARY IMAGE UPLOAD

@router.post("/rooms/{room_id}/images/signature")
def create_cloudinary_signature(
    room_id: int,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    room_exists = db.execute(
        text("SELECT id FROM rooms WHERE id = :room_id"),
        {"room_id": room_id},
    ).scalar_one_or_none()

    if room_exists is None:
        raise HTTPException(
            status_code=404,
            detail="Room not found.",
        )

    cloud_name = os.getenv("CLOUDINARY_CLOUD_NAME")
    api_key = os.getenv("CLOUDINARY_API_KEY")
    api_secret = os.getenv("CLOUDINARY_API_SECRET")

    if not cloud_name or not api_key or not api_secret:
        raise HTTPException(
            status_code=500,
            detail="Cloudinary configuration is incomplete.",
        )

    timestamp = int(time.time())
    folder = f"rooms/{room_id}"

    params = {
        "timestamp": timestamp,
        "folder": folder,
    }

    signature = cloudinary.utils.api_sign_request(
        params,
        api_secret,
    )

    return {
        "cloud_name": cloud_name,
        "api_key": api_key,
        "timestamp": timestamp,
        "folder": folder,
        "signature": signature,
    }

# POST /admin/rooms/{room_id}/images
# Add an image URL to a room

@router.post("/rooms/{room_id}/images", status_code=201)
def add_room_image(
    room_id: int,
    body: RoomImageCreate,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    if not validate_cloudinary_url(body.image_url):
        raise HTTPException(
                status_code=422,
                detail="Image URL must belong to the configured Cloudinary cloud.",
            )

    room_exists = db.execute(
        text("SELECT id FROM rooms WHERE id = :room_id"),
        {"room_id": room_id},
    ).scalar_one_or_none()

    if room_exists is None:
        raise HTTPException(
            status_code=404,
            detail="Room not found.",
        )

    result = db.execute(
        text("""
            INSERT INTO room_images (
                room_id,
                image_url,
                sort_order
            )
            VALUES (
                :room_id,
                :image_url,
                :sort_order
            )
            RETURNING id
        """),
        {
            "room_id": room_id,
            "image_url": body.image_url,
            "sort_order": body.sort_order,
        },
    )

    image_id = result.scalar_one()
    db.commit()

    return {
        "message": "Room image added successfully.",
        "image_id": image_id,
        "room_id": room_id,
        "image_url": body.image_url,
        "sort_order": body.sort_order,
    }


# DELETE /admin/rooms/{room_id}/images/{image_id}
# Remove an image record from database

# DELETE /admin/rooms/{room_id}/images/{image_id}
# Delete an image from Cloudinary and database

@router.delete(
    "/rooms/{room_id}/images/{image_id}",
    status_code=204,
)
def delete_room_image(
    room_id: int,
    image_id: int,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    image = db.execute(
        text("""
            SELECT id, image_url
            FROM room_images
            WHERE id = :image_id
              AND room_id = :room_id
        """),
        {
            "image_id": image_id,
            "room_id": room_id,
        },
    ).mappings().one_or_none()

    if image is None:
        raise HTTPException(
            status_code=404,
            detail="Room image not found.",
        )

    cloud_name = os.getenv("CLOUDINARY_CLOUD_NAME")
    api_key = os.getenv("CLOUDINARY_API_KEY")
    api_secret = os.getenv("CLOUDINARY_API_SECRET")

    if not cloud_name or not api_key or not api_secret:
        raise HTTPException(
            status_code=500,
            detail="Cloudinary configuration is incomplete.",
        )

    parsed = urlparse(image["image_url"])

    if (
        parsed.hostname != "res.cloudinary.com"
        or parsed.scheme != "https"
    ):
        raise HTTPException(
            status_code=422,
            detail="Invalid Cloudinary image URL.",
        )

    path_parts = parsed.path.split("/image/upload/", 1)

    if len(path_parts) != 2:
        raise HTTPException(
            status_code=422,
            detail="Invalid Cloudinary image URL.",
        )

    public_id_parts = path_parts[1].split("/")

    if (
        public_id_parts
        and public_id_parts[0].startswith("v")
        and public_id_parts[0][1:].isdigit()
    ):
        public_id_parts.pop(0)

    if not public_id_parts:
        raise HTTPException(
            status_code=422,
            detail="Unable to identify Cloudinary image.",
        )

    public_id_parts[-1] = public_id_parts[-1].rsplit(".", 1)[0]
    public_id = unquote("/".join(public_id_parts))

    cloudinary.config(
        cloud_name=cloud_name,
        api_key=api_key,
        api_secret=api_secret,
        secure=True,
    )

    try:
        result = uploader.destroy(
            public_id,
            resource_type="image",
            invalidate=True,
        )       
    except Exception as exc:
        print(f"Cloudinary deletion error: {repr(exc)}")

        raise HTTPException(
            status_code=502,
            detail=f"Cloudinary deletion failed: {str(exc)}",
        ) from exc

    if result.get("result") not in ("ok", "not found"):
        raise HTTPException(
            status_code=502,
            detail="Cloudinary could not delete the image.",
        )

    try:
        db.execute(
            text("""
                DELETE FROM room_images
                WHERE id = :image_id
                  AND room_id = :room_id
            """),
            {
                "image_id": image_id,
                "room_id": room_id,
            },
        )
        db.commit()
    except Exception:
        db.rollback()
        raise

    return None


# GET /admin/users
# List and search users

@router.get("/users")
def get_admin_users(
    page: int = Query(default=1, ge=1),
    limit: int = Query(default=20, ge=1, le=100),
    keyword: str | None = Query(default=None),
    role: str | None = Query(default=None, pattern="^(USER|ADMIN)$"),
    user_status: str | None = Query(
        default=None,
        alias="status",
        pattern="^(ACTIVE|BLOCKED)$",
    ),
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    offset = (page - 1) * limit

    filters = """
        WHERE
            (
                CAST(:keyword AS TEXT) IS NULL
                OR u.email ILIKE '%' || :keyword || '%'
                OR u.full_name ILIKE '%' || :keyword || '%'
                OR u.phone ILIKE '%' || :keyword || '%'
            )
            AND (
                CAST(:role AS TEXT) IS NULL
                OR u.role = :role
            )
            AND (
                CAST(:status AS TEXT) IS NULL
                OR u.status = :status
            )
    """

    params = {
        "keyword": keyword,
        "role": role,
        "status": user_status,
        "limit": limit,
        "offset": offset,
    }

    total = db.execute(
        text(f"""
            SELECT COUNT(*)
            FROM users u
            {filters}
        """),
        params,
    ).scalar_one()

    result = db.execute(
        text(f"""
            SELECT
                u.id,
                u.email,
                u.full_name,
                u.phone,
                u.avatar_url,
                u.role,
                u.status,
                u.created_at,
                u.updated_at
            FROM users u
            {filters}
            ORDER BY u.created_at DESC, u.id DESC
            LIMIT :limit OFFSET :offset
        """),
        params,
    )

    return {
        "items": [
            dict(row)
            for row in result.mappings().all()
        ],
        "total": total,
        "page": page,
        "limit": limit,
    }


# GET /admin/users/{user_id}
# Get user details

@router.get("/users/{user_id}")
def get_admin_user(
    user_id: int,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    user = db.execute(
        text("""
            SELECT
                u.id,
                u.firebase_uid,
                u.email,
                u.full_name,
                u.phone,
                u.avatar_url,
                u.role,
                u.status,
                u.created_at,
                u.updated_at
            FROM users u
            WHERE u.id = :user_id
        """),
        {"user_id": user_id},
    ).mappings().one_or_none()

    if user is None:
        raise HTTPException(
            status_code=404,
            detail="User not found.",
        )

    # Additional user statistics.
    rental_request_count = db.execute(
        text("""
            SELECT COUNT(*)
            FROM rental_requests
            WHERE user_id = :user_id
        """),
        {"user_id": user_id},
    ).scalar_one()

    contract_count = db.execute(
        text("""
            SELECT COUNT(*)
            FROM rental_contracts
            WHERE tenant_id = :user_id
        """),
        {"user_id": user_id},
    ).scalar_one()

    return {
        **dict(user),
        "statistics": {
            "rental_requests": rental_request_count,
            "contracts": contract_count,
        },
    }


# PATCH /admin/users/{user_id}/status
# Block or unblock a user

@router.patch("/users/{user_id}/status")
def update_user_status(
    user_id: int,
    body: UserStatusUpdate,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    try:
        # Lock active administrator rows to prevent
        # concurrent operations from blocking the last admin.
        active_admins = db.execute(
            text("""
                SELECT id
                FROM users
                WHERE role = 'ADMIN'
                  AND status = 'ACTIVE'
                ORDER BY id
                FOR UPDATE
            """)
        ).scalars().all()

        user = db.execute(
            text("""
                SELECT id, role, status
                FROM users
                WHERE id = :user_id
                FOR UPDATE
            """),
            {"user_id": user_id},
        ).mappings().one_or_none()

        if user is None:
            raise HTTPException(
                status_code=404,
                detail="User not found.",
            )

        if user["status"] == body.status:
            return {
                "message": "User status is unchanged.",
                "user_id": user_id,
                "status": user["status"],
            }

        # Prevent an administrator from blocking their own account.
        if (
            user_id == current_user["id"]
            and body.status == "BLOCKED"
        ):
            raise HTTPException(
                status_code=400,
                detail="You cannot block your own account.",
            )

        # Never block the last active administrator.
        if (
            user["role"] == "ADMIN"
            and user["status"] == "ACTIVE"
            and body.status == "BLOCKED"
            and len(active_admins) <= 1
        ):
            raise HTTPException(
                status_code=409,
                detail="Cannot block the last active administrator.",
            )

        db.execute(
            text("""
                UPDATE users
                SET status = :status
                WHERE id = :user_id
            """),
            {
                "status": body.status,
                "user_id": user_id,
            },
        )

        db.commit()

        return {
            "message": "User status updated successfully.",
            "user_id": user_id,
            "old_status": user["status"],
            "new_status": body.status,
        }

    except Exception:
        db.rollback()
        raise
# PATCH /admin/users/{user_id}/role
# Change a user's role

@router.patch("/users/{user_id}/role")
def update_user_role(
    user_id: int,
    body: UserRoleUpdate,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    try:
        # Lock active administrators to prevent concurrent
        # operations from removing the last administrator.
        active_admins = db.execute(
            text("""
                SELECT id
                FROM users
                WHERE role = 'ADMIN'
                  AND status = 'ACTIVE'
                ORDER BY id
                FOR UPDATE
            """)
        ).scalars().all()

        user = db.execute(
            text("""
                SELECT id, role, status
                FROM users
                WHERE id = :user_id
                FOR UPDATE
            """),
            {"user_id": user_id},
        ).mappings().one_or_none()

        if user is None:
            raise HTTPException(
                status_code=404,
                detail="User not found.",
            )

        if user["role"] == body.role:
            return {
                "message": "User role is unchanged.",
                "user_id": user_id,
                "role": user["role"],
            }

        # Prevent administrators from removing their own privileges.
        if (
            user_id == current_user["id"]
            and body.role == "USER"
        ):
            raise HTTPException(
                status_code=400,
                detail="You cannot remove your own administrator privileges.",
            )

        # Never demote the last active administrator.
        if (
            user["role"] == "ADMIN"
            and user["status"] == "ACTIVE"
            and body.role == "USER"
            and len(active_admins) <= 1
        ):
            raise HTTPException(
                status_code=409,
                detail="Cannot demote the last active administrator.",
            )

        db.execute(
            text("""
                UPDATE users
                SET role = :role
                WHERE id = :user_id
            """),
            {
                "role": body.role,
                "user_id": user_id,
            },
        )

        db.commit()

        return {
            "message": "User role updated successfully.",
            "user_id": user_id,
            "old_role": user["role"],
            "new_role": body.role,
        }

    except Exception:
        db.rollback()
        raise

# =========================================================
# RENTAL REQUEST MANAGEMENT
# =========================================================


# GET /admin/rental-requests
# List and filter rental requests

@router.get("/rental-requests")
def get_admin_rental_requests(
    page: int = Query(default=1, ge=1),
    limit: int = Query(default=20, ge=1, le=100),
    request_status: str | None = Query(
        default=None,
        alias="status",
        pattern="^(PENDING|APPROVED|REJECTED|CANCELLED)$",
    ),
    keyword: str | None = Query(default=None),
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    offset = (page - 1) * limit

    filters = """
        WHERE
            (
                CAST(:status AS TEXT) IS NULL
                OR rr.status = :status
            )
            AND (
                CAST(:keyword AS TEXT) IS NULL
                OR u.full_name ILIKE '%' || :keyword || '%'
                OR u.email ILIKE '%' || :keyword || '%'
                OR r.title ILIKE '%' || :keyword || '%'
                OR r.address ILIKE '%' || :keyword || '%'
            )
    """

    params = {
        "status": request_status,
        "keyword": keyword,
        "limit": limit,
        "offset": offset,
    }

    total = db.execute(
        text(f"""
            SELECT COUNT(*)
            FROM rental_requests rr
            JOIN users u ON u.id = rr.user_id
            JOIN rooms r ON r.id = rr.room_id
            {filters}
        """),
        params,
    ).scalar_one()

    result = db.execute(
        text(f"""
            SELECT
                rr.id,
                rr.room_id,
                rr.user_id,
                rr.message,
                rr.desired_move_in,
                rr.status,
                rr.created_at,
                rr.updated_at,

                u.full_name AS tenant_name,
                u.email AS tenant_email,
                u.phone AS tenant_phone,

                r.title AS room_title,
                r.address AS room_address,
                r.monthly_price AS room_monthly_price

            FROM rental_requests rr
            JOIN users u ON u.id = rr.user_id
            JOIN rooms r ON r.id = rr.room_id

            {filters}

            ORDER BY rr.created_at DESC, rr.id DESC
            LIMIT :limit OFFSET :offset
        """),
        params,
    )

    return {
        "items": [
            dict(row)
            for row in result.mappings().all()
        ],
        "total": total,
        "page": page,
        "limit": limit,
    }


# GET /admin/rental-requests/{request_id}
# Get rental request details

@router.get("/rental-requests/{request_id}")
def get_admin_rental_request(
    request_id: int,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    result = db.execute(
        text("""
            SELECT
                rr.id,
                rr.room_id,
                rr.user_id,
                rr.message,
                rr.desired_move_in,
                rr.status,
                rr.created_at,
                rr.updated_at,

                u.full_name AS tenant_name,
                u.email AS tenant_email,
                u.phone AS tenant_phone,
                u.avatar_url AS tenant_avatar,

                r.title AS room_title,
                r.description AS room_description,
                r.address AS room_address,
                r.ward AS room_ward,
                r.district AS room_district,
                r.city AS room_city,
                r.monthly_price AS room_monthly_price,
                r.area_m2 AS room_area_m2,
                r.capacity AS room_capacity,
                r.status AS room_status

            FROM rental_requests rr
            JOIN users u ON u.id = rr.user_id
            JOIN rooms r ON r.id = rr.room_id

            WHERE rr.id = :request_id
        """),
        {"request_id": request_id},
    ).mappings().one_or_none()

    if result is None:
        raise HTTPException(
            status_code=404,
            detail="Rental request not found.",
        )

    return dict(result)


# PATCH /admin/rental-requests/{request_id}/status
# Approve or reject a pending request

@router.patch("/rental-requests/{request_id}/status")
def update_rental_request_status(
    request_id: int,
    body: RentalRequestStatusUpdate,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    try:
        request = db.execute(
            text("""
                SELECT
                    id,
                    room_id,
                    user_id,
                    status
                FROM rental_requests
                WHERE id = :request_id
                FOR UPDATE
            """),
            {"request_id": request_id},
        ).mappings().one_or_none()

        if request is None:
            raise HTTPException(
                status_code=404,
                detail="Rental request not found.",
            )

        if request["status"] != "PENDING":
            raise HTTPException(
                status_code=409,
                detail=(
                    "Only pending rental requests can be approved or rejected."
                ),
            )

        # Lock the associated room before checking availability.
        room = db.execute(
            text("""
                SELECT id, status
                FROM rooms
                WHERE id = :room_id
                FOR UPDATE
            """),
            {"room_id": request["room_id"]},
        ).mappings().one_or_none()

        if room is None:
            raise HTTPException(
                status_code=404,
                detail="Associated room not found.",
            )

        # An unavailable room cannot receive an approved request.
        if (
            body.status == "APPROVED"
            and room["status"] != "AVAILABLE"
        ):
            raise HTTPException(
                status_code=409,
                detail="Cannot approve a request for an unavailable room.",
            )

        db.execute(
            text("""
                UPDATE rental_requests
                SET status = :status
                WHERE id = :request_id
            """),
            {
                "status": body.status,
                "request_id": request_id,
            },
        )

        db.commit()

        return {
            "message": "Rental request status updated successfully.",
            "request_id": request_id,
            "old_status": request["status"],
            "new_status": body.status,
        }

    except Exception:
        db.rollback()
        raise

# =========================================================
# CONTRACT MANAGEMENT
# =========================================================


# GET /admin/contracts
# List and filter contracts

@router.get("/contracts")
def get_admin_contracts(
    page: int = Query(default=1, ge=1),
    limit: int = Query(default=20, ge=1, le=100),
    contract_status: str | None = Query(
        default=None,
        alias="status",
        pattern="^(ACTIVE|ENDED|CANCELLED)$",
    ),
    keyword: str | None = Query(default=None),
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    offset = (page - 1) * limit

    filters = """
        WHERE
            (
                CAST(:status AS TEXT) IS NULL
                OR c.status = :status
            )
            AND (
                CAST(:keyword AS TEXT) IS NULL
                OR u.full_name ILIKE '%' || :keyword || '%'
                OR u.email ILIKE '%' || :keyword || '%'
                OR r.title ILIKE '%' || :keyword || '%'
                OR r.address ILIKE '%' || :keyword || '%'
            )
    """

    params = {
        "status": contract_status,
        "keyword": keyword,
        "limit": limit,
        "offset": offset,
    }

    total = db.execute(
        text(f"""
            SELECT COUNT(*)
            FROM rental_contracts c
            JOIN users u ON u.id = c.tenant_id
            JOIN rooms r ON r.id = c.room_id
            {filters}
        """),
        params,
    ).scalar_one()

    result = db.execute(
        text(f"""
            SELECT
                c.id,
                c.request_id,
                c.room_id,
                c.tenant_id,
                c.start_date,
                c.end_date,
                c.monthly_price,
                c.deposit_amount,
                c.status,
                c.created_at,
                c.updated_at,

                u.full_name AS tenant_name,
                u.email AS tenant_email,
                u.phone AS tenant_phone,

                r.title AS room_title,
                r.address AS room_address

            FROM rental_contracts c
            JOIN users u ON u.id = c.tenant_id
            JOIN rooms r ON r.id = c.room_id

            {filters}

            ORDER BY c.created_at DESC, c.id DESC
            LIMIT :limit OFFSET :offset
        """),
        params,
    )

    return {
        "items": [
            dict(row)
            for row in result.mappings().all()
        ],
        "total": total,
        "page": page,
        "limit": limit,
    }


# GET /admin/contracts/{contract_id}
# Get contract details

@router.get("/contracts/{contract_id}")
def get_admin_contract(
    contract_id: int,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    contract = db.execute(
        text("""
            SELECT
                c.id,
                c.request_id,
                c.room_id,
                c.tenant_id,
                c.start_date,
                c.end_date,
                c.monthly_price,
                c.deposit_amount,
                c.status,
                c.created_at,
                c.updated_at,

                u.full_name AS tenant_name,
                u.email AS tenant_email,
                u.phone AS tenant_phone,
                u.avatar_url AS tenant_avatar,

                r.title AS room_title,
                r.description AS room_description,
                r.address AS room_address,
                r.ward AS room_ward,
                r.district AS room_district,
                r.city AS room_city,
                r.monthly_price AS current_room_price,
                r.status AS room_status

            FROM rental_contracts c
            JOIN users u ON u.id = c.tenant_id
            JOIN rooms r ON r.id = c.room_id

            WHERE c.id = :contract_id
        """),
        {"contract_id": contract_id},
    ).mappings().one_or_none()

    if contract is None:
        raise HTTPException(
            status_code=404,
            detail="Contract not found.",
        )

    payments = db.execute(
        text("""
            SELECT
                id,
                amount,
                payment_type,
                payment_period,
                status,
                paid_at,
                note,
                created_at
            FROM payments
            WHERE contract_id = :contract_id
            ORDER BY created_at DESC, id DESC
        """),
        {"contract_id": contract_id},
    ).mappings().all()

    return {
        **dict(contract),
        "payments": [
            dict(payment)
            for payment in payments
        ],
    }


# POST /admin/contracts
# Create a contract from an approved rental request

@router.post("/contracts", status_code=201)
def create_contract(
    body: ContractCreate,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    if body.end_date is not None and body.end_date < body.start_date:
        raise HTTPException(
            status_code=422,
            detail="End date cannot be earlier than start date.",
        )

    try:
        # Lock the rental request first.
        rental_request = db.execute(
            text("""
                SELECT
                    id,
                    room_id,
                    user_id,
                    status
                FROM rental_requests
                WHERE id = :request_id
                FOR UPDATE
            """),
            {"request_id": body.request_id},
        ).mappings().one_or_none()

        if rental_request is None:
            raise HTTPException(
                status_code=404,
                detail="Rental request not found.",
            )

        if rental_request["status"] != "APPROVED":
            raise HTTPException(
                status_code=409,
                detail="Only approved rental requests can create contracts.",
            )

        # A request can have only one contract.
        existing_contract = db.execute(
            text("""
                SELECT id
                FROM rental_contracts
                WHERE request_id = :request_id
            """),
            {"request_id": body.request_id},
        ).scalar_one_or_none()

        if existing_contract is not None:
            raise HTTPException(
                status_code=409,
                detail="This rental request already has a contract.",
            )

        # Lock the room to prevent concurrent contract creation.
        room = db.execute(
            text("""
                SELECT id, status
                FROM rooms
                WHERE id = :room_id
                FOR UPDATE
            """),
            {"room_id": rental_request["room_id"]},
        ).mappings().one_or_none()

        if room is None:
            raise HTTPException(
                status_code=404,
                detail="Associated room not found.",
            )

        if room["status"] != "AVAILABLE":
            raise HTTPException(
                status_code=409,
                detail="This room is no longer available.",
            )

        # Check for an existing active contract.
        active_contract = db.execute(
            text("""
                SELECT id
                FROM rental_contracts
                WHERE room_id = :room_id
                  AND status = 'ACTIVE'
                LIMIT 1
            """),
            {"room_id": room["id"]},
        ).scalar_one_or_none()

        if active_contract is not None:
            raise HTTPException(
                status_code=409,
                detail="This room already has an active contract.",
            )

        # Create the contract.
        result = db.execute(
            text("""
                INSERT INTO rental_contracts (
                    request_id,
                    room_id,
                    tenant_id,
                    start_date,
                    end_date,
                    monthly_price,
                    deposit_amount,
                    status
                )
                VALUES (
                    :request_id,
                    :room_id,
                    :tenant_id,
                    :start_date,
                    :end_date,
                    :monthly_price,
                    :deposit_amount,
                    'ACTIVE'
                )
                RETURNING id
            """),
            {
                "request_id": body.request_id,
                "room_id": room["id"],
                "tenant_id": rental_request["user_id"],
                "start_date": body.start_date,
                "end_date": body.end_date,
                "monthly_price": body.monthly_price,
                "deposit_amount": body.deposit_amount,
            },
        )

        contract_id = result.scalar_one()

        # Mark the room as rented.
        db.execute(
            text("""
                UPDATE rooms
                SET status = 'RENTED'
                WHERE id = :room_id
            """),
            {"room_id": room["id"]},
        )

        db.commit()

        return {
            "message": "Contract created successfully.",
            "contract_id": contract_id,
            "request_id": body.request_id,
            "room_id": room["id"],
            "tenant_id": rental_request["user_id"],
            "status": "ACTIVE",
        }

    except Exception:
        db.rollback()
        raise


# PATCH /admin/contracts/{contract_id}/status
# End or cancel an active contract

@router.patch("/contracts/{contract_id}/status")
def update_contract_status(
    contract_id: int,
    body: ContractStatusUpdate,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    try:
        # Lock the contract.
        contract = db.execute(
            text("""
                SELECT
                    id,
                    room_id,
                    status
                FROM rental_contracts
                WHERE id = :contract_id
                FOR UPDATE
            """),
            {"contract_id": contract_id},
        ).mappings().one_or_none()

        if contract is None:
            raise HTTPException(
                status_code=404,
                detail="Contract not found.",
            )

        if contract["status"] != "ACTIVE":
            raise HTTPException(
                status_code=409,
                detail="Only active contracts can be ended or cancelled.",
            )

        # Lock the associated room.
        room = db.execute(
            text("""
                SELECT id
                FROM rooms
                WHERE id = :room_id
                FOR UPDATE
            """),
            {"room_id": contract["room_id"]},
        ).scalar_one_or_none()

        if room is None:
            raise HTTPException(
                status_code=404,
                detail="Associated room not found.",
            )

        # Update contract status.
        db.execute(
            text("""
                UPDATE rental_contracts
                SET status = :status
                WHERE id = :contract_id
            """),
            {
                "status": body.status,
                "contract_id": contract_id,
            },
        )

        # If no other active contract exists, make the room available.
        other_active_contract = db.execute(
            text("""
                SELECT id
                FROM rental_contracts
                WHERE room_id = :room_id
                  AND status = 'ACTIVE'
                  AND id <> :contract_id
                LIMIT 1
            """),
            {
                "room_id": contract["room_id"],
                "contract_id": contract_id,
            },
        ).scalar_one_or_none()

        if other_active_contract is None:
            db.execute(
                text("""
                    UPDATE rooms
                    SET status = 'AVAILABLE'
                    WHERE id = :room_id
                """),
                {"room_id": contract["room_id"]},
            )

        db.commit()

        return {
            "message": "Contract status updated successfully.",
            "contract_id": contract_id,
            "old_status": contract["status"],
            "new_status": body.status,
        }

    except Exception:
        db.rollback()
        raise


# =========================================================
# PAYMENT MANAGEMENT
# =========================================================


# GET /admin/payments
# List and filter payments

@router.get("/payments")
def get_admin_payments(
    page: int = Query(default=1, ge=1),
    limit: int = Query(default=20, ge=1, le=100),
    payment_status: str | None = Query(
        default=None,
        alias="status",
        pattern="^(PENDING|PAID|FAILED|REFUNDED)$",
    ),
    payment_type: str | None = Query(
        default=None,
        pattern="^(DEPOSIT|RENT|OTHER)$",
    ),
    keyword: str | None = Query(default=None),
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    offset = (page - 1) * limit

    filters = """
        WHERE
            (
                CAST(:status AS TEXT) IS NULL
                OR p.status = :status
            )
            AND (
                CAST(:payment_type AS TEXT) IS NULL
                OR p.payment_type = :payment_type
            )
            AND (
                CAST(:keyword AS TEXT) IS NULL
                OR u.full_name ILIKE '%' || :keyword || '%'
                OR u.email ILIKE '%' || :keyword || '%'
                OR r.title ILIKE '%' || :keyword || '%'
                OR p.note ILIKE '%' || :keyword || '%'
            )
    """

    params = {
        "status": payment_status,
        "payment_type": payment_type,
        "keyword": keyword,
        "limit": limit,
        "offset": offset,
    }

    total = db.execute(
        text(f"""
            SELECT COUNT(*)
            FROM payments p
            JOIN rental_contracts c ON c.id = p.contract_id
            JOIN users u ON u.id = c.tenant_id
            JOIN rooms r ON r.id = c.room_id
            {filters}
        """),
        params,
    ).scalar_one()

    result = db.execute(
        text(f"""
            SELECT
                p.id,
                p.contract_id,
                p.amount,
                p.payment_type,
                p.payment_period,
                p.status,
                p.paid_at,
                p.note,
                p.created_at,

                c.tenant_id,
                c.room_id,

                u.full_name AS tenant_name,
                u.email AS tenant_email,

                r.title AS room_title

            FROM payments p
            JOIN rental_contracts c ON c.id = p.contract_id
            JOIN users u ON u.id = c.tenant_id
            JOIN rooms r ON r.id = c.room_id

            {filters}

            ORDER BY p.created_at DESC, p.id DESC
            LIMIT :limit OFFSET :offset
        """),
        params,
    )

    return {
        "items": [
            dict(row)
            for row in result.mappings().all()
        ],
        "total": total,
        "page": page,
        "limit": limit,
    }


# POST /admin/contracts/{contract_id}/payments
# Record a payment for a contract

@router.post(
    "/contracts/{contract_id}/payments",
    status_code=201,
)
def create_payment(
    contract_id: int,
    body: PaymentCreate,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    try:
        contract = db.execute(
            text("""
                SELECT id, status
                FROM rental_contracts
                WHERE id = :contract_id
                FOR UPDATE
            """),
            {"contract_id": contract_id},
        ).mappings().one_or_none()

        if contract is None:
            raise HTTPException(
                status_code=404,
                detail="Contract not found.",
            )

        if contract["status"] != "ACTIVE":
            raise HTTPException(
                status_code=409,
                detail="Cannot record a payment for an inactive contract.",
            )

        if body.payment_type != "RENT" and body.payment_period is not None:
            raise HTTPException(
                status_code=422,
                detail="payment_period is only allowed for RENT payments.",
            )

        if body.payment_type == "RENT" and body.payment_period is None:
            raise HTTPException(
                status_code=422,
                detail="payment_period is required for RENT payments.",
            )

        result = db.execute(
            text("""
                INSERT INTO payments (
                    contract_id,
                    amount,
                    payment_type,
                    payment_period,
                    status,
                    note
                )
                VALUES (
                    :contract_id,
                    :amount,
                    :payment_type,
                    :payment_period,
                    'PENDING',
                    :note
                )
                RETURNING
                    id,
                    contract_id,
                    amount,
                    payment_type,
                    payment_period,
                    status,
                    paid_at,
                    note,
                    created_at
            """),
            {
                "contract_id": contract_id,
                "amount": body.amount,
                "payment_type": body.payment_type,
                "payment_period": body.payment_period,
                "note": body.note,
            },
        )

        payment = dict(result.mappings().one())
        db.commit()

        return {
            "message": "Payment recorded successfully.",
            "payment": payment,
        }

    except Exception:
        db.rollback()
        raise


# PATCH /admin/payments/{payment_id}/status
# Update payment status

@router.patch("/payments/{payment_id}/status")
def update_payment_status(
    payment_id: int,
    body: PaymentStatusUpdate,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    try:
        payment = db.execute(
            text("""
                SELECT id, contract_id, status
                FROM payments
                WHERE id = :payment_id
                FOR UPDATE
            """),
            {"payment_id": payment_id},
        ).mappings().one_or_none()

        if payment is None:
            raise HTTPException(
                status_code=404,
                detail="Payment not found.",
            )

        old_status = payment["status"]
        new_status = body.status

        if old_status == new_status:
            return {
                "message": "Payment status is unchanged.",
                "payment_id": payment_id,
                "status": old_status,
            }

        # Validate payment status transitions.
        allowed_transitions = {
            "PENDING": {"PAID", "FAILED"},
            "PAID": {"REFUNDED"},
            "FAILED": set(),
            "REFUNDED": set(),
        }

        if new_status not in allowed_transitions[old_status]:
            raise HTTPException(
                status_code=409,
                detail=(
                    f"Cannot change payment status "
                    f"from {old_status} to {new_status}."
                ),
            )

        if new_status == "PAID":
            db.execute(
                text("""
                    UPDATE payments
                    SET status = 'PAID',
                        paid_at = CURRENT_TIMESTAMP
                    WHERE id = :payment_id
                """),
                {"payment_id": payment_id},
            )
        else:
            db.execute(
                text("""
                    UPDATE payments
                    SET status = :status
                    WHERE id = :payment_id
                """),
                {
                    "status": new_status,
                    "payment_id": payment_id,
                },
            )

        db.commit()

        return {
            "message": "Payment status updated successfully.",
            "payment_id": payment_id,
            "old_status": old_status,
            "new_status": new_status,
        }

    except Exception:
        db.rollback()
        raise

# =========================================================
# ADMIN DASHBOARD
# =========================================================

@router.get("/dashboard")
def get_admin_dashboard(
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    # Room statistics
    room_stats = db.execute(
        text("""
            SELECT
                COUNT(*) AS total,
                COUNT(*) FILTER (
                    WHERE status = 'AVAILABLE'
                ) AS available,
                COUNT(*) FILTER (
                    WHERE status = 'RENTED'
                ) AS rented,
                COUNT(*) FILTER (
                    WHERE status = 'INACTIVE'
                ) AS inactive
            FROM rooms
        """)
    ).mappings().one()

    # User statistics
    user_stats = db.execute(
        text("""
            SELECT
                COUNT(*) AS total,
                COUNT(*) FILTER (
                    WHERE role = 'USER'
                ) AS users,
                COUNT(*) FILTER (
                    WHERE role = 'ADMIN'
                ) AS admins,
                COUNT(*) FILTER (
                    WHERE status = 'ACTIVE'
                ) AS active,
                COUNT(*) FILTER (
                    WHERE status = 'BLOCKED'
                ) AS blocked
            FROM users
        """)
    ).mappings().one()

    # Rental request statistics
    request_stats = db.execute(
        text("""
            SELECT
                COUNT(*) AS total,
                COUNT(*) FILTER (
                    WHERE status = 'PENDING'
                ) AS pending,
                COUNT(*) FILTER (
                    WHERE status = 'APPROVED'
                ) AS approved,
                COUNT(*) FILTER (
                    WHERE status = 'REJECTED'
                ) AS rejected,
                COUNT(*) FILTER (
                    WHERE status = 'CANCELLED'
                ) AS cancelled
            FROM rental_requests
        """)
    ).mappings().one()

    # Contract statistics
    contract_stats = db.execute(
        text("""
            SELECT
                COUNT(*) AS total,
                COUNT(*) FILTER (
                    WHERE status = 'ACTIVE'
                ) AS active,
                COUNT(*) FILTER (
                    WHERE status = 'ENDED'
                ) AS ended,
                COUNT(*) FILTER (
                    WHERE status = 'CANCELLED'
                ) AS cancelled
            FROM rental_contracts
        """)
    ).mappings().one()

    # Payment statistics
    payment_stats = db.execute(
        text("""
            SELECT
                COUNT(*) AS total,
                COUNT(*) FILTER (
                    WHERE status = 'PENDING'
                ) AS pending,
                COUNT(*) FILTER (
                    WHERE status = 'PAID'
                ) AS paid,
                COUNT(*) FILTER (
                    WHERE status = 'FAILED'
                ) AS failed,
                COUNT(*) FILTER (
                    WHERE status = 'REFUNDED'
                ) AS refunded,

                COALESCE(
                    SUM(amount) FILTER (
                        WHERE status = 'PAID'
                    ),
                    0
                ) AS total_paid,

                COALESCE(
                    SUM(amount) FILTER (
                        WHERE status = 'PENDING'
                    ),
                    0
                ) AS total_pending

            FROM payments
        """)
    ).mappings().one()

    # Recent rental requests
    recent_requests = db.execute(
        text("""
            SELECT
                rr.id,
                rr.status,
                rr.created_at,
                u.full_name AS tenant_name,
                r.title AS room_title
            FROM rental_requests rr
            JOIN users u ON u.id = rr.user_id
            JOIN rooms r ON r.id = rr.room_id
            ORDER BY rr.created_at DESC, rr.id DESC
            LIMIT 5
        """)
    ).mappings().all()

    # Recent payments
    recent_payments = db.execute(
        text("""
            SELECT
                p.id,
                p.amount,
                p.payment_type,
                p.status,
                p.created_at,
                u.full_name AS tenant_name,
                r.title AS room_title
            FROM payments p
            JOIN rental_contracts c ON c.id = p.contract_id
            JOIN users u ON u.id = c.tenant_id
            JOIN rooms r ON r.id = c.room_id
            ORDER BY p.created_at DESC, p.id DESC
            LIMIT 5
        """)
    ).mappings().all()

    return {
        "rooms": dict(room_stats),
        "users": dict(user_stats),
        "rental_requests": dict(request_stats),
        "contracts": dict(contract_stats),
        "payments": dict(payment_stats),
        "recent_rental_requests": [
            dict(row) for row in recent_requests
        ],
        "recent_payments": [
            dict(row) for row in recent_payments
        ],
    }

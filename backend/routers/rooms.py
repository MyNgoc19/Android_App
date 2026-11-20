
from typing import Any, Literal
from datetime import date

from fastapi import APIRouter, HTTPException, Query
from pydantic import BaseModel, Field
from sqlalchemy import text
from sqlalchemy.exc import IntegrityError

from database import get_db
from fastapi import Depends
from sqlalchemy.orm import Session
from routers.auth import require_user


router = APIRouter(
    prefix="/rooms",
    tags=["Public Rooms"],
)


class RoomResponse(BaseModel):
    id: int
    title: str
    description: str | None = None
    address: str
    ward: str | None = None
    district: str | None = None
    city: str
    monthly_price: int
    area_m2: float | None = None
    capacity: int
    amenities: list[str]
    status: str
    images: list[str]


class RoomListResponse(BaseModel):
    items: list[RoomResponse]
    total: int
    page: int
    limit: int


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
        ) AS images
    FROM rooms r
"""


@router.get("", response_model=RoomListResponse)
def get_rooms(
    keyword: str | None = None,
    city: str | None = None,
    district: str | None = None,
    min_price: int | None = Query(default=None, ge=0),
    max_price: int | None = Query(default=None, ge=0),
    min_area: float | None = Query(default=None, gt=0),
    max_area: float | None = Query(default=None, gt=0),
    page: int = Query(default=1, ge=1),
    limit: int = Query(default=20, ge=1, le=100),
    db: Session = Depends(get_db),
):
    if min_price is not None and max_price is not None:
        if min_price > max_price:
            raise HTTPException(
                status_code=422,
                detail="min_price cannot be greater than max_price.",
            )

    if min_area is not None and max_area is not None:
        if min_area > max_area:
            raise HTTPException(
                status_code=422,
                detail="min_area cannot be greater than max_area.",
            )

    
    filters = """
        WHERE r.status = 'AVAILABLE'
        AND (
            CAST(:keyword AS TEXT) IS NULL
            OR r.title ILIKE '%' || CAST(:keyword AS TEXT) || '%'
            OR r.address ILIKE '%' || CAST(:keyword AS TEXT) || '%'
        )
        AND (
            CAST(:city AS TEXT) IS NULL
            OR LOWER(r.city) = LOWER(CAST(:city AS TEXT))
        )
        AND (
            CAST(:district AS TEXT) IS NULL
            OR LOWER(r.district) = LOWER(CAST(:district AS TEXT))
        )
        AND (
            CAST(:min_price AS BIGINT) IS NULL
            OR r.monthly_price >= CAST(:min_price AS BIGINT)
        )
        AND (
            CAST(:max_price AS BIGINT) IS NULL
            OR r.monthly_price <= CAST(:max_price AS BIGINT)
        )
        AND (
            CAST(:min_area AS NUMERIC) IS NULL
            OR r.area_m2 >= CAST(:min_area AS NUMERIC)
        )
        AND (
            CAST(:max_area AS NUMERIC) IS NULL
            OR r.area_m2 <= CAST(:max_area AS NUMERIC)
        )
    """


    keyword = keyword.strip() if keyword and keyword.strip() else None
    city = city.strip() if city and city.strip() else None
    district = district.strip() if district and district.strip() else None

    params: dict[str, Any] = {
        "keyword": keyword,
        "city": city,
        "district": district,
        "min_price": min_price,
        "max_price": max_price,
        "min_area": min_area,
        "max_area": max_area,
    }

    total = db.execute(
        text(f"SELECT COUNT(*) FROM rooms r {filters}"),
        params,
    ).scalar_one()

    offset = (page - 1) * limit

    result = db.execute(
        text(
            ROOM_SELECT
            + filters
            + """
            ORDER BY r.created_at DESC, r.id DESC
            LIMIT :limit OFFSET :offset
            """
        ),
        {
            **params,
            "limit": limit,
            "offset": offset,
        },
    )

    items = [
        dict(row)
        for row in result.mappings().all()
    ]

    return {
        "items": items,
        "total": total,
        "page": page,
        "limit": limit,
    }


@router.get("/{room_id}", response_model=RoomResponse)
def get_room_detail(
    room_id: int,
    db: Session = Depends(get_db),
):
    result = db.execute(
        text(
            ROOM_SELECT
            + """
            WHERE r.id = :room_id
              AND r.status = 'AVAILABLE'
            """
        ),
        {"room_id": room_id},
    )

    room = result.mappings().one_or_none()

    if room is None:
        raise HTTPException(
            status_code=404,
            detail="Room not found.",
        )

    return dict(room)


# Authenticated user APIs
user_router = APIRouter(tags=["User"])


class UserProfileUpdate(BaseModel):
    full_name: str | None = Field(default=None, min_length=1, max_length=150)
    phone: str | None = Field(default=None, max_length=30)
    avatar_url: str | None = None
    gender: Literal["MALE", "FEMALE", "OTHER"] | None = None


class RentalRequestCreate(BaseModel):
    message: str | None = Field(default=None, max_length=2000)
    desired_move_in: date | None = None


@user_router.get("/users/me")
def get_my_profile(
    current_user: dict = Depends(require_user),
    db: Session = Depends(get_db),
):
    profile = db.execute(
        text("""
            SELECT id, firebase_uid, email, full_name, phone, avatar_url, gender,
                   role, status, created_at, updated_at
            FROM users
            WHERE id = :user_id
        """),
        {"user_id": current_user["id"]},
    ).mappings().one_or_none()

    if profile is None:
        raise HTTPException(status_code=404, detail="User profile not found.")

    return dict(profile)


@user_router.patch("/users/me")
def update_my_profile(
    body: UserProfileUpdate,
    current_user: dict = Depends(require_user),
    db: Session = Depends(get_db),
):
    data = body.model_dump(exclude_unset=True)
    if not data:
        raise HTTPException(status_code=400, detail="No profile fields provided.")

    if "full_name" in data:
        if data["full_name"] is None or not data["full_name"].strip():
            raise HTTPException(status_code=422, detail="Full name cannot be empty.")
        data["full_name"] = data["full_name"].strip()

    if "phone" in data and data["phone"] is not None:
        data["phone"] = data["phone"].strip() or None

    if "avatar_url" in data and data["avatar_url"] is not None:
        avatar_url = data["avatar_url"].strip()
        if avatar_url and not avatar_url.startswith("https://"):
            raise HTTPException(status_code=422, detail="Avatar URL must use HTTPS.")
        data["avatar_url"] = avatar_url or None

    assignments = ", ".join(f"{field} = :{field}" for field in data)
    data["user_id"] = current_user["id"]

    try:
        db.execute(
            text(f"""
                UPDATE users
                SET {assignments}
                WHERE id = :user_id
            """),
            data,
        )
        db.commit()
    except Exception:
        db.rollback()
        raise

    profile = db.execute(
        text("""
            SELECT id, firebase_uid, email, full_name, phone, avatar_url,
                   role, status, created_at, updated_at
            FROM users
            WHERE id = :user_id
        """),
        {"user_id": current_user["id"]},
    ).mappings().one()

    return dict(profile)


@user_router.get("/users/me/favorites")
def get_my_favorites(
    current_user: dict = Depends(require_user),
    db: Session = Depends(get_db),
):
    rows = db.execute(
        text("""
            SELECT
                r.id, r.title, r.description, r.address, r.ward,
                r.district, r.city, r.monthly_price, r.area_m2,
                r.capacity, r.amenities, r.status, f.created_at AS favorited_at,
                COALESCE(
                    (
                        SELECT json_agg(ri.image_url ORDER BY ri.sort_order, ri.id)
                        FROM room_images ri
                        WHERE ri.room_id = r.id
                    ),
                    '[]'::json
                ) AS images
            FROM favorites f
            JOIN rooms r ON r.id = f.room_id
            WHERE f.user_id = :user_id
            ORDER BY f.created_at DESC, r.id DESC
        """),
        {"user_id": current_user["id"]},
    ).mappings().all()

    return {"items": [dict(row) for row in rows], "total": len(rows)}


@user_router.post("/users/me/favorites/{room_id}", status_code=201)
def add_my_favorite(
    room_id: int,
    current_user: dict = Depends(require_user),
    db: Session = Depends(get_db),
):
    room = db.execute(
        text("SELECT id, status FROM rooms WHERE id = :room_id"),
        {"room_id": room_id},
    ).mappings().one_or_none()

    if room is None:
        raise HTTPException(status_code=404, detail="Room not found.")

    if room["status"] != "AVAILABLE":
        raise HTTPException(
            status_code=409,
            detail="Only available rooms can be added to favorites.",
        )

    db.execute(
        text("""
            INSERT INTO favorites (user_id, room_id)
            VALUES (:user_id, :room_id)
            ON CONFLICT (user_id, room_id) DO NOTHING
        """),
        {"user_id": current_user["id"], "room_id": room_id},
    )
    db.commit()

    return {"message": "Room added to favorites.", "room_id": room_id}


@user_router.delete("/users/me/favorites/{room_id}", status_code=204)
def remove_my_favorite(
    room_id: int,
    current_user: dict = Depends(require_user),
    db: Session = Depends(get_db),
):
    db.execute(
        text("""
            DELETE FROM favorites
            WHERE user_id = :user_id AND room_id = :room_id
        """),
        {"user_id": current_user["id"], "room_id": room_id},
    )
    db.commit()
    return None


@router.post("/{room_id}/rental-requests", status_code=201)
def create_my_rental_request(
    room_id: int,
    body: RentalRequestCreate,
    current_user: dict = Depends(require_user),
    db: Session = Depends(get_db),
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
            raise HTTPException(status_code=404, detail="Room not found.")

        if room["status"] != "AVAILABLE":
            raise HTTPException(
                status_code=409,
                detail="This room is not currently available.",
            )

        result = db.execute(
            text("""
                INSERT INTO rental_requests (
                    room_id, user_id, message, desired_move_in, status
                )
                VALUES (
                    :room_id, :user_id, :message, :desired_move_in, 'PENDING'
                )
                RETURNING id, room_id, user_id, message, desired_move_in,
                          status, created_at, updated_at
            """),
            {
                "room_id": room_id,
                "user_id": current_user["id"],
                "message": body.message,
                "desired_move_in": body.desired_move_in,
            },
        )
        request = dict(result.mappings().one())
        db.commit()
        return request
    except IntegrityError as exc:
        db.rollback()
        raise HTTPException(
            status_code=409,
            detail="You already have a pending request for this room.",
        ) from exc
    except Exception:
        db.rollback()
        raise


@user_router.get("/users/me/rental-requests")
def get_my_rental_requests(
    current_user: dict = Depends(require_user),
    db: Session = Depends(get_db),
):
    rows = db.execute(
        text("""
            SELECT
                rr.id, rr.room_id, rr.user_id, rr.message,
                rr.desired_move_in, rr.status, rr.created_at, rr.updated_at,
                r.title AS room_title, r.address AS room_address,
                r.city AS room_city, r.monthly_price AS room_monthly_price,
                r.status AS room_status,
                (
                    SELECT ri.image_url
                    FROM room_images ri
                    WHERE ri.room_id = r.id
                    ORDER BY ri.sort_order, ri.id
                    LIMIT 1
                ) AS room_image
            FROM rental_requests rr
            JOIN rooms r ON r.id = rr.room_id
            WHERE rr.user_id = :user_id
            ORDER BY rr.created_at DESC, rr.id DESC
        """),
        {"user_id": current_user["id"]},
    ).mappings().all()

    return {"items": [dict(row) for row in rows], "total": len(rows)}


@user_router.get("/users/me/rental-requests/{request_id}")
def get_my_rental_request(
    request_id: int,
    current_user: dict = Depends(require_user),
    db: Session = Depends(get_db),
):
    request = db.execute(
        text("""
            SELECT
                rr.id, rr.room_id, rr.user_id, rr.message,
                rr.desired_move_in, rr.status, rr.created_at, rr.updated_at,
                r.title AS room_title, r.description AS room_description,
                r.address AS room_address, r.ward AS room_ward,
                r.district AS room_district, r.city AS room_city,
                r.monthly_price AS room_monthly_price,
                r.area_m2 AS room_area_m2, r.capacity AS room_capacity,
                r.status AS room_status
            FROM rental_requests rr
            JOIN rooms r ON r.id = rr.room_id
            WHERE rr.id = :request_id AND rr.user_id = :user_id
        """),
        {"request_id": request_id, "user_id": current_user["id"]},
    ).mappings().one_or_none()

    if request is None:
        raise HTTPException(status_code=404, detail="Rental request not found.")

    return dict(request)


@user_router.patch("/users/me/rental-requests/{request_id}/cancel")
def cancel_my_rental_request(
    request_id: int,
    current_user: dict = Depends(require_user),
    db: Session = Depends(get_db),
):
    try:
        request = db.execute(
            text("""
                SELECT id, status
                FROM rental_requests
                WHERE id = :request_id AND user_id = :user_id
                FOR UPDATE
            """),
            {"request_id": request_id, "user_id": current_user["id"]},
        ).mappings().one_or_none()

        if request is None:
            raise HTTPException(status_code=404, detail="Rental request not found.")

        if request["status"] != "PENDING":
            raise HTTPException(
                status_code=409,
                detail="Only pending rental requests can be cancelled.",
            )

        db.execute(
            text("""
                UPDATE rental_requests
                SET status = 'CANCELLED'
                WHERE id = :request_id
            """),
            {"request_id": request_id},
        )
        db.commit()

        return {
            "message": "Rental request cancelled successfully.",
            "request_id": request_id,
            "status": "CANCELLED",
        }
    except Exception:
        db.rollback()
        raise


@user_router.get("/users/me/contracts")
def get_my_contracts(
    current_user: dict = Depends(require_user),
    db: Session = Depends(get_db),
):
    rows = db.execute(
        text("""
            SELECT
                c.id, c.request_id, c.room_id, c.tenant_id,
                c.start_date, c.end_date, c.monthly_price,
                c.deposit_amount, c.status, c.created_at, c.updated_at,
                r.title AS room_title, r.address AS room_address,
                r.city AS room_city
            FROM rental_contracts c
            JOIN rooms r ON r.id = c.room_id
            WHERE c.tenant_id = :user_id
            ORDER BY c.created_at DESC, c.id DESC
        """),
        {"user_id": current_user["id"]},
    ).mappings().all()

    return {"items": [dict(row) for row in rows], "total": len(rows)}


@user_router.get("/users/me/contracts/{contract_id}")
def get_my_contract(
    contract_id: int,
    current_user: dict = Depends(require_user),
    db: Session = Depends(get_db),
):
    contract = db.execute(
        text("""
            SELECT
                c.id, c.request_id, c.room_id, c.tenant_id,
                c.start_date, c.end_date, c.monthly_price,
                c.deposit_amount, c.status, c.created_at, c.updated_at,
                r.title AS room_title, r.description AS room_description,
                r.address AS room_address, r.ward AS room_ward,
                r.district AS room_district, r.city AS room_city
            FROM rental_contracts c
            JOIN rooms r ON r.id = c.room_id
            WHERE c.id = :contract_id AND c.tenant_id = :user_id
        """),
        {"contract_id": contract_id, "user_id": current_user["id"]},
    ).mappings().one_or_none()

    if contract is None:
        raise HTTPException(status_code=404, detail="Contract not found.")

    payments = db.execute(
        text("""
            SELECT id, amount, payment_type, payment_period,
                   status, paid_at, note, created_at
            FROM payments
            WHERE contract_id = :contract_id
            ORDER BY created_at DESC, id DESC
        """),
        {"contract_id": contract_id},
    ).mappings().all()

    return {**dict(contract), "payments": [dict(row) for row in payments]}


@user_router.get("/users/me/payments")
def get_my_payments(
    current_user: dict = Depends(require_user),
    db: Session = Depends(get_db),
):
    rows = db.execute(
        text("""
            SELECT
                p.id, p.contract_id, p.amount, p.payment_type,
                p.payment_period, p.status, p.paid_at, p.note, p.created_at,
                c.room_id, c.start_date, c.end_date,
                r.title AS room_title, r.address AS room_address
            FROM payments p
            JOIN rental_contracts c ON c.id = p.contract_id
            JOIN rooms r ON r.id = c.room_id
            WHERE c.tenant_id = :user_id
            ORDER BY p.created_at DESC, p.id DESC
        """),
        {"user_id": current_user["id"]},
    ).mappings().all()

    return {"items": [dict(row) for row in rows], "total": len(rows)}

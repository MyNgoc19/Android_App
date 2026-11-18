
from typing import Any

from fastapi import APIRouter, HTTPException, Query
from pydantic import BaseModel
from sqlalchemy import text

from database import get_db
from fastapi import Depends
from sqlalchemy.orm import Session


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

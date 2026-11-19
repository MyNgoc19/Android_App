"""
Yêu cầu sửa chữa (maintenance requests)

Người thuê (cần đăng nhập USER):
    GET   /users/me/maintenance-requests                 Danh sách yêu cầu của tôi
    POST  /users/me/maintenance-requests                 Gửi yêu cầu (gắn vào hợp đồng ACTIVE)
    PATCH /users/me/maintenance-requests/{id}/cancel     Hủy yêu cầu đang chờ

Admin (cần đăng nhập ADMIN):
    GET   /admin/maintenance-requests                    Danh sách + lọc
    GET   /admin/maintenance-requests/{id}               Chi tiết
    PATCH /admin/maintenance-requests/{id}/status        Đổi trạng thái + ghi chú
"""

from fastapi import APIRouter, Depends, HTTPException, Query
from pydantic import BaseModel, Field
from sqlalchemy import text
from sqlalchemy.orm import Session

from database import get_db
from routers.auth import require_admin, require_user


router = APIRouter(tags=["User Maintenance"])
admin_router = APIRouter(prefix="/admin", tags=["Admin Maintenance"])


# =========================================================
# REQUEST MODELS
# =========================================================

class MaintenanceCreate(BaseModel):
    title: str = Field(min_length=1, max_length=200)
    description: str = Field(min_length=1, max_length=2000)
    priority: str = Field(default="MEDIUM", pattern="^(LOW|MEDIUM|HIGH)$")
    image_url: str | None = None


class MaintenanceStatusUpdate(BaseModel):
    status: str = Field(pattern="^(IN_PROGRESS|DONE|REJECTED)$")
    admin_note: str | None = Field(default=None, max_length=2000)


# =========================================================
# SHARED QUERY
# =========================================================

MAINTENANCE_SELECT = """
    SELECT
        m.id,
        m.contract_id,
        m.user_id,
        m.title,
        m.description,
        m.priority,
        m.image_url,
        m.status,
        m.admin_note,
        m.resolved_at,
        m.created_at,
        m.updated_at,

        c.room_id,
        r.title AS room_title,
        r.address AS room_address,

        u.full_name AS tenant_name,
        u.email AS tenant_email,
        u.phone AS tenant_phone

    FROM maintenance_requests m
    JOIN rental_contracts c ON c.id = m.contract_id
    JOIN rooms r ON r.id = c.room_id
    JOIN users u ON u.id = m.user_id
"""


def _get_maintenance(db: Session, request_id: int) -> dict | None:
    row = db.execute(
        text(MAINTENANCE_SELECT + " WHERE m.id = :request_id"),
        {"request_id": request_id},
    ).mappings().one_or_none()
    return dict(row) if row is not None else None


# =========================================================
# USER APIs
# =========================================================

@router.get("/users/me/maintenance-requests")
def get_my_maintenance_requests(
    current_user: dict = Depends(require_user),
    db: Session = Depends(get_db),
):
    rows = db.execute(
        text(
            MAINTENANCE_SELECT
            + """
            WHERE m.user_id = :user_id
            ORDER BY m.created_at DESC, m.id DESC
            """
        ),
        {"user_id": current_user["id"]},
    ).mappings().all()

    return {"items": [dict(row) for row in rows], "total": len(rows)}


@router.post("/users/me/maintenance-requests", status_code=201)
def create_my_maintenance_request(
    body: MaintenanceCreate,
    current_user: dict = Depends(require_user),
    db: Session = Depends(get_db),
):
    title = body.title.strip()
    description = body.description.strip()

    if not title or not description:
        raise HTTPException(
            status_code=422,
            detail="Title and description cannot be empty.",
        )

    image_url = body.image_url.strip() if body.image_url else None
    if image_url and not image_url.startswith("https://"):
        raise HTTPException(status_code=422, detail="Image URL must use HTTPS.")

    try:
        # Yêu cầu sửa chữa phải gắn với hợp đồng đang hiệu lực của chính người thuê
        contract_id = db.execute(
            text("""
                SELECT id
                FROM rental_contracts
                WHERE tenant_id = :user_id
                  AND status = 'ACTIVE'
                ORDER BY start_date DESC, id DESC
                LIMIT 1
            """),
            {"user_id": current_user["id"]},
        ).scalar_one_or_none()

        if contract_id is None:
            raise HTTPException(
                status_code=409,
                detail="You need an active rental contract to send a maintenance request.",
            )

        request_id = db.execute(
            text("""
                INSERT INTO maintenance_requests (
                    contract_id, user_id, title, description,
                    priority, image_url, status
                )
                VALUES (
                    :contract_id, :user_id, :title, :description,
                    :priority, :image_url, 'PENDING'
                )
                RETURNING id
            """),
            {
                "contract_id": contract_id,
                "user_id": current_user["id"],
                "title": title,
                "description": description,
                "priority": body.priority,
                "image_url": image_url,
            },
        ).scalar_one()

        db.commit()
    except Exception:
        db.rollback()
        raise

    return _get_maintenance(db, request_id)


@router.patch("/users/me/maintenance-requests/{request_id}/cancel")
def cancel_my_maintenance_request(
    request_id: int,
    current_user: dict = Depends(require_user),
    db: Session = Depends(get_db),
):
    try:
        request = db.execute(
            text("""
                SELECT id, status
                FROM maintenance_requests
                WHERE id = :request_id AND user_id = :user_id
                FOR UPDATE
            """),
            {"request_id": request_id, "user_id": current_user["id"]},
        ).mappings().one_or_none()

        if request is None:
            raise HTTPException(status_code=404, detail="Maintenance request not found.")

        if request["status"] != "PENDING":
            raise HTTPException(
                status_code=409,
                detail="Only pending maintenance requests can be cancelled.",
            )

        db.execute(
            text("""
                UPDATE maintenance_requests
                SET status = 'CANCELLED'
                WHERE id = :request_id
            """),
            {"request_id": request_id},
        )
        db.commit()
    except Exception:
        db.rollback()
        raise

    return _get_maintenance(db, request_id)


# =========================================================
# ADMIN APIs
# =========================================================

@admin_router.get("/maintenance-requests")
def get_admin_maintenance_requests(
    page: int = Query(default=1, ge=1),
    limit: int = Query(default=20, ge=1, le=100),
    request_status: str | None = Query(
        default=None,
        alias="status",
        pattern="^(PENDING|IN_PROGRESS|DONE|REJECTED|CANCELLED)$",
    ),
    priority: str | None = Query(default=None, pattern="^(LOW|MEDIUM|HIGH)$"),
    keyword: str | None = Query(default=None),
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    offset = (page - 1) * limit
    keyword = keyword.strip() if keyword and keyword.strip() else None

    filters = """
        WHERE
            (CAST(:status AS TEXT) IS NULL OR m.status = :status)
            AND (CAST(:priority AS TEXT) IS NULL OR m.priority = :priority)
            AND (
                CAST(:keyword AS TEXT) IS NULL
                OR m.title ILIKE '%' || :keyword || '%'
                OR u.full_name ILIKE '%' || :keyword || '%'
                OR u.email ILIKE '%' || :keyword || '%'
                OR r.title ILIKE '%' || :keyword || '%'
            )
    """

    params = {
        "status": request_status,
        "priority": priority,
        "keyword": keyword,
        "limit": limit,
        "offset": offset,
    }

    total = db.execute(
        text(f"""
            SELECT COUNT(*)
            FROM maintenance_requests m
            JOIN rental_contracts c ON c.id = m.contract_id
            JOIN rooms r ON r.id = c.room_id
            JOIN users u ON u.id = m.user_id
            {filters}
        """),
        params,
    ).scalar_one()

    rows = db.execute(
        text(
            MAINTENANCE_SELECT
            + filters
            + """
            ORDER BY
                CASE m.status WHEN 'PENDING' THEN 0 WHEN 'IN_PROGRESS' THEN 1 ELSE 2 END,
                CASE m.priority WHEN 'HIGH' THEN 0 WHEN 'MEDIUM' THEN 1 ELSE 2 END,
                m.created_at DESC, m.id DESC
            LIMIT :limit OFFSET :offset
            """
        ),
        params,
    ).mappings().all()

    return {
        "items": [dict(row) for row in rows],
        "total": total,
        "page": page,
        "limit": limit,
    }


@admin_router.get("/maintenance-requests/{request_id}")
def get_admin_maintenance_request(
    request_id: int,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    request = _get_maintenance(db, request_id)
    if request is None:
        raise HTTPException(status_code=404, detail="Maintenance request not found.")
    return request


@admin_router.patch("/maintenance-requests/{request_id}/status")
def update_maintenance_status(
    request_id: int,
    body: MaintenanceStatusUpdate,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    allowed_transitions = {
        "PENDING": {"IN_PROGRESS", "DONE", "REJECTED"},
        "IN_PROGRESS": {"DONE", "REJECTED"},
        "DONE": set(),
        "REJECTED": set(),
        "CANCELLED": set(),
    }

    try:
        request = db.execute(
            text("""
                SELECT id, status
                FROM maintenance_requests
                WHERE id = :request_id
                FOR UPDATE
            """),
            {"request_id": request_id},
        ).mappings().one_or_none()

        if request is None:
            raise HTTPException(status_code=404, detail="Maintenance request not found.")

        old_status = request["status"]

        if body.status not in allowed_transitions[old_status]:
            raise HTTPException(
                status_code=409,
                detail=f"Cannot change maintenance status from {old_status} to {body.status}.",
            )

        admin_note = body.admin_note.strip() if body.admin_note else None

        db.execute(
            text("""
                UPDATE maintenance_requests
                SET status = CAST(:status AS VARCHAR),
                    admin_note = COALESCE(CAST(:admin_note AS TEXT), admin_note),
                    resolved_at = CASE
                        WHEN CAST(:status AS VARCHAR) IN ('DONE', 'REJECTED') THEN CURRENT_TIMESTAMP
                        ELSE resolved_at
                    END
                WHERE id = :request_id
            """),
            {
                "status": body.status,
                "admin_note": admin_note,
                "request_id": request_id,
            },
        )
        db.commit()
    except Exception:
        db.rollback()
        raise

    return {
        "message": "Maintenance request status updated successfully.",
        "request_id": request_id,
        "old_status": old_status,
        "new_status": body.status,
    }
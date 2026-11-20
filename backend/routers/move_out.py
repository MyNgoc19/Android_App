"""
Yêu cầu trả phòng (move-out requests)

Người thuê (cần đăng nhập USER):
    GET   /users/me/move-out-requests                 Danh sách yêu cầu của tôi
    POST  /users/me/move-out-requests                 Gửi yêu cầu trả phòng (hợp đồng ACTIVE)
    PATCH /users/me/move-out-requests/{id}/cancel     Hủy yêu cầu đang chờ

Admin (cần đăng nhập ADMIN):
    GET   /admin/move-out-requests                    Danh sách + lọc
    GET   /admin/move-out-requests/{id}               Chi tiết
    PATCH /admin/move-out-requests/{id}/status        APPROVED / REJECTED / COMPLETED
          COMPLETED => hợp đồng ENDED (end_date = ngày trả phòng), phòng AVAILABLE lại
"""

from datetime import date

from fastapi import APIRouter, Depends, HTTPException, Query
from pydantic import BaseModel, Field
from sqlalchemy import text
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from database import get_db
from routers.auth import require_admin, require_user


router = APIRouter(tags=["User Move-out"])
admin_router = APIRouter(prefix="/admin", tags=["Admin Move-out"])






class MoveOutCreate(BaseModel):
    move_out_date: date
    reason: str | None = Field(default=None, max_length=2000)


class MoveOutStatusUpdate(BaseModel):
    status: str = Field(pattern="^(APPROVED|REJECTED|COMPLETED)$")
    admin_note: str | None = Field(default=None, max_length=2000)






MOVE_OUT_SELECT = """
    SELECT
        mo.id,
        mo.contract_id,
        mo.user_id,
        mo.move_out_date,
        mo.reason,
        mo.status,
        mo.admin_note,
        mo.created_at,
        mo.updated_at,

        c.room_id,
        c.start_date AS contract_start_date,
        c.end_date AS contract_end_date,
        c.status AS contract_status,
        c.deposit_amount,

        r.title AS room_title,
        r.address AS room_address,

        u.full_name AS tenant_name,
        u.email AS tenant_email,
        u.phone AS tenant_phone

    FROM move_out_requests mo
    JOIN rental_contracts c ON c.id = mo.contract_id
    JOIN rooms r ON r.id = c.room_id
    JOIN users u ON u.id = mo.user_id
"""


def _get_move_out(db: Session, request_id: int) -> dict | None:
    row = db.execute(
        text(MOVE_OUT_SELECT + " WHERE mo.id = :request_id"),
        {"request_id": request_id},
    ).mappings().one_or_none()
    return dict(row) if row is not None else None






@router.get("/users/me/move-out-requests")
def get_my_move_out_requests(
    current_user: dict = Depends(require_user),
    db: Session = Depends(get_db),
):
    rows = db.execute(
        text(
            MOVE_OUT_SELECT
            + """
            WHERE mo.user_id = :user_id
            ORDER BY mo.created_at DESC, mo.id DESC
            """
        ),
        {"user_id": current_user["id"]},
    ).mappings().all()

    return {"items": [dict(row) for row in rows], "total": len(rows)}


@router.post("/users/me/move-out-requests", status_code=201)
def create_my_move_out_request(
    body: MoveOutCreate,
    current_user: dict = Depends(require_user),
    db: Session = Depends(get_db),
):
    if body.move_out_date < date.today():
        raise HTTPException(
            status_code=422,
            detail="Move-out date cannot be in the past.",
        )

    try:
        contract = db.execute(
            text("""
                SELECT id, start_date
                FROM rental_contracts
                WHERE tenant_id = :user_id
                  AND status = 'ACTIVE'
                ORDER BY start_date DESC, id DESC
                LIMIT 1
                FOR UPDATE
            """),
            {"user_id": current_user["id"]},
        ).mappings().one_or_none()

        if contract is None:
            raise HTTPException(
                status_code=409,
                detail="You do not have an active rental contract.",
            )

        if body.move_out_date < contract["start_date"]:
            raise HTTPException(
                status_code=422,
                detail="Move-out date cannot be earlier than the contract start date.",
            )

        open_request = db.execute(
            text("""
                SELECT id
                FROM move_out_requests
                WHERE contract_id = :contract_id
                  AND status IN ('PENDING', 'APPROVED')
                LIMIT 1
            """),
            {"contract_id": contract["id"]},
        ).scalar_one_or_none()

        if open_request is not None:
            raise HTTPException(
                status_code=409,
                detail="You already have a move-out request for this contract.",
            )

        reason = body.reason.strip() if body.reason and body.reason.strip() else None

        request_id = db.execute(
            text("""
                INSERT INTO move_out_requests (
                    contract_id, user_id, move_out_date, reason, status
                )
                VALUES (
                    :contract_id, :user_id, :move_out_date, :reason, 'PENDING'
                )
                RETURNING id
            """),
            {
                "contract_id": contract["id"],
                "user_id": current_user["id"],
                "move_out_date": body.move_out_date,
                "reason": reason,
            },
        ).scalar_one()

        db.commit()
    except IntegrityError as exc:
        db.rollback()
        raise HTTPException(
            status_code=409,
            detail="You already have a move-out request for this contract.",
        ) from exc
    except Exception:
        db.rollback()
        raise

    return _get_move_out(db, request_id)


@router.patch("/users/me/move-out-requests/{request_id}/cancel")
def cancel_my_move_out_request(
    request_id: int,
    current_user: dict = Depends(require_user),
    db: Session = Depends(get_db),
):
    try:
        request = db.execute(
            text("""
                SELECT id, status
                FROM move_out_requests
                WHERE id = :request_id AND user_id = :user_id
                FOR UPDATE
            """),
            {"request_id": request_id, "user_id": current_user["id"]},
        ).mappings().one_or_none()

        if request is None:
            raise HTTPException(status_code=404, detail="Move-out request not found.")

        if request["status"] != "PENDING":
            raise HTTPException(
                status_code=409,
                detail="Only pending move-out requests can be cancelled.",
            )

        db.execute(
            text("""
                UPDATE move_out_requests
                SET status = 'CANCELLED'
                WHERE id = :request_id
            """),
            {"request_id": request_id},
        )
        db.commit()
    except Exception:
        db.rollback()
        raise

    return _get_move_out(db, request_id)






@admin_router.get("/move-out-requests")
def get_admin_move_out_requests(
    page: int = Query(default=1, ge=1),
    limit: int = Query(default=20, ge=1, le=100),
    request_status: str | None = Query(
        default=None,
        alias="status",
        pattern="^(PENDING|APPROVED|REJECTED|CANCELLED|COMPLETED)$",
    ),
    keyword: str | None = Query(default=None),
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    offset = (page - 1) * limit
    keyword = keyword.strip() if keyword and keyword.strip() else None

    filters = """
        WHERE
            (CAST(:status AS TEXT) IS NULL OR mo.status = :status)
            AND (
                CAST(:keyword AS TEXT) IS NULL
                OR u.full_name ILIKE '%' || :keyword || '%'
                OR u.email ILIKE '%' || :keyword || '%'
                OR r.title ILIKE '%' || :keyword || '%'
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
            FROM move_out_requests mo
            JOIN rental_contracts c ON c.id = mo.contract_id
            JOIN rooms r ON r.id = c.room_id
            JOIN users u ON u.id = mo.user_id
            {filters}
        """),
        params,
    ).scalar_one()

    rows = db.execute(
        text(
            MOVE_OUT_SELECT
            + filters
            + """
            ORDER BY mo.created_at DESC, mo.id DESC
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


@admin_router.get("/move-out-requests/{request_id}")
def get_admin_move_out_request(
    request_id: int,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    request = _get_move_out(db, request_id)
    if request is None:
        raise HTTPException(status_code=404, detail="Move-out request not found.")
    return request


@admin_router.patch("/move-out-requests/{request_id}/status")
def update_move_out_status(
    request_id: int,
    body: MoveOutStatusUpdate,
    db: Session = Depends(get_db),
    current_user: dict = Depends(require_admin),
):
    allowed_transitions = {
        "PENDING": {"APPROVED", "REJECTED"},
        "APPROVED": {"COMPLETED", "REJECTED"},
        "REJECTED": set(),
        "CANCELLED": set(),
        "COMPLETED": set(),
    }

    try:
        request = db.execute(
            text("""
                SELECT id, contract_id, move_out_date, status
                FROM move_out_requests
                WHERE id = :request_id
                FOR UPDATE
            """),
            {"request_id": request_id},
        ).mappings().one_or_none()

        if request is None:
            raise HTTPException(status_code=404, detail="Move-out request not found.")

        old_status = request["status"]

        if body.status not in allowed_transitions[old_status]:
            raise HTTPException(
                status_code=409,
                detail=f"Cannot change move-out status from {old_status} to {body.status}.",
            )


        if body.status == "COMPLETED":
            contract = db.execute(
                text("""
                    SELECT id, room_id, start_date, status
                    FROM rental_contracts
                    WHERE id = :contract_id
                    FOR UPDATE
                """),
                {"contract_id": request["contract_id"]},
            ).mappings().one_or_none()

            if contract is None or contract["status"] != "ACTIVE":
                raise HTTPException(
                    status_code=409,
                    detail="The related contract is no longer active.",
                )

            end_date = max(request["move_out_date"], contract["start_date"])

            db.execute(
                text("""
                    UPDATE rental_contracts
                    SET status = 'ENDED',
                        end_date = :end_date,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE id = :contract_id
                """),
                {"end_date": end_date, "contract_id": contract["id"]},
            )

            other_active_contract = db.execute(
                text("""
                    SELECT id
                    FROM rental_contracts
                    WHERE room_id = :room_id
                      AND status = 'ACTIVE'
                      AND id <> :contract_id
                    LIMIT 1
                """),
                {"room_id": contract["room_id"], "contract_id": contract["id"]},
            ).scalar_one_or_none()

            if other_active_contract is None:
                db.execute(
                    text("""
                        UPDATE rooms
                        SET status = 'AVAILABLE',
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = :room_id
                    """),
                    {"room_id": contract["room_id"]},
                )

        admin_note = body.admin_note.strip() if body.admin_note else None

        db.execute(
            text("""
                UPDATE move_out_requests
                SET status = :status,
                    admin_note = COALESCE(:admin_note, admin_note)
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
        "message": "Move-out request status updated successfully.",
        "request_id": request_id,
        "old_status": old_status,
        "new_status": body.status,
    }

# Shared API Contract — ROOM_RENTAL_MANAGEMENT_SYSTEM

Version: `v1`  
Base URL local: `http://localhost:8000/api/v1`  
Backend: FastAPI. Authentication: Firebase ID Token.

## 1. Rules shared by Android and Web

- All protected requests send `Authorization: Bearer <firebase_id_token>`.
- Firebase verifies identity only. Backend reads `users.role` and `users.status` from the database.
- Roles: `USER` (Android tenant) and `ADMIN` (Web management).
- Never accept `role`, `user_id`, or `created_by` as trusted values from the client. Derive the current user from the verified token.
- `401`: missing/invalid token; `403`: insufficient permission or blocked account; `404`: resource not found; `409`: business conflict; `422`: invalid request data.
- List endpoints use `page` and `limit` (default 1 and 20).
- Money is integer VND. Dates use ISO format (`YYYY-MM-DD`); timestamps use ISO 8601.
- Soft-deactivate rooms by setting `status = INACTIVE`; do not hard-delete rooms with requests/contracts.

Success list format:
```json
{
  "items": [],
  "total": 0,
  "page": 1,
  "limit": 20
}
```

Error format:
```json
{ "detail": "Human-readable error message" }
```

## 2. Authentication — Android and Web

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/auth/session` | Firebase token | Verify token and create/sync local user |
| GET | `/auth/me` | USER, ADMIN | Current account profile |

`POST /auth/session` has no JSON body; Firebase ID Token is sent in the Authorization header.

Example response:
```json
{
  "id": 12,
  "firebase_uid": "firebase-uid",
  "email": "tenant@example.com",
  "full_name": "Nguyen Van A",
  "phone": null,
  "avatar_url": null,
  "role": "USER",
  "status": "ACTIVE"
}
```

New accounts are always created as `USER`. Assign `ADMIN` out-of-band (trusted seed/admin process), never from client input.

## 3. Public room discovery — Android

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/rooms` | Search available rooms |
| GET | `/rooms/{room_id}` | Room details |

`GET /rooms` query parameters:
- `keyword`: search title/address
- `city`, `district`: location filters
- `min_price`, `max_price`: monthly price range
- `min_area`, `max_area`: area range
- `page`, `limit`: pagination

Example room item:
```json
{
  "id": 3,
  "title": "Studio near university",
  "description": "Furnished studio",
  "address": "12 Example Street",
  "ward": null,
  "district": "District 10",
  "city": "Ho Chi Minh City",
  "monthly_price": 4500000,
  "area_m2": 25.0,
  "capacity": 2,
  "amenities": ["wifi", "parking"],
  "status": "AVAILABLE",
  "images": ["https://example.com/room-1.jpg"]
}
```

Only `AVAILABLE` rooms are returned by the User-facing endpoints.

## 4. User APIs — Android

All endpoints below require an active `USER` account.

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/users/me` | View own profile |
| PATCH | `/users/me` | Update own full_name, phone, avatar_url |
| GET | `/users/me/favorites` | List favorite rooms |
| POST | `/users/me/favorites/{room_id}` | Add favorite |
| DELETE | `/users/me/favorites/{room_id}` | Remove favorite |
| POST | `/rooms/{room_id}/rental-requests` | Submit rental request |
| GET | `/users/me/rental-requests` | List own requests |
| GET | `/users/me/contracts` | List own rental contracts |
| GET | `/users/me/payments` | List own payment records |

Rental request body:
```json
{
  "message": "I would like to arrange a viewing.",
  "desired_move_in": "2026-12-01"
}
```

Rental request response:
```json
{
  "id": 21,
  "room_id": 3,
  "user_id": 12,
  "message": "I would like to arrange a viewing.",
  "desired_move_in": "2026-12-01",
  "status": "PENDING",
  "created_at": "2026-11-11T10:00:00"
}
```

Business rules:
- User can only read/update their own profile, requests, contracts, and payments.
- Reject a request if the room is not `AVAILABLE`.
- Prevent duplicate active `PENDING` requests from the same user for the same room.
- User cannot approve their own rental request or change its status directly.
- User can cancel their own request only while it is `PENDING`.

## 5. Admin APIs — Web

All endpoints below require `ADMIN`.

### Dashboard and room management

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/admin/dashboard` | Summary counts |
| GET | `/admin/rooms` | List all rooms, including inactive/rented |
| POST | `/admin/rooms` | Create room |
| GET | `/admin/rooms/{room_id}` | Room details |
| PATCH | `/admin/rooms/{room_id}` | Update room |
| PATCH | `/admin/rooms/{room_id}/status` | Set AVAILABLE/INACTIVE |
| POST | `/admin/rooms/{room_id}/images` | Add image URL |
| DELETE | `/admin/rooms/{room_id}/images/{image_id}` | Remove image |

Create room body:
```json
{
  "title": "Studio near university",
  "description": "Furnished studio",
  "address": "12 Example Street",
  "ward": "Ward 4",
  "district": "District 10",
  "city": "Ho Chi Minh City",
  "monthly_price": 4500000,
  "area_m2": 25,
  "capacity": 2,
  "amenities": ["wifi", "parking"]
}
```

### User management

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/admin/users` | Search/list users |
| GET | `/admin/users/{user_id}` | User details |
| PATCH | `/admin/users/{user_id}/status` | Block/unblock user |

Status body:
```json
{ "status": "BLOCKED" }
```
Allowed values: `ACTIVE`, `BLOCKED`. Prevent blocking/demoting the last active admin.

### Rental request management

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/admin/rental-requests` | List/filter requests |
| GET | `/admin/rental-requests/{request_id}` | Request details |
| PATCH | `/admin/rental-requests/{request_id}/status` | Approve/reject request |

Status body:
```json
{ "status": "APPROVED" }
```
Allowed transitions: `PENDING -> APPROVED` or `PENDING -> REJECTED`. When approving, create a rental contract in the same database transaction and change room status to `RENTED` if that is the agreed business rule. If viewing requests does not mean a confirmed rental, use a separate `VIEWING_REQUEST` workflow instead.

### Contract and payment management

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/admin/contracts` | List contracts |
| GET | `/admin/contracts/{contract_id}` | Contract details |
| POST | `/admin/contracts` | Create contract when not auto-created on approval |
| PATCH | `/admin/contracts/{contract_id}/status` | End/cancel contract |
| GET | `/admin/payments` | List payments |
| POST | `/admin/contracts/{contract_id}/payments` | Record a payment |
| PATCH | `/admin/payments/{payment_id}/status` | Update payment status |

Contract body:
```json
{
  "request_id": 21,
  "start_date": "2026-12-01",
  "end_date": null,
  "monthly_price": 4500000,
  "deposit_amount": 4500000
}
```

Payment body:
```json
{
  "amount": 4500000,
  "payment_type": "DEPOSIT",
  "payment_period": null,
  "note": "Deposit received"
}
```

Important: these payment APIs only record payment status. Do not claim online payment processing unless a payment gateway is integrated and verified.

## 6. Minimum MVP implementation order

1. Auth: `/auth/session`, `/auth/me`, role/status checking.
2. Rooms: public listing/detail + Admin create/update/deactivate.
3. Rental requests: User create/list/cancel + Admin list/approve/reject.
4. User management and dashboard.
5. Contracts and payments, if required by project scope.
6. Favorites and room images.

## 7. Ownership / implementation split

- Android developer: Firebase login UI, profile, room search/detail, favorites, rental request, own contracts/payments.
- Web developer: Admin login UI, dashboard, room CRUD, image management, user management, rental request review, contract/payment management.
- Shared Backend: agree on schema and this contract first. Then split FastAPI routers/services by feature. Both frontends use the same deployed API and database.
- During parallel development, frontend developers may use mock JSON matching this contract. Do not create separate database copies as the integration source of truth.

## 8. Suggested FastAPI router layout

```text
backend/
  main.py
  database.py
  schema.sql
  dependencies/
    auth.py
  routers/
    auth.py
    rooms.py
    users.py
    admin_rooms.py
    admin_users.py
    rental_requests.py
    contracts.py
    payments.py
```

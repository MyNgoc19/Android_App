
# Shared API Contract — ROOM_RENTAL_MANAGEMENT_SYSTEM

Version: v1

Base URL:
- Local backend: `http://localhost:8000/api/v1`
- Android Emulator: use `http://10.0.2.2:8000/api/v1` when using the standard Android Emulator.
- Physical Android device: use `http://<computer-lan-ip>:8000/api/v1`.
- Production: use the deployed HTTPS domain.

Backend: FastAPI  
Database: PostgreSQL  
Authentication: Firebase Authentication ID Token  
Image Storage: Firebase Storage

---

## 1. Shared Rules

### Authentication

All protected requests must include:

`Authorization: Bearer <firebase_id_token>`

- Firebase verifies the user's identity.
- Backend maps the verified Firebase UID to `users.firebase_uid`.
- Backend reads `users.role` and `users.status` from PostgreSQL.
- Never trust `role`, `user_id`, `tenant_id`, or `created_by` sent by the client.
- New accounts are always assigned `USER`.
- `ADMIN` accounts must be assigned through a trusted admin/seed process.

### Roles

| Role | Client | Permissions |
|---|---|---|
| USER | Android | Browse rooms, manage profile, favorites, rental requests, own contracts/payments |
| ADMIN | Web | Manage rooms, users, requests, contracts, payments, dashboard |

Blocked accounts (`status = BLOCKED`) cannot use protected business APIs.

### HTTP Status Codes

| Code | Meaning |
|---|---|
| 200 | Successful request |
| 201 | Resource created |
| 204 | Successful deletion, no response body |
| 400 | Invalid business operation |
| 401 | Missing or invalid token |
| 403 | Insufficient permission or blocked account |
| 404 | Resource not found |
| 409 | Business conflict |
| 422 | Invalid request data |

### Data Conventions

- Money: integer VND.
- Date: `YYYY-MM-DD`.
- Timestamp: ISO 8601 with timezone, preferably UTC (`Z`).
- IDs: PostgreSQL BIGINT, represented as JSON integers.
- `amenities`: JSON array of strings.
- Pagination: `page` starts at 1; default `limit` is 20.

List response:

{
  "items": [],
  "total": 0,
  "page": 1,
  "limit": 20
}

Error response:

{
  "detail": "Human-readable error message"
}

### Database Access

Android and Web must never connect directly to PostgreSQL.

Both clients call the same FastAPI backend. FastAPI is responsible for authentication, authorization, validation, transactions, and database access.

---

## 2. Authentication

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/auth/session` | Firebase token | Verify token and create/sync local user |
| GET | `/auth/me` | USER, ADMIN | Get current account |

`POST /auth/session`:
- No JSON body.
- Firebase ID Token is sent in the Authorization header.
- Backend verifies the token and creates a local user if one does not exist.
- New users receive role `USER` and status `ACTIVE`.
- Existing users are loaded from the database.

Example response:

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

---

## 3. Public Room Discovery — Android

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/rooms` | Search available rooms |
| GET | `/rooms/{room_id}` | Get room details |

`GET /rooms` query parameters:

- `keyword`: search title/address.
- `city`: filter by city.
- `district`: filter by district.
- `min_price`, `max_price`: monthly price range.
- `min_area`, `max_area`: room area range.
- `page`, `limit`: pagination.

Only rooms with `status = AVAILABLE` are returned by User-facing endpoints.

Example room response:

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
  "images": [
    "https://example.com/room-1.jpg"
  ]
}

---

## 4. User APIs — Android

All endpoints require an authenticated, active `USER` account.

### Profile

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/users/me` | Get own profile |
| PATCH | `/users/me` | Update own profile |

Allowed profile fields:
- `full_name`
- `phone`
- `avatar_url`

User cannot update their own role or account status.

### Favorites

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/users/me/favorites` | List favorite rooms |
| POST | `/users/me/favorites/{room_id}` | Add favorite |
| DELETE | `/users/me/favorites/{room_id}` | Remove favorite |

A user cannot add an inactive or unavailable room as a new favorite. Existing favorites may remain stored if a room later becomes unavailable.

### Rental Requests

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/rooms/{room_id}/rental-requests` | Submit rental request |
| GET | `/users/me/rental-requests` | List own requests |
| GET | `/users/me/rental-requests/{request_id}` | Get own request |
| PATCH | `/users/me/rental-requests/{request_id}/cancel` | Cancel own pending request |

Request body:

{
  "message": "I am interested in renting this room.",
  "desired_move_in": "2026-12-01"
}

Response:

{
  "id": 21,
  "room_id": 3,
  "user_id": 12,
  "message": "I am interested in renting this room.",
  "desired_move_in": "2026-12-01",
  "status": "PENDING",
  "created_at": "2026-11-11T10:00:00Z"
}

Business rules:

- The room must be `AVAILABLE`.
- A user cannot have more than one `PENDING` request for the same room.
- A user can cancel only their own `PENDING` request.
- Users cannot approve, reject, or directly change request status.
- Backend derives `user_id` from the verified token.

### Contracts and Payments

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/users/me/contracts` | List own contracts |
| GET | `/users/me/contracts/{contract_id}` | Get own contract |
| GET | `/users/me/payments` | List own payment records |

Users can only access their own contracts and payments.

---

## 5. Admin APIs — Web

All endpoints below require an authenticated, active `ADMIN` account.

### Dashboard

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/admin/dashboard` | Get management summary |

Dashboard counts should be calculated from current database records, with clearly defined status filters.

### Room Management

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/admin/rooms` | List all rooms |
| POST | `/admin/rooms` | Create room |
| GET | `/admin/rooms/{room_id}` | Get room details |
| PATCH | `/admin/rooms/{room_id}` | Update room information |
| PATCH | `/admin/rooms/{room_id}/status` | Set AVAILABLE/INACTIVE |
| POST | `/admin/rooms/{room_id}/images` | Add image URL |
| DELETE | `/admin/rooms/{room_id}/images/{image_id}` | Remove image |

Create room body:

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

Room status body:

{
  "status": "INACTIVE"
}

Allowed values:
- `AVAILABLE`
- `INACTIVE`

Admin cannot manually set `RENTED` through this endpoint. The backend sets `RENTED` when an active contract is created.

A room with an active contract cannot be changed to `AVAILABLE` or `INACTIVE` without first handling the contract.

### Room Images

- Admin uploads image files to Firebase Storage.
- The API receives and stores the resulting download URL in `room_images.image_url`.
- The API does not store binary image data in PostgreSQL.
- Backend must validate that the URL belongs to the configured storage provider.
- Removing a database image record and deleting the actual Storage object are separate operations; implement cleanup deliberately.

Add image body:

{
  "image_url": "https://example.com/room-image.jpg",
  "sort_order": 0
}

### User Management

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/admin/users` | Search/list users |
| GET | `/admin/users/{user_id}` | Get user details |
| PATCH | `/admin/users/{user_id}/status` | Block/unblock user |

Status body:

{
  "status": "BLOCKED"
}

Allowed values: `ACTIVE`, `BLOCKED`.

Do not allow an admin to block or demote the last active administrator.

### Rental Request Management

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/admin/rental-requests` | List/filter requests |
| GET | `/admin/rental-requests/{request_id}` | Get request details |
| PATCH | `/admin/rental-requests/{request_id}/status` | Approve/reject request |

Status body:

{
  "status": "APPROVED"
}

Allowed transitions:

- `PENDING -> APPROVED`
- `PENDING -> REJECTED`

Admin cannot approve an already cancelled or rejected request.

Approving a request does not automatically create a contract. Contract creation is a separate operation after the rental terms have been agreed.

### Contract Management

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/admin/contracts` | List contracts |
| GET | `/admin/contracts/{contract_id}` | Get contract details |
| POST | `/admin/contracts` | Create contract |
| PATCH | `/admin/contracts/{contract_id}/status` | End/cancel contract |

Create contract body:

{
  "request_id": 21,
  "start_date": "2026-12-01",
  "end_date": null,
  "monthly_price": 4500000,
  "deposit_amount": 4500000
}

Backend derives `room_id` and `tenant_id` from the approved rental request. Clients must not supply these fields.

Contract creation must execute in one database transaction:

1. Lock/check the approved rental request and its room.
2. Verify the request has no existing contract.
3. Verify the room is still available.
4. Insert the contract.
5. Update room status to `RENTED`.
6. Commit the transaction.

If any step fails, roll back the whole transaction.

Contract status transitions:

- `ACTIVE -> ENDED`
- `ACTIVE -> CANCELLED`

When a contract ends or is cancelled, backend must update room availability according to the agreed business rules and ensure no other active contract exists.

### Payment Management

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/admin/payments` | List payments |
| POST | `/admin/contracts/{contract_id}/payments` | Record payment |
| PATCH | `/admin/payments/{payment_id}/status` | Update payment status |

Payment body:

{
  "amount": 4500000,
  "payment_type": "DEPOSIT",
  "payment_period": null,
  "note": "Deposit received"
}

Payment types:
- `DEPOSIT`
- `RENT`
- `OTHER`

Payment statuses:
- `PENDING`
- `PAID`
- `FAILED`
- `REFUNDED`

For monthly rent, `payment_period` uses `YYYY-MM`. For non-periodic payments, it can be `null`.

These APIs only record payment information and status. They do not process online transactions or transfer money.

---

## 6. Status Reference

### Room

- `AVAILABLE`: can receive rental requests.
- `RENTED`: has an active rental contract.
- `INACTIVE`: not displayed in public room discovery.

### Rental Request

- `PENDING`: waiting for admin review.
- `APPROVED`: accepted, can proceed to contract creation.
- `REJECTED`: declined by admin.
- `CANCELLED`: cancelled by the requesting user.

### Contract

- `ACTIVE`: currently in effect.
- `ENDED`: completed.
- `CANCELLED`: terminated.

### Payment

- `PENDING`: awaiting confirmation.
- `PAID`: payment received.
- `FAILED`: payment failed.
- `REFUNDED`: payment refunded.

---

## 7. Minimum MVP Implementation Order

1. Authentication and authorization.
2. Public room listing/detail and Admin room management.
3. Rental request create/list/cancel and Admin review.
4. Contract creation and room status update.
5. User management and dashboard.
6. Payment records.
7. Favorites and room images.

---

## 8. Development Split

### Android User

- Firebase login.
- User profile.
- Room listing and detail.
- Favorites.
- Rental request create/list/cancel.
- Own contracts and payments.

### Web Admin

- Admin login.
- Dashboard.
- Room CRUD and status.
- Room image management.
- User management.
- Rental request review.
- Contract and payment management.

### Shared Backend

- Firebase token verification.
- Role and account status checks.
- FastAPI routers and services.
- PostgreSQL database access.
- Business validation and transactions.

Both frontends must use the same API contract and backend. During parallel development, frontend developers may use mock JSON matching this document.

Do not create separate databases as integration sources of truth.

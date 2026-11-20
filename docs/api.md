
# Tài liệu đặc tả API dùng chung — HỆ THỐNG QUẢN LÝ PHÒNG THUÊ

Phiên bản: v1

Địa chỉ API gốc:
- Local backend: `http://localhost:8000/api/v1`
- Android Emulator: use `http://10.0.2.2:8000/api/v1` when using the standard Android Emulator.
- Physical Android device: use `http://<computer-lan-ip>:8000/api/v1`.
- Production: use the deployed HTTPS domain.

Máy chủ: FastAPI  
Cơ sở dữ liệu: PostgreSQL  
Xác thực: Mã ID Token của Firebase Authentication  
Lưu trữ hình ảnh: Cloudinary

---

## 1. Quy tắc dùng chung

### Xác thực

Tất cả yêu cầu đến API cần xác thực phải gửi kèm:

`Authorization: Bearer <firebase_id_token>`

- Firebase xác minh danh tính người dùng.
- Backend đối chiếu Firebase UID đã xác minh với `users.firebase_uid`.
- Backend đọc `users.role` và `users.status` từ PostgreSQL.
- Không tin tưởng các giá trị `role`, `user_id`, `tenant_id` hoặc `created_by` do ứng dụng gửi lên.
- Tài khoản mới luôn được gán vai trò `USER`.
- Tài khoản `ADMIN` phải được cấp quyền thông qua quy trình quản trị đáng tin cậy hoặc tệp dữ liệu khởi tạo.

### Vai trò tài khoản

| Role | Client | Permissions |
|---|---|---|
| USER | Android | Browse rooms, manage profile, favorites, rental requests, own contracts/payments |
| ADMIN | Web | Manage rooms, users, requests, contracts, payments, dashboard |

Tài khoản bị khóa (`status = BLOCKED`) không thể sử dụng các API nghiệp vụ cần xác thực.

### Mã trạng thái HTTP

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

### Quy ước dữ liệu

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

### Truy cập cơ sở dữ liệu

Android và Web không được kết nối trực tiếp đến PostgreSQL.

Cả hai ứng dụng đều gọi chung Backend FastAPI. FastAPI chịu trách nhiệm xác thực, phân quyền, kiểm tra dữ liệu, quản lý giao dịch và truy cập cơ sở dữ liệu.

---

## 2. Xác thực tài khoản

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

## 3. Tìm kiếm phòng công khai — Android

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

Các API dành cho người thuê chỉ trả về phòng có `status = AVAILABLE`.

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

## 4. API dành cho người thuê — Android

Tất cả endpoint yêu cầu tài khoản `USER` đã đăng nhập và đang hoạt động.

### Hồ sơ cá nhân

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/users/me` | Get own profile |
| PATCH | `/users/me` | Update own profile |

Allowed profile fields:
- `full_name`
- `phone`
- `avatar_url`
- `gender` (`MALE`, `FEMALE`, `OTHER`)

User cannot update their own role or account status. Avatar uploads use `GET /users/me/avatar/signature` to obtain a signed Cloudinary upload payload, then save the returned HTTPS URL through `PATCH /users/me`.

### Danh sách yêu thích

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/users/me/favorites` | List favorite rooms |
| POST | `/users/me/favorites/{room_id}` | Add favorite |
| DELETE | `/users/me/favorites/{room_id}` | Remove favorite |

A user cannot add an inactive or unavailable room as a new favorite. Existing favorites may remain stored if a room later becomes unavailable.

### Yêu cầu thuê phòng

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

### Hợp đồng và thanh toán

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/users/me/contracts` | List own contracts |
| GET | `/users/me/contracts/{contract_id}` | Get own contract |
| GET | `/users/me/payments` | List own payment records |

Người thuê chỉ được xem hợp đồng và khoản thanh toán của chính mình.

---

## 5. API dành cho quản trị viên — Web

Tất cả endpoint bên dưới yêu cầu tài khoản `ADMIN` đã đăng nhập và đang hoạt động.

### Bảng điều khiển

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/admin/dashboard` | Get management summary |

Dashboard counts should be calculated from current database records, with clearly defined status filters.

### Quản lý phòng

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/admin/rooms` | List all rooms |
| POST | `/admin/rooms` | Create room |
| GET | `/admin/rooms/{room_id}` | Get room details |
| PATCH | `/admin/rooms/{room_id}` | Update room information |
| PATCH | `/admin/rooms/{room_id}/status` | Set AVAILABLE/INACTIVE |
| POST | `/admin/rooms/{room_id}/images/signature` | Create a signed Cloudinary upload payload |
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

### Hình ảnh phòng

- Admin requests a signed upload payload from `POST /admin/rooms/{room_id}/images/signature`.
- The browser uploads the image directly to the configured Cloudinary cloud using that signature.
- The API stores the resulting HTTPS Cloudinary URL in `room_images.image_url`.
- PostgreSQL stores image URLs and metadata, not binary image data.
- Backend validates that submitted image URLs belong to the configured Cloudinary cloud.
- Deleting a room image removes the Cloudinary asset and its database record.

Add image body:

{
  "image_url": "https://example.com/room-image.jpg",
  "sort_order": 0
}

### Quản lý tài khoản

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/admin/users` | Search/list users |
| GET | `/admin/users/{user_id}` | Get user details |
| PATCH | `/admin/users/{user_id}/status` | Block/unblock user |
| PATCH | `/admin/users/{user_id}/role` | Grant/revoke administrator role |

Status body:

{
  "status": "BLOCKED"
}

Allowed values: `ACTIVE`, `BLOCKED`.

Do not allow an admin to block or demote the last active administrator.

### Quản lý yêu cầu thuê

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

Việc duyệt yêu cầu không tự động tạo hợp đồng. Hợp đồng phải được tạo bằng thao tác riêng sau khi hai bên thống nhất điều khoản thuê.

### Quản lý hợp đồng

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
6. Reject any other pending requests for the same room because it is no longer available.
7. Commit the transaction.

If any step fails, roll back the whole transaction.

Contract status transitions:

- `ACTIVE -> ENDED`
- `ACTIVE -> CANCELLED`

When a contract ends or is cancelled, backend must update room availability according to the agreed business rules and ensure no other active contract exists.

### Quản lý thanh toán

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/admin/payments` | List payments |
| GET | `/admin/payments/{payment_id}` | Get payment details |
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

Các API này chỉ ghi nhận thông tin và trạng thái thanh toán. Chúng không xử lý giao dịch trực tuyến hoặc chuyển tiền.

---


## 6. Yêu cầu sửa chữa — Android và Web Admin

Các endpoint dành cho người thuê yêu cầu tài khoản `USER` đã đăng nhập và đang hoạt động. Yêu cầu sửa chữa phải thuộc hợp đồng thuê còn hiệu lực của chính người dùng đó.

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/users/me/maintenance-requests` | USER | List own maintenance requests |
| POST | `/users/me/maintenance-requests` | USER | Create a maintenance request |
| PATCH | `/users/me/maintenance-requests/{request_id}/cancel` | USER | Cancel own pending request |
| GET | `/admin/maintenance-requests` | ADMIN | List/filter all requests |
| GET | `/admin/maintenance-requests/{request_id}` | ADMIN | Get request details |
| PATCH | `/admin/maintenance-requests/{request_id}/status` | ADMIN | Update request status and admin note |

Create body:

```json
{
  "title": "Air conditioner is not cooling",
  "description": "The unit turns on but does not cool the room.",
  "priority": "MEDIUM",
  "image_url": null
}
```

Priority values are `LOW`, `MEDIUM`, and `HIGH`. Admin status values are `IN_PROGRESS`, `DONE`, and `REJECTED`. User cancellation is allowed only while status is `PENDING`.

Allowed admin transitions:
- `PENDING -> IN_PROGRESS | DONE | REJECTED`
- `IN_PROGRESS -> DONE | REJECTED`

## 7. Yêu cầu trả phòng — Android và Web Admin

Người thuê phải có hợp đồng còn hiệu lực mới được gửi yêu cầu trả phòng. Ngày trả phòng không được nằm trong quá khứ hoặc trước ngày bắt đầu hợp đồng.

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/users/me/move-out-requests` | USER | List own requests |
| POST | `/users/me/move-out-requests` | USER | Submit a move-out request |
| PATCH | `/users/me/move-out-requests/{request_id}/cancel` | USER | Cancel own pending request |
| GET | `/admin/move-out-requests` | ADMIN | List/filter all requests |
| GET | `/admin/move-out-requests/{request_id}` | ADMIN | Get request details |
| PATCH | `/admin/move-out-requests/{request_id}/status` | ADMIN | Approve, reject, or complete a request |

Create body:

```json
{
  "move_out_date": "2026-12-31",
  "reason": "Moving to another area"
}
```

Admin status values are `APPROVED`, `REJECTED`, and `COMPLETED`. Allowed transitions:
- `PENDING -> APPROVED | REJECTED`
- `APPROVED -> COMPLETED | REJECTED`

When a move-out request becomes `COMPLETED`, Backend ends the related contract, records the move-out date as the contract end date, and makes the room available again.

## 8. Các bảng mới có cấu trúc, chưa có đầy đủ chức năng

Cơ sở dữ liệu có các bảng `viewing_appointments`, `meter_readings` và `payment_accounts`. Hiện tại, ứng dụng FastAPI chưa đăng ký đầy đủ luồng API tương ứng cho các bảng này. Không tích hợp giao diện với các bảng đó cho đến khi đặc tả API và endpoint Backend được triển khai.

Current payment APIs record payment entries and let Admin transition payment status. They do not expose a user-side transfer-report endpoint or online payment processing. Payment status values accepted by the current API are `PENDING`, `PAID`, `FAILED`, and `REFUNDED`.

## 9. Danh sách trạng thái

### Phòng

- `AVAILABLE`: can receive rental requests.
- `RENTED`: has an active rental contract.
- `INACTIVE`: not displayed in public room discovery.

### Yêu cầu thuê

- `PENDING`: waiting for admin review.
- `APPROVED`: accepted, can proceed to contract creation.
- `REJECTED`: declined by admin.
- `CANCELLED`: cancelled by the requesting user.

### Hợp đồng

- `ACTIVE`: currently in effect.
- `ENDED`: completed.
- `CANCELLED`: terminated.

### Thanh toán

- `PENDING`: awaiting confirmation.
- `PAID`: payment received.
- `FAILED`: payment failed.
- `REFUNDED`: payment refunded.

---

## 10. Thứ tự triển khai phiên bản tối thiểu

1. Authentication and authorization.
2. Public room listing/detail and Admin room management.
3. Rental request create/list/cancel and Admin review.
4. Contract creation and room status update.
5. User management and dashboard.
6. Payment records.
7. Favorites and room images.

---

## 11. Phân chia công việc phát triển

### Ứng dụng Android cho người thuê

- Firebase login.
- User profile.
- Room listing and detail.
- Favorites.
- Rental request create/list/cancel.
- Own contracts and payments.

### Trang quản trị Web

- Admin login.
- Dashboard.
- Room CRUD and status.
- Room image management.
- User management.
- Rental request review.
- Contract and payment management.

### Backend dùng chung

- Firebase token verification.
- Role and account status checks.
- FastAPI routers and services.
- PostgreSQL database access.
- Business validation and transactions.

Hai giao diện phải sử dụng chung đặc tả API và Backend. Trong quá trình phát triển song song, thành viên phụ trách giao diện có thể dùng dữ liệu JSON giả lập theo đúng cấu trúc trong tài liệu này.

Không tạo các cơ sở dữ liệu riêng làm nguồn dữ liệu chính để tích hợp.

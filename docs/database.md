# Database Design — N&N Home

## 1. Tổng quan

Hệ thống sử dụng PostgreSQL làm nguồn dữ liệu chung cho Android User, Web Admin và FastAPI Backend. Chỉ Backend kết nối trực tiếp PostgreSQL; hai frontend gọi REST API.

Schema khởi tạo và nâng cấp database nằm tại `backend/schemas/schema.sql`. File hiện tạo 13 bảng, các khóa ngoại, ràng buộc, index và trigger cập nhật `updated_at`.

## 2. Danh sách bảng

| Bảng | Mục đích |
|---|---|
| `users` | Tài khoản User/Admin được đồng bộ từ Firebase Authentication. |
| `rooms` | Thông tin phòng cho thuê, giá, sức chứa, tiện ích và trạng thái. |
| `room_images` | URL ảnh phòng lưu trên Cloudinary và thứ tự hiển thị. |
| `favorites` | Quan hệ User yêu thích phòng; khóa chính ghép user_id và room_id. |
| `viewing_appointments` | Lịch hẹn xem phòng và trạng thái xử lý của Admin. |
| `rental_requests` | Yêu cầu thuê phòng do User gửi và Admin duyệt. |
| `rental_contracts` | Hợp đồng phát sinh từ yêu cầu thuê đã được duyệt. |
| `payments` | Các khoản tiền cọc, tiền thuê và khoản thu khác. |
| `meter_readings` | Chỉ số điện/nước theo kỳ gắn với hợp đồng và khoản thanh toán. |
| `maintenance_requests` | Yêu cầu sửa chữa gắn với hợp đồng đang hiệu lực. |
| `move_out_requests` | Yêu cầu trả phòng gắn với hợp đồng. |
| `payment_accounts` | Thông tin tài khoản nhận tiền được hiển thị cho người thuê. |
| `activity_logs` | Nhật ký thao tác của người dùng/quản trị viên. |

## 3. Cấu trúc các bảng

### `users`

Tài khoản User/Admin được đồng bộ từ Firebase Authentication.

Cột: `id`, `firebase_uid`, `email`, `full_name`, `phone`, `avatar_url`, `gender`, `role`, `status`, `created_at`, `updated_at`.

### `rooms`

Thông tin phòng cho thuê, giá, sức chứa, tiện ích và trạng thái.

Cột: `id`, `title`, `description`, `room_type`, `address`, `ward`, `district`, `city`, `monthly_price`, `deposit_amount`, `electricity_price`, `water_price`, `service_fee`, `area_m2`, `capacity`, `amenities`, `status`, `created_by`, `created_at`, `updated_at`.

### `room_images`

URL ảnh phòng lưu trên Cloudinary và thứ tự hiển thị.

Cột: `id`, `room_id`, `image_url`, `sort_order`, `created_at`.

### `favorites`

Quan hệ User yêu thích phòng; khóa chính ghép user_id và room_id.

Cột: `user_id`, `room_id`, `created_at`.

### `viewing_appointments`

Lịch hẹn xem phòng và trạng thái xử lý của Admin.

Cột: `id`, `room_id`, `user_id`, `scheduled_at`, `note`, `status`, `admin_note`, `created_at`, `updated_at`.

### `rental_requests`

Yêu cầu thuê phòng do User gửi và Admin duyệt.

Cột: `id`, `room_id`, `user_id`, `message`, `desired_move_in`, `status`, `created_at`, `updated_at`.

### `rental_contracts`

Hợp đồng phát sinh từ yêu cầu thuê đã được duyệt.

Cột: `id`, `request_id`, `room_id`, `tenant_id`, `start_date`, `end_date`, `monthly_price`, `deposit_amount`, `terms`, `status`, `created_at`, `updated_at`.

### `payments`

Các khoản tiền cọc, tiền thuê và khoản thu khác.

Cột: `id`, `contract_id`, `amount`, `payment_type`, `payment_period`, `due_date`, `status`, `method`, `transaction_code`, `submitted_at`, `paid_at`, `note`, `created_at`, `updated_at`.

### `meter_readings`

Chỉ số điện/nước theo kỳ gắn với hợp đồng và khoản thanh toán.

Cột: `id`, `contract_id`, `payment_id`, `period`, `electricity_old`, `electricity_new`, `water_old`, `water_new`, `electricity_price`, `water_price`, `recorded_by`, `created_at`.

### `maintenance_requests`

Yêu cầu sửa chữa gắn với hợp đồng đang hiệu lực.

Cột: `id`, `contract_id`, `user_id`, `title`, `description`, `priority`, `image_url`, `status`, `admin_note`, `resolved_at`, `created_at`, `updated_at`.

### `move_out_requests`

Yêu cầu trả phòng gắn với hợp đồng.

Cột: `id`, `contract_id`, `user_id`, `move_out_date`, `reason`, `status`, `admin_note`, `created_at`, `updated_at`.

### `payment_accounts`

Thông tin tài khoản nhận tiền được hiển thị cho người thuê.

Cột: `id`, `method`, `bank_name`, `account_number`, `account_holder`, `is_active`, `created_at`, `updated_at`.

### `activity_logs`

Nhật ký thao tác của người dùng/quản trị viên.

Cột: `id`, `actor_id`, `action`, `target_type`, `target_id`, `details`, `created_at`.


## 4. Quan hệ dữ liệu

- `users` 1-N `rooms` qua `rooms.created_by`.
- `users` 1-N `rental_requests`; `rooms` 1-N `rental_requests`.
- `rental_requests` 1-0..1 `rental_contracts`; hợp đồng ràng buộc đồng thời đúng request, room và tenant.
- `users` 1-N `rental_contracts` qua `tenant_id`; `rooms` 1-N `rental_contracts` theo thời gian.
- `rooms` 1-N `room_images`.
- `users` N-N `rooms` qua `favorites`.
- `users` 1-N `viewing_appointments`; `rooms` 1-N `viewing_appointments`.
- `rental_contracts` 1-N `payments` và 1-N `meter_readings`.
- `rental_contracts` 1-N `maintenance_requests`.
- `rental_contracts` 1-N `move_out_requests`.
- `payments` 1-0..1 `meter_readings` qua `payment_id`.
- `users` 1-N `activity_logs` qua `actor_id`.
- `payment_accounts` lưu thông tin nhận tiền độc lập.

## 5. Trạng thái nghiệp vụ

| Nhóm | Giá trị |
|---|---|
| User | `ACTIVE`, `BLOCKED` |
| Role | `USER`, `ADMIN` |
| Room | `AVAILABLE`, `RENTED`, `INACTIVE` |
| Room type | `ROOM`, `APARTMENT` |
| Viewing appointment | `PENDING`, `CONFIRMED`, `REJECTED`, `CANCELLED`, `DONE` |
| Rental request | `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED` |
| Contract | `ACTIVE`, `ENDED`, `CANCELLED` |
| Payment type | `DEPOSIT`, `RENT`, `OTHER` |
| Payment status | `PENDING`, `PAID`, `FAILED`, `REFUNDED` |
| Payment method | `BANK_TRANSFER`, `CASH`, `MOMO` |
| Maintenance priority | `LOW`, `MEDIUM`, `HIGH` |
| Maintenance status | `PENDING`, `IN_PROGRESS`, `DONE`, `REJECTED`, `CANCELLED` |
| Move-out status | `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`, `COMPLETED` |

## 6. Ràng buộc quan trọng

- Email có unique constraint và unique index không phân biệt chữ hoa/chữ thường.
- Một User chỉ có tối đa một yêu cầu thuê `PENDING` cho cùng một phòng.
- Một phòng chỉ có tối đa một hợp đồng `ACTIVE`.
- Một yêu cầu thuê chỉ tạo được một hợp đồng.
- Một hợp đồng chỉ có một khoản thuê `PENDING` hoặc `PAID` cho mỗi kỳ.
- Một User chỉ có một lịch hẹn `PENDING` hoặc `CONFIRMED` cho cùng một phòng.
- Một hợp đồng chỉ có một yêu cầu trả phòng `PENDING` hoặc `APPROVED`.
- Tiền thuê (`RENT`) bắt buộc có `payment_period` theo định dạng `YYYY-MM`.
- Chỉ khoản thanh toán `PAID` mới bắt buộc có `paid_at`.
- Chỉ số điện/nước mới không được nhỏ hơn chỉ số cũ.
- Ngày kết thúc hợp đồng không được trước ngày bắt đầu.

## 7. Index và tự động cập nhật

Schema tạo index cho các truy vấn thường dùng như trạng thái/thành phố phòng, giá phòng, người thuê, hợp đồng, thanh toán, yêu cầu bảo trì và yêu cầu trả phòng.

Trigger `set_updated_at` tự cập nhật `updated_at` khi sửa các bảng có trường này.

## 8. Khởi tạo database

Tạo database PostgreSQL trước, sau đó chạy từ thư mục repository:

```bash
psql -U <postgres_user> -d <database_name> -v ON_ERROR_STOP=1 -f backend/schemas/schema.sql
```

Có thể chạy lại schema để tạo các bảng/index/trigger còn thiếu và áp dụng các bước nâng cấp đã khai báo. Trước khi chạy trên database đang có dữ liệu, cần sao lưu database.
\n## 9. Tài khoản Admin mẫu\n\nBảng `users` lưu Firebase UID và quyền ứng dụng, không lưu mật khẩu. Tài khoản demo phải được tạo trong Firebase Authentication trước, sau đó đăng nhập Web Admin một lần để Backend đồng bộ Firebase UID vào bảng `users`.\n\nTài khoản demo local: `admin.demo@nn-home.test`, mật khẩu `DemoAdmin@12345`. Sau khi đồng bộ, chạy `backend/schemas/seed_admin.sql` từ thư mục gốc repository để nâng tài khoản này lên `ADMIN` và đặt trạng thái `ACTIVE`. Hướng dẫn đầy đủ nằm trong [docs/admin-account.md](admin-account.md). Không sử dụng thông tin này trên production.\n
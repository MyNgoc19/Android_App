# Database Design – Room Rental Management System

## 1. Tổng quan

Dự án sử dụng **PostgreSQL** làm hệ quản trị cơ sở dữ liệu chung.

Hệ thống gồm 3 thành phần:

- **Android User:** Ứng dụng dành cho người thuê phòng.
- **Web Admin:** Trang quản trị dành cho quản trị viên.
- **FastAPI Backend:** Xử lý nghiệp vụ, xác thực người dùng và giao tiếp với database.

**Nguyên tắc kết nối:**

- Android và Web Admin chỉ gọi API của FastAPI.
- Chỉ FastAPI được phép kết nối trực tiếp đến PostgreSQL.
- Không lưu thông tin kết nối database trong Android hoặc Web Admin.
- Cả hai frontend sử dụng chung một backend và một database.

## 2. Danh sách bảng

Database hiện tại gồm 8 bảng:

| Bảng | Chức năng |
|---|---|
| `users` | Lưu thông tin người dùng và quản trị viên |
| `rooms` | Lưu thông tin phòng cho thuê |
| `room_images` | Lưu đường dẫn hình ảnh của phòng |
| `rental_requests` | Lưu yêu cầu thuê phòng của người dùng |
| `rental_contracts` | Lưu hợp đồng thuê phòng |
| `payments` | Lưu thông tin các khoản thanh toán |
| `favorites` | Lưu danh sách phòng yêu thích |
| `activity_logs` | Lưu lịch sử hoạt động quan trọng |

## 3. Quan hệ giữa các bảng

```text
users
 ├── rooms
 │    └── room_images
 │
 ├── rental_requests
 │    └── rental_contracts
 │         └── payments
 │
 ├── favorites
 │    └── rooms
 │
 └── activity_logs
```

Chi tiết quan hệ:

| Quan hệ | Loại quan hệ | Ý nghĩa |
|---|---|---|
| users – rooms | 1-N | Một Admin có thể đăng nhiều phòng |
| rooms – room_images | 1-N | Một phòng có thể có nhiều hình ảnh |
| users – rental_requests | 1-N | Một User có thể gửi nhiều yêu cầu thuê |
| rooms – rental_requests | 1-N | Một phòng có thể nhận nhiều yêu cầu thuê |
| rental_requests – rental_contracts | 1-0..1 | Một yêu cầu được duyệt có thể tạo tối đa một hợp đồng |
| rooms – rental_contracts | 1-N | Một phòng có thể có nhiều hợp đồng theo thời gian |
| users – rental_contracts | 1-N | Một User có thể có nhiều hợp đồng theo thời gian |
| rental_contracts – payments | 1-N | Một hợp đồng có thể có nhiều khoản thanh toán |
| users – favorites | 1-N | Một User có thể yêu thích nhiều phòng |
| rooms – favorites | 1-N | Một phòng có thể được nhiều User yêu thích |
| users – activity_logs | 1-N | Một User có thể tạo nhiều bản ghi hoạt động |

## 4. Mô tả các bảng

### 4.1. `users`

Lưu thông tin tài khoản người dùng và quản trị viên.

| Cột | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| id | BIGINT | Khóa chính |
| firebase_uid | VARCHAR(128) | UID do Firebase Authentication cung cấp |
| email | VARCHAR(255) | Email tài khoản |
| full_name | VARCHAR(150) | Họ và tên |
| phone | VARCHAR(30) | Số điện thoại |
| avatar_url | TEXT | Đường dẫn ảnh đại diện |
| role | VARCHAR(10) | USER hoặc ADMIN |
| status | VARCHAR(10) | ACTIVE hoặc BLOCKED |
| created_at | TIMESTAMPTZ | Thời điểm tạo |
| updated_at | TIMESTAMPTZ | Thời điểm cập nhật |

Lưu ý:
- `firebase_uid` dùng để liên kết tài khoản trong database với Firebase Authentication.
- Tài khoản đăng ký mới mặc định có role `USER`.
- Không cho phép người dùng tự thay đổi role thành `ADMIN`.
- Backend phải kiểm tra trạng thái tài khoản trước khi cho phép sử dụng chức năng.

### 4.2. `rooms`

Lưu thông tin phòng cho thuê.

| Cột | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| id | BIGINT | Khóa chính |
| title | VARCHAR(200) | Tên phòng |
| description | TEXT | Mô tả phòng |
| address | TEXT | Địa chỉ chi tiết |
| ward | VARCHAR(120) | Phường |
| district | VARCHAR(120) | Quận |
| city | VARCHAR(120) | Thành phố |
| monthly_price | BIGINT | Giá thuê mỗi tháng (VND) |
| area_m2 | NUMERIC(10,2) | Diện tích (m²) |
| capacity | INTEGER | Sức chứa |
| amenities | JSONB | Danh sách tiện ích |
| status | VARCHAR(10) | AVAILABLE, RENTED hoặc INACTIVE |
| created_by | BIGINT | ID Admin tạo phòng |
| created_at | TIMESTAMPTZ | Thời điểm tạo |
| updated_at | TIMESTAMPTZ | Thời điểm cập nhật |

Quy ước trạng thái:
- `AVAILABLE`: Phòng đang có thể cho thuê.
- `RENTED`: Phòng đang có hợp đồng thuê hiệu lực.
- `INACTIVE`: Phòng tạm ngừng kinh doanh hoặc không hiển thị để thuê.

### 4.3. `room_images`

Lưu hình ảnh của phòng.

| Cột | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| id | BIGINT | Khóa chính |
| room_id | BIGINT | ID phòng |
| image_url | TEXT | Đường dẫn hình ảnh |
| sort_order | INTEGER | Thứ tự hiển thị |
| created_at | TIMESTAMPTZ | Thời điểm thêm ảnh |

Quy ước:
- File ảnh được lưu trên dịch vụ lưu trữ bên ngoài, đề xuất sử dụng Firebase Storage.
- Database chỉ lưu URL của ảnh, không lưu trực tiếp file ảnh.
- Khi xóa phòng, các bản ghi hình ảnh liên quan cũng được xóa.

### 4.4. `rental_requests`

Lưu yêu cầu thuê phòng do User gửi.

| Cột | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| id | BIGINT | Khóa chính |
| room_id | BIGINT | ID phòng được yêu cầu thuê |
| user_id | BIGINT | ID người gửi yêu cầu |
| message | TEXT | Lời nhắn |
| desired_move_in | DATE | Ngày dự kiến chuyển vào |
| status | VARCHAR(10) | Trạng thái yêu cầu |
| created_at | TIMESTAMPTZ | Thời điểm tạo |
| updated_at | TIMESTAMPTZ | Thời điểm cập nhật |

Các trạng thái:
- `PENDING`: Đang chờ Admin xử lý.
- `APPROVED`: Được Admin chấp thuận.
- `REJECTED`: Bị Admin từ chối.
- `CANCELLED`: User đã hủy yêu cầu.

Quy tắc:
- Một User chỉ được có tối đa một yêu cầu `PENDING` cho cùng một phòng.
- User chỉ được hủy yêu cầu của chính mình khi yêu cầu đang ở trạng thái `PENDING`.
- Admin là người duyệt hoặc từ chối yêu cầu.
- `APPROVED` không đồng nghĩa với việc hợp đồng đã được tạo.

### 4.5. `rental_contracts`

Lưu thông tin hợp đồng thuê phòng.

| Cột | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| id | BIGINT | Khóa chính |
| request_id | BIGINT | ID yêu cầu thuê ban đầu |
| room_id | BIGINT | ID phòng |
| tenant_id | BIGINT | ID người thuê |
| start_date | DATE | Ngày bắt đầu hợp đồng |
| end_date | DATE | Ngày kết thúc hợp đồng |
| monthly_price | BIGINT | Giá thuê mỗi tháng (VND) |
| deposit_amount | BIGINT | Tiền đặt cọc (VND) |
| status | VARCHAR(10) | Trạng thái hợp đồng |
| created_at | TIMESTAMPTZ | Thời điểm tạo |
| updated_at | TIMESTAMPTZ | Thời điểm cập nhật |

Các trạng thái:
- `ACTIVE`: Hợp đồng đang có hiệu lực.
- `ENDED`: Hợp đồng đã kết thúc.
- `CANCELLED`: Hợp đồng đã bị hủy.

Quy tắc:
- Mỗi yêu cầu thuê chỉ được tạo tối đa một hợp đồng.
- Một phòng chỉ được có tối đa một hợp đồng `ACTIVE` tại một thời điểm.
- Khi tạo hợp đồng thành công, trạng thái phòng chuyển sang `RENTED`.
- Khi hợp đồng kết thúc hoặc bị hủy, việc chuyển phòng về `AVAILABLE` cần được xử lý theo nghiệp vụ và kiểm tra xem phòng có hợp đồng `ACTIVE` khác hay không.
- Tạo hợp đồng và cập nhật trạng thái phòng phải thực hiện trong cùng một database transaction.

### 4.6. `payments`

Lưu thông tin thanh toán liên quan đến hợp đồng.

| Cột | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| id | BIGINT | Khóa chính |
| contract_id | BIGINT | ID hợp đồng |
| amount | BIGINT | Số tiền thanh toán (VND) |
| payment_type | VARCHAR(10) | Loại thanh toán |
| payment_period | VARCHAR(7) | Kỳ thanh toán, định dạng YYYY-MM |
| status | VARCHAR(10) | Trạng thái thanh toán |
| paid_at | TIMESTAMPTZ | Thời điểm thanh toán thành công |
| note | TEXT | Ghi chú |
| created_at | TIMESTAMPTZ | Thời điểm tạo |

Loại thanh toán:
- `DEPOSIT`: Tiền đặt cọc.
- `RENT`: Tiền thuê phòng.
- `OTHER`: Khoản thanh toán khác.

Trạng thái:
- `PENDING`: Đang chờ thanh toán.
- `PAID`: Đã thanh toán.
- `FAILED`: Thanh toán thất bại.
- `REFUNDED`: Đã hoàn tiền.

Lưu ý:
- Bảng này dùng để ghi nhận và quản lý thông tin thanh toán.
- Chưa đồng nghĩa với việc hệ thống đã tích hợp cổng thanh toán trực tuyến.
- Với tiền thuê theo tháng, `payment_period` sử dụng định dạng `YYYY-MM`.

### 4.7. `favorites`

Lưu danh sách phòng yêu thích của User.

| Cột | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| user_id | BIGINT | ID người dùng |
| room_id | BIGINT | ID phòng |
| created_at | TIMESTAMPTZ | Thời điểm thêm yêu thích |

Quy tắc:
- Một User không thể thêm cùng một phòng vào danh sách yêu thích nhiều lần.
- Khi User xóa yêu thích, bản ghi tương ứng bị xóa.
- Khi User hoặc phòng bị xóa, dữ liệu yêu thích liên quan cũng bị xóa.

### 4.8. `activity_logs`

Lưu lịch sử hoạt động quan trọng của hệ thống.

| Cột | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| id | BIGINT | Khóa chính |
| actor_id | BIGINT | ID người thực hiện |
| action | VARCHAR(100) | Tên hành động |
| target_type | VARCHAR(50) | Loại đối tượng bị tác động |
| target_id | BIGINT | ID đối tượng bị tác động |
| details | JSONB | Thông tin bổ sung |
| created_at | TIMESTAMPTZ | Thời điểm thực hiện |

Ví dụ:
- Admin tạo phòng mới.
- Admin cập nhật thông tin phòng.
- Admin duyệt yêu cầu thuê.
- Admin tạo hợp đồng.
- User gửi yêu cầu thuê phòng.

`actor_id` có thể NULL nếu tài khoản thực hiện đã bị xóa.

## 5. Quy trình nghiệp vụ chính

### 5.1. Quy trình đăng ký và đăng nhập

1. User đăng ký hoặc đăng nhập thông qua Firebase Authentication.
2. Firebase trả về ID Token.
3. Android gửi ID Token đến FastAPI.
4. FastAPI xác minh token.
5. Backend kiểm tra hoặc tạo bản ghi tương ứng trong bảng `users`.
6. Tài khoản mới mặc định có role `USER`.
7. Backend trả về thông tin tài khoản và quyền truy cập.

Admin phải được cấp quyền thông qua quy trình quản trị riêng, không được tự đăng ký với role `ADMIN`.

### 5.2. Quy trình thuê phòng

1. User xem danh sách phòng.
2. User chọn một phòng có trạng thái `AVAILABLE`.
3. User gửi yêu cầu thuê phòng.
4. Backend tạo bản ghi trong `rental_requests` với trạng thái `PENDING`.
5. Admin xem và xử lý yêu cầu.
6. Admin chuyển yêu cầu thành `APPROVED` hoặc `REJECTED`.
7. Nếu được duyệt, hai bên thống nhất điều khoản thuê.
8. Admin tạo hợp đồng trong `rental_contracts`.
9. Backend cập nhật trạng thái phòng thành `RENTED`.
10. User có thể xem thông tin hợp đồng của mình.

Lưu ý: Bước 8 và 9 phải thực hiện trong cùng một transaction để tránh trường hợp tạo hợp đồng thành công nhưng trạng thái phòng không được cập nhật.

### 5.3. Quy trình thanh toán

1. User xem các khoản cần thanh toán của hợp đồng.
2. User thực hiện thanh toán theo phương thức được hệ thống hỗ trợ.
3. Backend ghi nhận kết quả vào bảng `payments`.
4. Khi thanh toán thành công, cập nhật trạng thái thành `PAID` và ghi nhận `paid_at`.
5. User và Admin có thể xem lịch sử thanh toán theo quyền của mình.

## 6. Quy tắc dữ liệu chung

- ID sử dụng `BIGINT` tự tăng.
- Thời gian sử dụng `TIMESTAMPTZ`.
- Ngày không cần giờ sử dụng `DATE`.
- Tiền sử dụng `BIGINT`, đơn vị VND, không lưu số tiền dưới dạng chuỗi.
- Diện tích sử dụng `NUMERIC(10,2)`.
- Các trường có tập giá trị cố định phải tuân thủ các trạng thái được định nghĩa trong schema.
- Backend phải kiểm tra dữ liệu đầu vào trước khi ghi vào database.
- Không cho phép frontend tự quyết định quyền truy cập bằng cách gửi role trong request.
- Các thao tác ảnh hưởng đến nhiều bảng phải sử dụng transaction khi cần thiết.

## 7. Bảo mật và phân quyền

| Chức năng | USER | ADMIN |
|---|---|---|
| Xem danh sách phòng | Có | Có |
| Xem chi tiết phòng | Có | Có |
| Quản lý phòng | Không | Có |
| Quản lý hình ảnh phòng | Không | Có |
| Gửi yêu cầu thuê | Có | Không |
| Hủy yêu cầu của bản thân | Có | Không |
| Xem yêu cầu thuê | Của bản thân | Tất cả |
| Duyệt yêu cầu thuê | Không | Có |
| Tạo hợp đồng | Không | Có |
| Xem hợp đồng | Của bản thân | Tất cả |
| Xem thanh toán | Của bản thân | Tất cả |
| Quản lý tài khoản | Không | Có |
| Quản lý trạng thái tài khoản | Không | Có |

Backend phải xác thực Firebase ID Token và kiểm tra role, status của tài khoản trước khi thực hiện các thao tác được bảo vệ.

## 8. Index và ràng buộc dữ liệu

Schema có các index nhằm cải thiện hiệu suất truy vấn:

- Tìm phòng theo trạng thái và thành phố.
- Tìm phòng theo giá thuê.
- Lấy yêu cầu thuê theo User và trạng thái.
- Lấy yêu cầu thuê theo phòng và trạng thái.
- Lấy hợp đồng theo người thuê và trạng thái.
- Lấy thanh toán theo hợp đồng và trạng thái.
- Lấy hình ảnh theo phòng và thứ tự hiển thị.

Các ràng buộc quan trọng:

- Không cho phép trùng email hoặc Firebase UID.
- Không cho phép trùng User và phòng trong danh sách yêu thích.
- Không cho phép một User có nhiều yêu cầu `PENDING` cho cùng một phòng.
- Không cho phép một phòng có nhiều hợp đồng `ACTIVE`.
- Không cho phép hợp đồng tham chiếu đến yêu cầu thuê có User hoặc phòng không khớp.

## 9. File SQL

File tạo cấu trúc database:

`backend/schemas/schema.sql`

File này là schema nền tảng dành cho một database PostgreSQL mới. Nếu database đã có dữ liệu hoặc cấu trúc cũ, cần viết migration riêng, không dùng file này để thay thế trực tiếp database hiện có.

**Lưu ý:** Tài liệu này mô tả thiết kế chung. Khi thay đổi tên bảng, cột, trạng thái hoặc nghiệp vụ, nhóm cần cập nhật đồng thời `docs/database.md`, `backend/schemas/schema.sql` và `docs/api.md` để tránh frontend và backend triển khai không thống nhất.
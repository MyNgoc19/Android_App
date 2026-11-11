# Room Rental Management System

Hệ thống quản lý phòng cho thuê, gồm ứng dụng Android dành cho người thuê và trang Web dành cho quản trị viên.

## 1. Giới thiệu dự án

Dự án được xây dựng nhằm hỗ trợ quản lý hoạt động cho thuê phòng, bao gồm:

- Xem danh sách và thông tin chi tiết phòng.
- Tìm kiếm phòng cho thuê.
- Quản lý phòng và hình ảnh.
- Gửi và xử lý yêu cầu thuê phòng.
- Quản lý hợp đồng thuê.
- Theo dõi thanh toán.
- Quản lý tài khoản người dùng.

## 2. Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Android User | Kotlin, Android Studio |
| Web Admin | JavaScript / TypeScript, React |
| Backend | Python, FastAPI |
| Database | PostgreSQL |
| Authentication | Firebase Authentication |
| Image Storage | Firebase Storage (dự kiến) |
| Cache | Redis (tùy nhu cầu) |

Lưu ý: Các công nghệ có ghi chú dự kiến hoặc tùy nhu cầu cần được nhóm xác nhận trước khi triển khai.

## 3. Cấu trúc dự án

```text
ROOM_RENTAL_MANAGEMENT_SYSTEM/
│
├── android-user/                 # Android User Application
│
├── web-admin/                    # Web Admin Application
│
├── backend/                      # Shared FastAPI Backend
│   ├── routers/
│   ├── services/
│   ├── models/
│   ├── schemas/
│   │   └── schema.sql
│   ├── database.py
│   ├── firebase_config.py
│   ├── redis_client.py
│   ├── main.py
│   ├── requirements.txt
│   └── .env.example
│
├── docs/
│   ├── api.md
│   └── database.md
│
├── .gitignore
└── README.md
```

Cấu trúc trên là cấu trúc mục tiêu. Một số thư mục và file có thể chưa được tạo đầy đủ trong giai đoạn chuẩn bị.

## 4. Kiến trúc hệ thống

```text
+-------------------+       +-------------------+
|                   |       |                   |
|   Android User    |       |    Web Admin      |
|                   |       |                   |
+---------+---------+       +---------+---------+
          |                           |
          |        REST API           |
          +-------------+-------------+
                        |
                        v
              +------------------+
              |                  |
              |  FastAPI Backend |
              |                  |
              +--------+---------+
                       |
                       v
              +------------------+
              |                  |
              |   PostgreSQL     |
              |                  |
              +------------------+
```

Nguyên tắc:

- Android và Web Admin sử dụng chung FastAPI Backend.
- Backend chịu trách nhiệm xử lý nghiệp vụ và phân quyền.
- Chỉ Backend được phép truy cập trực tiếp PostgreSQL.
- Hai frontend không kết nối database trực tiếp.

## 5. Tài liệu dự án

| Tài liệu | Nội dung |
|---|---|
| `docs/api.md` | Danh sách API và quy tắc request/response |
| `docs/database.md` | Thiết kế database và quan hệ giữa các bảng |
| `backend/schemas/schema.sql` | SQL tạo cấu trúc PostgreSQL |
| `backend/.env.example` | Danh sách biến môi trường cần cấu hình |

Khi thay đổi API hoặc database, thành viên phụ trách cần cập nhật tài liệu tương ứng để các thành viên khác sử dụng thống nhất.

## 6. Phân chia công việc

Dự án được chia thành hai phần frontend và một phần backend dùng chung.

| Thành viên / Nhóm | Phụ trách |
|---|---|
| Android Developer | Ứng dụng Android dành cho User |
| Web Developer | Website dành cho Admin |
| Backend | FastAPI, database, authentication và API |

Backend là thành phần dùng chung nên các thay đổi ảnh hưởng đến API hoặc database cần được trao đổi với cả hai frontend trước khi triển khai.

## 7. Quy ước Git

### Branch chính

- `master`: Chứa phiên bản ổn định và các phần chung đã được nhóm thống nhất.

### Branch phát triển

- `feature/android-user`: Phát triển ứng dụng Android User.
- `feature/web-admin`: Phát triển Web Admin.
- `feature/backend`: Phát triển Backend dùng chung (nếu nhóm phân công riêng).

### Quy trình làm việc

1. Cập nhật branch từ `master`.
2. Tạo hoặc chuyển sang branch phụ trách.
3. Thực hiện công việc trong phạm vi được phân công.
4. Commit với nội dung rõ ràng.
5. Push branch lên GitHub.
6. Tạo Pull Request về `master`.
7. Review và kiểm tra trước khi merge.

Không tự ý sửa các file dùng chung như `docs/api.md`, `docs/database.md`, `schema.sql` mà không trao đổi với các thành viên liên quan.

## 8. Cài đặt và chạy dự án

Các hướng dẫn chạy chi tiết sẽ được bổ sung sau khi nhóm hoàn tất cấu hình môi trường.

### Backend

Các bước dự kiến:

1. Cài Python và PostgreSQL.
2. Tạo database PostgreSQL.
3. Sao chép `backend/.env.example` thành `backend/.env`.
4. Điền thông tin cấu hình thực tế.
5. Cài thư viện từ `requirements.txt`.
6. Khởi tạo database bằng `schema.sql`.
7. Chạy FastAPI bằng Uvicorn.

### Android User

1. Mở thư mục `android-user/` bằng Android Studio.
2. Cấu hình SDK và các thông tin cần thiết.
3. Cấu hình địa chỉ Backend API.
4. Build và chạy ứng dụng.

### Web Admin

1. Mở thư mục `web-admin/`.
2. Cài các dependencies.
3. Cấu hình địa chỉ Backend API.
4. Chạy ứng dụng theo script trong `package.json`.

**Lưu ý:** Các bước trên là hướng dẫn dự kiến, chưa phải hướng dẫn chạy đã được kiểm thử.

## 9. Quy tắc bảo mật

- Không commit file `.env` lên GitHub.
- Không commit Firebase Service Account JSON.
- Không đưa thông tin mật khẩu hoặc API key vào source code.
- Backend phải xác thực Firebase ID Token.
- Backend phải kiểm tra quyền `USER` và `ADMIN`.
- Không tin tưởng role hoặc user ID do frontend tự gửi lên.
- Không để Android hoặc Web Admin kết nối trực tiếp PostgreSQL.

## 10. Trạng thái dự án

Dự án đang trong giai đoạn thống nhất kiến trúc, API, database và môi trường phát triển trước khi triển khai các chức năng.
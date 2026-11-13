# Room Rental Management System

Hệ thống quản lý phòng cho thuê, gồm ứng dụng Android dành cho người thuê và trang Web dành cho quản trị viên.

## 1. Giới thiệu dự án

Room Rental Management System là hệ thống hỗ trợ quản lý hoạt động cho thuê phòng, gồm hai ứng dụng:

- **Android User:** Dành cho người có nhu cầu tìm kiếm và thuê phòng.
- **Web Admin:** Dành cho quản trị viên quản lý phòng, người thuê và hoạt động cho thuê.

Hệ thống sử dụng FastAPI làm Backend trung gian, PostgreSQL để lưu trữ dữ liệu và Firebase Authentication để xác thực tài khoản.

Mục tiêu của phiên bản MVP là xây dựng các chức năng cốt lõi phục vụ quy trình tìm phòng, gửi yêu cầu thuê, quản lý hợp đồng và theo dõi thanh toán.

## 2. Phạm vi chức năng

### 2.1. MVP – Các chức năng triển khai

**Android User**

- Đăng nhập và xác thực tài khoản.
- Xem danh sách phòng và thông tin chi tiết phòng.
- Tìm kiếm và lọc phòng.
- Quản lý danh sách phòng yêu thích.
- Gửi yêu cầu thuê phòng.
- Theo dõi trạng thái yêu cầu thuê phòng.
- Xem thông tin hợp đồng.
- Xem lịch sử thanh toán.
- Xem và cập nhật thông tin cá nhân.

**Web Admin**

- Đăng nhập và xác thực quyền Admin.
- Xem Dashboard tổng quan.
- Quản lý phòng (thêm, xem, chỉnh sửa và cập nhật trạng thái).
- Quản lý hình ảnh phòng.
- Quản lý tài khoản người dùng.
- Quản lý yêu cầu thuê phòng.
- Duyệt hoặc từ chối yêu cầu thuê phòng.
- Quản lý hợp đồng thuê.
- Quản lý thông tin và trạng thái thanh toán.

### 2.2. Future Features – Định hướng phát triển tương lai

Một số giao diện trong thiết kế Figma được sử dụng để định hướng mở rộng sản phẩm, không thuộc phạm vi triển khai của phiên bản MVP.

Các chức năng chưa triển khai trong MVP:

- **Utility Billing:** Quản lý hóa đơn điện, nước và các chi phí tiện ích.
- **Reports & Analytics:** Báo cáo và phân tích dữ liệu nâng cao.

Đối với các chức năng trên:

- Chưa xây dựng nghiệp vụ thực tế.
- Chưa yêu cầu API riêng.
- Chưa bổ sung bảng database.
- Có thể hiển thị giao diện giới thiệu hoặc trạng thái Coming Soon nếu cần.

**Lưu ý:** Phạm vi triển khai MVP được xác định theo tài liệu API và Database đã thống nhất. Không tự ý mở rộng chức năng chỉ vì chức năng đó xuất hiện trong bản thiết kế Figma.

## 3. Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Android User | Kotlin, Android Studio |
| Web Admin | React, TypeScript, Vite |
| Backend | Python, FastAPI |
| ORM | SQLAlchemy |
| Database | PostgreSQL |
| Authentication | Firebase Authentication |
| Image Storage | Firebase Storage (dự kiến) |
| API Communication | REST API, JSON |

Lưu ý:

- PostgreSQL là hệ quản trị cơ sở dữ liệu được nhóm thống nhất sử dụng.
- Redis không nằm trong phạm vi MVP.
- Firebase Authentication được sử dụng để xác thực người dùng.
- Backend chịu trách nhiệm kiểm tra token và phân quyền theo dữ liệu lưu trong PostgreSQL.

## 4. Cấu trúc dự án

```text
ROOM_RENTAL_MANAGEMENT_SYSTEM/
│
├── android-user/                 # Android User Application
│
├── web-admin/                    # Web Admin Application
│
├── backend/                      # Shared FastAPI Backend
│   ├── routers/                  # API endpoints
│   ├── services/                 # Business logic
│   ├── models/                   # SQLAlchemy models
│   ├── schemas/                  # Pydantic schemas
│   │   └── schema.sql            # PostgreSQL database schema
│   ├── database.py               # Database connection
│   ├── firebase_config.py        # Firebase configuration
│   ├── main.py                   # FastAPI application entry point
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

Cấu trúc trên là cấu trúc mục tiêu của dự án. Một số thư mục và file có thể chưa được tạo hoặc hoàn thiện trong giai đoạn chuẩn bị.

Các thành viên sẽ triển khai và hoàn thiện từng phần trong quá trình phát triển.

## 5. Kiến trúc hệ thống

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

## 6. Tài liệu dự án

| Tài liệu | Nội dung |
|---|---|
| `docs/api.md` | Danh sách API và quy tắc request/response |
| `docs/database.md` | Thiết kế database và quan hệ giữa các bảng |
| `backend/schemas/schema.sql` | SQL tạo cấu trúc PostgreSQL |
| `backend/.env.example` | Danh sách biến môi trường cần cấu hình |

Khi thay đổi API hoặc database, thành viên phụ trách cần cập nhật tài liệu tương ứng để các thành viên khác sử dụng thống nhất.

## 7. Phân chia công việc

Dự án gồm hai ứng dụng frontend và một Backend dùng chung.

| Thành viên / Nhóm | Phạm vi phụ trách |
|---|---|
| Android Developer | Xây dựng ứng dụng Android dành cho User |
| Web Developer | Xây dựng website quản trị dành cho Admin |
| Backend Developer | Xây dựng FastAPI, database, authentication và API dùng chung |

### Android Developer

- Phát triển giao diện và chức năng dành cho User.
- Tích hợp Firebase Authentication.
- Gọi API từ Backend.
- Xử lý và hiển thị dữ liệu nhận được từ API.

### Web Developer

- Phát triển giao diện quản trị Admin.
- Tích hợp Firebase Authentication.
- Gọi API từ Backend.
- Xây dựng các chức năng quản lý trong phạm vi MVP.

### Backend Developer

- Xây dựng REST API bằng FastAPI.
- Thiết kế và quản lý PostgreSQL bằng SQLAlchemy.
- Xử lý nghiệp vụ trong Service.
- Xác thực Firebase ID Token.
- Kiểm tra quyền USER và ADMIN.
- Cung cấp API cho Android và Web Admin.

### Quy ước phối hợp

- Backend là thành phần dùng chung của hai ứng dụng.
- Các thay đổi liên quan đến API hoặc database phải được trao đổi với những thành viên liên quan.
- Không tự ý thay đổi cấu trúc database hoặc API đã thống nhất mà chưa thông báo cho nhóm.
- Khi API hoặc database thay đổi, cần cập nhật tài liệu tương ứng.

## 8. Quy ước Git

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

## 9. Cài đặt và chạy dự án

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

## 10. Quy tắc bảo mật

- Không commit file `.env` lên GitHub.
- Không commit Firebase Service Account JSON.
- Không đưa thông tin mật khẩu hoặc API key vào source code.
- Backend phải xác thực Firebase ID Token.
- Backend phải kiểm tra quyền `USER` và `ADMIN`.
- Không tin tưởng role hoặc user ID do frontend tự gửi lên.
- Không để Android hoặc Web Admin kết nối trực tiếp PostgreSQL.

## 11. Trạng thái dự án

Dự án hiện đang trong giai đoạn chuẩn bị trước khi triển khai.

Các nội dung đã được thống nhất:

- Kiến trúc tổng thể của hệ thống.
- Công nghệ sử dụng.
- PostgreSQL database schema.
- API specification.
- Phạm vi chức năng MVP.
- Cấu trúc thư mục mục tiêu.
- Quy trình làm việc trên GitHub.

Các công việc tiếp theo:

1. Hoàn thiện và xác nhận tài liệu chung.
2. Kiểm tra cấu trúc project của Android, Web và Backend.
3. Phân chia công việc cho từng thành viên.
4. Tạo branch riêng để phát triển.
5. Triển khai và tích hợp các chức năng MVP.

Các chức năng Utility Billing và Reports & Analytics được giữ lại làm định hướng phát triển trong tương lai, chưa thuộc phạm vi MVP.

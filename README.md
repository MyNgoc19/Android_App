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
| Android User | Java, Android Studio |
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

| Thành phần | Phạm vi phụ trách |
|---|---|
| Android User | Phát triển ứng dụng Android dành cho người thuê |
| Web Admin | Phát triển website quản trị dành cho Admin |
| Shared Backend | Xây dựng API, xử lý nghiệp vụ, xác thực và truy cập database cho cả hai ứng dụng |

### Android User

- Phát triển giao diện và chức năng dành cho User.
- Tích hợp Firebase Authentication.
- Gọi API từ FastAPI Backend.
- Hiển thị danh sách phòng, chi tiết phòng và chức năng tìm kiếm.
- Xây dựng chức năng yêu thích, gửi yêu cầu thuê và theo dõi trạng thái yêu cầu.
- Hiển thị thông tin hợp đồng và lịch sử thanh toán.
- Quản lý thông tin cá nhân.

### Web Admin

- Phát triển giao diện quản trị Admin.
- Tích hợp Firebase Authentication.
- Gọi API từ FastAPI Backend.
- Xây dựng Dashboard tổng quan.
- Quản lý phòng và hình ảnh phòng.
- Quản lý tài khoản người dùng.
- Quản lý yêu cầu thuê phòng.
- Quản lý hợp đồng và thông tin thanh toán.

### Shared Backend

- Xây dựng REST API bằng FastAPI.
- Thiết kế và quản lý PostgreSQL bằng SQLAlchemy.
- Xây dựng các lớp Router, Service, Model và Schema.
- Xử lý nghiệp vụ dùng chung cho Android và Web Admin.
- Xác thực Firebase ID Token.
- Kiểm tra quyền USER và ADMIN.
- Quản lý transaction và truy cập database an toàn.

### Quy ước phối hợp

- Backend là thành phần dùng chung, phục vụ cả Android User và Web Admin.
- Việc phân công người phụ trách Backend cần được nhóm thống nhất.
- Hai frontend phải sử dụng đúng API Contract trong `docs/api.md`.
- Không tự ý thay đổi cấu trúc database hoặc API đã thống nhất mà chưa trao đổi với các thành viên liên quan.
- Khi API hoặc database thay đổi, cần cập nhật tài liệu tương ứng.
- Các chức năng chưa thuộc MVP không được tự ý mở rộng vào phạm vi triển khai.

## 8. Quy ước Git

### Branch chính

- `master`: Chứa tài liệu chung và phiên bản ổn định của dự án.

### Branch phát triển

Các branch dự kiến:

- `feature/android-user`: Phát triển ứng dụng Android User.
- `feature/web-admin`: Phát triển Web Admin.
- `feature/backend`: Phát triển Backend dùng chung, nếu nhóm phân công người phụ trách riêng.

Tên branch có thể được điều chỉnh theo từng chức năng, ví dụ:

- `feature/authentication`
- `feature/room-management`
- `feature/rental-requests`
- `feature/contract-management`

### Quy trình làm việc

1. Cập nhật branch `master` mới nhất.
2. Tạo hoặc chuyển sang branch phụ trách.
3. Thực hiện công việc trong phạm vi được phân công.
4. Commit với nội dung rõ ràng.
5. Push branch lên GitHub.
6. Tạo Pull Request về `master`.
7. Review và kiểm tra trước khi merge.

### Quy ước khi làm việc chung

- Không tự ý sửa các file dùng chung như `docs/api.md`, `docs/database.md` và `backend/schemas/schema.sql` mà không trao đổi với thành viên liên quan.
- Khi có thay đổi API hoặc database, cần thông báo cho các thành viên đang tích hợp.
- Không commit file `.env`, Firebase Service Account JSON hoặc thông tin bí mật.
- Không push trực tiếp các thay đổi chưa kiểm tra lên branch chính.

## 9. Cài đặt và chạy dự án

Các hướng dẫn dưới đây là quy trình dự kiến. Chi tiết cấu hình và lệnh chạy sẽ được cập nhật sau khi nhóm hoàn thiện cấu trúc và kiểm thử từng thành phần.

### Backend

1. Cài đặt Python và PostgreSQL.
2. Tạo database PostgreSQL cho dự án.
3. Sao chép `backend/.env.example` thành `backend/.env`.
4. Điền các thông tin cấu hình cần thiết.
5. Tạo môi trường ảo Python.
6. Cài đặt thư viện từ `backend/requirements.txt`.
7. Khởi tạo database bằng `backend/schemas/schema.sql`.
8. Cấu hình Firebase Admin SDK và thông tin xác thực cần thiết.
9. Chạy FastAPI bằng Uvicorn.

### Android User

1. Cài đặt Android Studio và Android SDK.
2. Mở project trong thư mục `android-user/`.
3. Cấu hình Firebase cho ứng dụng Android.
4. Cấu hình địa chỉ Backend API.
5. Kết nối Android với Backend.
6. Build và chạy ứng dụng trên Emulator hoặc thiết bị thật.

Đối với Android Emulator sử dụng địa chỉ Backend:

`http://10.0.2.2:8000/api/v1`

### Web Admin

1. Cài đặt Node.js và npm.
2. Mở project trong thư mục `web-admin/`.
3. Cài đặt các dependencies.
4. Cấu hình Firebase cho Web Admin.
5. Cấu hình địa chỉ Backend API.
6. Chạy ứng dụng bằng script được khai báo trong `package.json`.

**Lưu ý:** Các bước trên chưa được kiểm thử trên project hoàn chỉnh. Không xem đây là hướng dẫn chạy chính thức cho đến khi nhóm hoàn tất cấu hình và kiểm tra thực tế.

## 10. Quy tắc bảo mật

- Không commit file `.env` lên GitHub.
- Không commit Firebase Service Account JSON.
- Không đưa mật khẩu hoặc API key vào source code.
- Backend phải xác thực Firebase ID Token trước khi xử lý các API được bảo vệ.
- Backend phải kiểm tra quyền `USER` và `ADMIN` dựa trên dữ liệu trong PostgreSQL.
- Không tin tưởng role hoặc user ID do frontend tự gửi lên.
- Không để Android hoặc Web Admin kết nối trực tiếp PostgreSQL.
- Không trả về thông tin nhạy cảm của người dùng trong API response.

## 11. Trạng thái dự án

Dự án hiện đang trong giai đoạn chuẩn bị và hoàn thiện cấu trúc trước khi triển khai chức năng.

### 11.1. Các nội dung đã thống nhất

- Kiến trúc tổng thể của hệ thống.
- Công nghệ sử dụng cho Android, Web và Backend.
- PostgreSQL là hệ quản trị cơ sở dữ liệu chung.
- Database schema gồm 8 bảng.
- API Contract giữa Android, Web và Backend.
- Phạm vi chức năng MVP.
- Các chức năng định hướng phát triển trong tương lai.
- Cấu trúc thư mục mục tiêu.
- Quy ước làm việc trên GitHub.

### 11.2. Các nội dung đang chuẩn bị triển khai

- Hoàn thiện cấu trúc project Android User.
- Hoàn thiện cấu trúc project Web Admin.
- Hoàn thiện cấu trúc FastAPI Backend.
- Cấu hình môi trường phát triển.
- Kết nối Firebase Authentication.
- Kết nối PostgreSQL.
- Phân chia công việc và tạo branch riêng cho từng phần.
- Triển khai, kiểm thử và tích hợp các chức năng MVP.

### 11.3. Phạm vi phát triển trong tương lai

Các chức năng sau được giữ lại trong thiết kế Figma để định hướng mở rộng:

- Utility Billing – Quản lý hóa đơn điện, nước và chi phí tiện ích.
- Reports & Analytics – Báo cáo và phân tích dữ liệu nâng cao.

Hai chức năng trên chưa thuộc phạm vi MVP, chưa yêu cầu API, database schema hoặc business logic riêng.

**Lưu ý:** Cấu trúc thư mục và các tài liệu hiện tại là nền tảng chuẩn bị cho quá trình phát triển. Không đồng nghĩa với việc các ứng dụng đã hoàn thiện hoặc có thể chạy được.

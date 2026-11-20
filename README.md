# N&N Home — Room Rental Management System

Hệ thống quản lý phòng cho thuê, gồm ứng dụng Android dành cho người tìm thuê và website Web Admin dành cho quản trị viên. Hai ứng dụng sử dụng chung một Backend FastAPI và cơ sở dữ liệu PostgreSQL.

## 1. Tổng quan

**N&N Home** hỗ trợ quy trình quản lý phòng cho thuê từ lúc đăng tin, tìm kiếm phòng, gửi yêu cầu thuê, tạo hợp đồng đến theo dõi thanh toán và xử lý các yêu cầu trong thời gian thuê.

### Thành phần hệ thống

| Thành phần | Mô tả | Công nghệ chính |
|---|---|---|
| Android User | Ứng dụng dành cho người thuê phòng | Java, Android Studio |
| Web Admin | Website quản trị dành cho Admin | React, TypeScript, Vite |
| Backend | API và xử lý nghiệp vụ dùng chung | Python, FastAPI |
| Database | Lưu trữ dữ liệu hệ thống | PostgreSQL, SQLAlchemy |
| Authentication | Đăng nhập và xác thực danh tính | Firebase Authentication |
| Image hosting | Lưu trữ hình ảnh phòng và ảnh đại diện | Cloudinary |

## 2. Chức năng chính

### 2.1. Android User

Ứng dụng Android dành cho người thuê, hiện có các nhóm chức năng:

- **Tài khoản:** đăng ký, đăng nhập, đăng nhập Google, xem và cập nhật hồ sơ.
- **Khám phá phòng:** xem danh sách phòng, xem chi tiết, tìm kiếm và lọc phòng.
- **Yêu thích:** thêm và quản lý danh sách phòng yêu thích.
- **Yêu cầu thuê:** gửi yêu cầu thuê, chọn ngày dự kiến bắt đầu và thời hạn thuê, theo dõi trạng thái xử lý.
- **Phòng và hợp đồng của tôi:** xem thông tin phòng đang thuê và hợp đồng.
- **Thanh toán:** xem các khoản thanh toán, tiền cọc và lịch sử thanh toán.
- **Yêu cầu sửa chữa:** gửi yêu cầu bảo trì/sửa chữa trong thời gian có hợp đồng hiệu lực.
- **Yêu cầu trả phòng:** gửi và theo dõi yêu cầu trả phòng.
- **Hồ sơ cá nhân:** cập nhật thông tin và ảnh đại diện.

### 2.2. Web Admin

Website dành cho quản trị viên, gồm:

- **Dashboard:** xem thông tin tổng quan.
- **Quản lý phòng:** xem danh sách, thêm, chỉnh sửa, cập nhật trạng thái và quản lý hình ảnh phòng.
- **Quản lý người dùng:** xem thông tin, quản lý trạng thái và quyền tài khoản.
- **Quản lý yêu cầu thuê:** xem chi tiết, duyệt hoặc từ chối yêu cầu.
- **Quản lý hợp đồng:** tạo hợp đồng từ yêu cầu thuê đã được duyệt, xem và cập nhật trạng thái hợp đồng.
- **Quản lý thanh toán:** tạo và theo dõi các bản ghi thanh toán.
- **Quản lý yêu cầu sửa chữa:** xem và cập nhật trạng thái xử lý.
- **Quản lý yêu cầu trả phòng:** xem, duyệt/từ chối và hoàn tất quy trình trả phòng.

> **Lưu ý về nghiệp vụ thuê phòng:** Duyệt yêu cầu thuê không tự động làm phòng chuyển sang trạng thái đang thuê. Admin cần tạo hợp đồng riêng. Khi tạo hợp đồng thành công, Backend cập nhật phòng sang `RENTED`. Khi yêu cầu trả phòng được hoàn tất theo nghiệp vụ, hợp đồng được kết thúc và phòng có thể trở lại `AVAILABLE`.

> **Lưu ý về thanh toán:** Chức năng hiện tại quản lý và ghi nhận các khoản thanh toán trong hệ thống; không đồng nghĩa đã tích hợp cổng thanh toán trực tuyến hoặc xử lý giao dịch ngân hàng.

## 3. Kiến trúc hệ thống

Dự án sử dụng kiến trúc **Modular Monolith**: một Backend FastAPI duy nhất, chia thành các router/service theo nghiệp vụ. Không triển khai Microservices.

```text
+------------------------+        +------------------------+
|      Android User      |        |        Web Admin       |
|       Java             |        |    React + TypeScript  |
+-----------+------------+        +------------+-----------+
            |                                  |
            |             REST API             |
            +----------------+-----------------+
                             |
                             v
                  +----------------------+
                  |     FastAPI Backend  |
                  | Authentication       |
                  | Business Logic       |
                  | Validation           |
                  | Authorization        |
                  +----------+-----------+
                             |
                 +-----------+-----------+
                 |                       |
                 v                       v
        +------------------+    +-------------------+
        |    PostgreSQL    |    |    Cloudinary     |
        | Application data |    | Room/profile imgs |
        +------------------+    +-------------------+

                  Firebase Authentication
                  Identity verification
```

### Nguyên tắc tích hợp

- Android và Web Admin gọi chung REST API có prefix `/api/v1`.
- Chỉ Backend được kết nối trực tiếp tới PostgreSQL.
- Client gửi Firebase ID Token qua header `Authorization: Bearer <token>`.
- Backend xác minh token, ánh xạ Firebase UID với tài khoản trong bảng `users`, sau đó kiểm tra role và trạng thái tài khoản.
- Không tin tưởng `role`, `user_id` hoặc `tenant_id` do client tự gửi.
- Backend dùng Cloudinary để tạo chữ ký upload; database lưu URL ảnh thay vì lưu file nhị phân.

## 4. Công nghệ và thư viện

| Phần | Công nghệ |
|---|---|
| Android | Java, Android SDK, AndroidX, Material Components |
| Android networking | Retrofit, OkHttp, Gson |
| Android image loading | Glide |
| Android authentication | Firebase Authentication, Google Sign-In |
| Web | React 19, TypeScript, Vite, React Router |
| Web icons | lucide-react |
| Backend | Python, FastAPI, Uvicorn |
| Database access | SQLAlchemy, psycopg |
| Database | PostgreSQL |
| Authentication | Firebase Admin SDK |
| Image storage | Cloudinary |

## 5. Cấu trúc repository

```text
Android_App/
├── setup.bat                     # Windows setup
├── setup.ps1                     # PowerShell setup
├── setup.sh                      # macOS/Linux setup
├── .github/workflows/ci.yml      # Automated build and database checks
├── android-user/                 # Android User (Java)
│   ├── app/src/main/java/        # Activities, models, repositories, API
│   ├── app/src/main/res/         # Layouts, drawables, menus, resources
│   └── gradle/                   # Gradle wrapper và version catalog
│
├── web-admin/                    # Web Admin (React + TypeScript)
│   ├── src/components/           # Layout và component dùng chung
│   ├── src/contexts/             # Authentication, theme
│   ├── src/pages/                # Các trang quản trị
│   └── .env.example              # Mẫu cấu hình frontend
│
├── backend/                      # FastAPI Backend dùng chung
│   ├── routers/                  # API theo nhóm nghiệp vụ
│   ├── services/                 # Xử lý nghiệp vụ/tài khoản
│   ├── schemas/schema.sql        # SQL khởi tạo cấu trúc database
│   ├── database.py               # Kết nối PostgreSQL
│   ├── firebase_config.py        # Firebase Admin SDK
│   ├── main.py                   # Điểm khởi chạy FastAPI
│   ├── requirements.txt
│   └── .env.example
│
├── docs/
│   ├── api.md                    # API contract
│   └── database.md               # Tài liệu thiết kế database
│
└── README.md
```

## 6. Database

PostgreSQL là database dùng chung cho cả Android và Web Admin. File khởi tạo schema hiện có các bảng:

| Bảng | Vai trò |
|---|---|
| `users` | Tài khoản User và Admin |
| `rooms` | Thông tin phòng cho thuê |
| `room_images` | Hình ảnh phòng |
| `favorites` | Danh sách phòng yêu thích |
| `viewing_appointments` | Lịch hẹn xem phòng |
| `rental_requests` | Yêu cầu thuê phòng |
| `rental_contracts` | Hợp đồng thuê |
| `payments` | Bản ghi thanh toán |
| `meter_readings` | Chỉ số đồng hồ điện/nước |
| `maintenance_requests` | Yêu cầu sửa chữa/bảo trì |
| `move_out_requests` | Yêu cầu trả phòng |
| `payment_accounts` | Thông tin tài khoản nhận thanh toán |
| `activity_logs` | Nhật ký hoạt động |

Nguồn schema để khởi tạo database là `backend/schemas/schema.sql`. Khi thay đổi cấu trúc bảng hoặc nghiệp vụ, cần cập nhật cả schema và tài liệu liên quan.

## 7. Quy trình nghiệp vụ thuê phòng

1. User đăng nhập và xem các phòng đang `AVAILABLE`.
2. User gửi yêu cầu thuê phòng; Backend tạo `rental_requests` với trạng thái `PENDING`.
3. Admin xem yêu cầu trên Web Admin và chuyển trạng thái thành `APPROVED` hoặc `REJECTED`.
4. Với yêu cầu được duyệt, Admin tạo hợp đồng và thống nhất ngày bắt đầu, ngày kết thúc, giá thuê, tiền cọc.
5. Backend tạo hợp đồng và cập nhật phòng thành `RENTED` trong cùng một quy trình giao dịch.
6. Android hiển thị hợp đồng và phòng đang thuê của User.
7. Trong thời gian thuê, User có thể gửi yêu cầu sửa chữa hoặc yêu cầu trả phòng theo điều kiện hợp đồng.
8. Admin xử lý yêu cầu. Khi trả phòng được hoàn tất, hợp đồng kết thúc và trạng thái phòng được cập nhật theo nghiệp vụ.

## 8. Cài đặt và chạy dự án

### 8.1. Cài đặt tự động cho Backend và Web Admin

Cài sẵn Python 3.10 trở lên, Node.js 20 trở lên, npm và PostgreSQL. Mở terminal tại thư mục gốc repository rồi chạy đúng lệnh theo hệ điều hành:

| Hệ điều hành | Lệnh |
|---|---|
| Windows Command Prompt | `setup.bat` |
| Windows PowerShell | `Set-ExecutionPolicy -Scope Process Bypass` rồi `./setup.ps1` |
| macOS | `bash ./setup.sh` |
| Linux | `bash ./setup.sh` |

Bộ cài sẽ tạo `backend/.venv`, cài các package Python trong `backend/requirements.txt`, cài package npm trong `web-admin/` và tạo `backend/.env` cùng `web-admin/.env` từ file mẫu nếu các file này chưa tồn tại. File cấu hình có sẵn sẽ được giữ nguyên.

Sau khi cài đặt, cần tự điền thông tin PostgreSQL, Firebase và Cloudinary trong hai file `.env`. Bộ cài không tự tạo tài khoản dịch vụ, database PostgreSQL hoặc Firebase project.

Tạo database PostgreSQL rồi chạy schema:

```bash
psql -U <postgres_user> -d <database_name> -v ON_ERROR_STOP=1 -f backend/schemas/schema.sql
```

Chạy Backend:

- Windows PowerShell: `cd backend; .venv\\Scripts\\python.exe -m uvicorn main:app --reload --host 0.0.0.0 --port 8000`
- macOS/Linux: `cd backend && .venv/bin/python -m uvicorn main:app --reload --host 0.0.0.0 --port 8000`

Chạy Web Admin trong terminal khác:

```bash
cd web-admin
npm run dev
```

Mở URL Vite hiển thị trong terminal, mặc định thường là `http://localhost:5173`.

### 8.2. Yêu cầu môi trường

### 8.1. Yêu cầu môi trường

- Git
- Python 3.11+ (nên dùng phiên bản tương thích với các package trong `backend/requirements.txt`)
- PostgreSQL
- Node.js và npm
- Android Studio, Android SDK và JDK tương thích với Android Gradle Plugin trong dự án
- Firebase project có bật phương thức đăng nhập mà ứng dụng sử dụng
- Cloudinary account để upload ảnh

### 8.3. Backend

Mở terminal tại thư mục repository:

```bash
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

Trên Windows, kích hoạt môi trường ảo bằng:

```powershell
.venv\Scripts\activate
```

Tạo database PostgreSQL, sau đó cấu hình:

1. Sao chép `backend/.env.example` thành `backend/.env`.
2. Cập nhật `DATABASE_URL` theo PostgreSQL local.
3. Điền `FIREBASE_PROJECT_ID` và đường dẫn Firebase Service Account JSON.
4. Điền `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY` và `CLOUDINARY_API_SECRET`.
5. Khởi tạo schema:

```bash
psql -U <postgres_user> -d <database_name> -f schemas/schema.sql
```

Khởi chạy Backend khi đang ở thư mục `backend/`:

```bash
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

Địa chỉ kiểm tra:

- Health check: `http://localhost:8000/health`
- API documentation: `http://localhost:8000/docs`
- API base URL: `http://localhost:8000/api/v1`

### 8.4. Web Admin

```bash
cd web-admin
cp .env.example .env
npm install
npm run dev
```

Cập nhật các biến trong `web-admin/.env` theo Firebase Web App và Backend đang chạy:

- `VITE_API_BASE_URL`
- `VITE_FIREBASE_API_KEY`
- `VITE_FIREBASE_AUTH_DOMAIN`
- `VITE_FIREBASE_PROJECT_ID`
- `VITE_FIREBASE_STORAGE_BUCKET`
- `VITE_FIREBASE_MESSAGING_SENDER_ID`
- `VITE_FIREBASE_APP_ID`

Web Admin mặc định chạy tại địa chỉ Vite hiển thị trong terminal (thường là `http://localhost:5173`).

Build production:

```bash
npm run build
```

### 8.5. Android User

1. Mở Android Studio.
2. Chọn **Open** và trỏ đến thư mục `android-user/`.
3. Chờ Gradle Sync hoàn tất.
4. Kiểm tra cấu hình Firebase Android và thông tin Backend URL trong `android-user/app/build.gradle.kts`.
5. Chạy ứng dụng trên Emulator hoặc thiết bị Android thật.

Backend URL được cấu hình trong `build.gradle.kts`:

- Android Emulator tiêu chuẩn: `http://10.0.2.2:8000/api/v1/`
- Điện thoại thật: thay bằng địa chỉ LAN của máy đang chạy Backend, ví dụ `http://192.168.1.5:8000/api/v1/`. Điện thoại và máy tính cần cùng mạng LAN.

Build/cài đặt bằng terminal:

```bash
cd android-user
./gradlew installDebug
```

## 9. Tài liệu API và quy ước dữ liệu

Chi tiết endpoint, request/response và quy tắc phân quyền được mô tả trong `docs/api.md`.

Các quy ước chung:

- API prefix: `/api/v1`.
- Dữ liệu trao đổi: JSON qua HTTP/HTTPS.
- Tiền tệ: số nguyên VND.
- Ngày: `YYYY-MM-DD`.
- Timestamp: ISO 8601.
- Pagination: `page` bắt đầu từ 1; mặc định `limit=20`.
- Tài khoản mới mặc định có role `USER`; chỉ quy trình đáng tin cậy mới cấp role `ADMIN`.

## 10. Git workflow và phối hợp nhóm

Các nhánh đang được sử dụng trong quá trình phát triển có thể gồm:

- `user`: Android User và các thay đổi tích hợp liên quan.
- `web-admin` hoặc nhánh Web Admin tương ứng: giao diện quản trị.
- `master`: nhánh chính/ổn định của repository.

Tên nhánh thực tế có thể thay đổi theo tiến độ. Trước khi làm việc, cần kiểm tra nhánh hiện tại và cập nhật code mới nhất.

Quy ước phối hợp:

- Android và Web Admin phải tuân theo API contract trong `docs/api.md`.
- Không tự ý thay đổi endpoint, tên field, kiểu dữ liệu hoặc trạng thái nghiệp vụ mà không thông báo cho thành viên còn lại.
- Khi sửa database, cập nhật `backend/schemas/schema.sql` và tài liệu database.
- Backend là phần dùng chung; thay đổi API cần được kiểm tra trên cả hai client.
- Dùng commit message mô tả rõ nội dung thay đổi; ưu tiên Pull Request khi tích hợp nhánh.

## 11. Tài khoản Admin mẫu\n\nTài khoản demo dành cho môi trường local được mô tả tại [docs/admin-account.md](docs/admin-account.md). Tài liệu gồm thông tin đăng nhập mẫu, cách tạo tài khoản trong Firebase Authentication và cách cấp role `ADMIN` trong PostgreSQL bằng `backend/schemas/seed_admin.sql`. Không sử dụng thông tin demo trên production.\n\n## 12. Bảo mật

- Không commit `.env`, Firebase Service Account JSON, mật khẩu database hoặc Cloudinary API Secret.
- Không đưa thông tin bí mật vào README, issue hoặc commit.
- Cấu hình Firebase và Cloudinary bằng thông tin của môi trường triển khai tương ứng.
- Không lưu mật khẩu người dùng dạng plaintext.
- Backend phải xác minh Firebase ID Token và kiểm tra role/status từ PostgreSQL.
- Không để client truy cập PostgreSQL trực tiếp.
- Khi phát hành production, sử dụng HTTPS và cấu hình CORS theo domain thực tế.

## 13. Trạng thái và phạm vi

Repository hiện chứa mã nguồn cho ba thành phần Android User, Web Admin và FastAPI Backend, cùng tài liệu API và database. Các chức năng được liệt kê ở trên phản ánh những module/luồng đã có trong source code; mức độ hoàn thiện và khả năng chạy thực tế còn phụ thuộc cấu hình Firebase, Cloudinary, PostgreSQL, biến môi trường và kiểm thử tích hợp ở từng máy.

Một số dữ liệu như lịch hẹn xem phòng, chỉ số điện/nước và thông tin tài khoản thanh toán đã có cấu trúc trong database nhưng chưa có đầy đủ API/luồng giao diện tương ứng; không nên xem đó là bằng chứng rằng toàn bộ quy trình đã được triển khai hoàn chỉnh.

---

**Repository:** [MyNgoc19/Android_App](https://github.com/MyNgoc19/Android_App)

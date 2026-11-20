# N&N Home — Trang quản trị

trang quản trị là giao diện quản trị của hệ thống quản lý phòng cho thuê N&N Home. Ứng dụng được xây dựng bằng React, TypeScript và Vite, sử dụng chung FastAPI máy chủ Backend với Android User.

## Yêu cầu

- Node.js 20 trở lên và npm
- Python 3.10 trở lên để cài Backend dùng chung
- PostgreSQL
- dự án Firebase đã bật xác thực Firebase

## Cài đặt

Nên chạy bộ cài tại thư mục gốc repository để cài đồng thời Backend và Web Admin:

- Windows Command Prompt: `setup.bat`
- Windows PowerShell: `./setup.ps1`
- macOS/Linux: `bash ./setup.sh`

Bộ cài tạo `web-admin/.env` từ `.env.example` nếu file chưa tồn tại và cài các thư viện cần thiết bằng `npm ci`.

Cấu hình các biến trong `web-admin/.env`:

- `VITE_API_BASE_URL`: URL API FastAPI, ví dụ `http://localhost:8000/api/v1`.
- `VITE_FIREBASE_API_KEY`
- `VITE_FIREBASE_AUTH_DOMAIN`
- `VITE_FIREBASE_PROJECT_ID`
- `VITE_FIREBASE_STORAGE_BUCKET`
- `VITE_FIREBASE_MESSAGING_SENDER_ID`
- `VITE_FIREBASE_APP_ID`

Các giá trị Firebase phải thuộc cùng Firebase project mà Android User sử dụng. Không commit file `.env`.

## Chạy ứng dụng

Khởi động Backend trong terminal thứ nhất:

```bash
cd backend
.venv/bin/python -m uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

Trên Windows PowerShell:

```powershell
cd backend
.venv\Scripts\python.exe -m uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

Khởi động Web Admin trong terminal thứ hai:

```bash
cd web-admin
npm run dev
```

Mở địa chỉ Vite hiển thị trong terminal, mặc định là `http://localhost:5173`.

## Kiểm tra và đóng gói ứng dụng

```bash
npm run lint
npm run build
npm run preview
```

## Các chức năng quản trị

- Dashboard
- Quản lý phòng và hình ảnh
- Quản lý người dùng và phân quyền
- Quản lý yêu cầu thuê
- Quản lý hợp đồng
- Quản lý thanh toán
- Quản lý yêu cầu sửa chữa
- Quản lý yêu cầu trả phòng

Web Admin không kết nối PostgreSQL trực tiếp. Mọi dữ liệu được truy cập thông qua FastAPI và mã xác thực Firebase.

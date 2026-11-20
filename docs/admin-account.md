# Tài khoản Admin mẫu

## Thông tin đăng nhập dùng cho môi trường phát triển

| Trường | Giá trị |
|---|---|
| Email | `admin.demo@nn-home.test` |
| Mật khẩu | `DemoAdmin@12345` |
| Vai trò | `ADMIN` |
| Trạng thái | `ACTIVE` |

Đây là thông tin mẫu công khai dành riêng cho môi trường local/demo. Không dùng tài khoản hoặc mật khẩu này trên production. Hãy đổi mật khẩu sau khi đăng nhập lần đầu nếu tiếp tục sử dụng tài khoản trong môi trường chia sẻ.

## Tạo tài khoản mẫu

Backend dùng Firebase Authentication để xác thực danh tính. Bảng PostgreSQL `users` chỉ lưu Firebase UID và quyền ứng dụng; không lưu mật khẩu. Vì vậy, tài khoản cần được tạo trong Firebase trước khi cấp quyền Admin trong database.

1. Mở Firebase Console và chọn đúng Firebase project đang được cấu hình trong `backend/.env` và `web-admin/.env`.
2. Vào **Authentication → Users → Add user**.
3. Tạo tài khoản với email và mật khẩu mẫu ở trên.
4. Chạy Backend và Web Admin.
5. Đăng nhập Web Admin một lần bằng tài khoản vừa tạo. Backend sẽ xác minh Firebase ID Token và đồng bộ tài khoản vào bảng `users` với role mặc định `USER`.
6. Từ thư mục gốc repository, chạy lệnh dưới đây để cấp quyền Admin.

Linux/macOS:

```bash
psql -U <postgres_user> -d <database_name> -v ON_ERROR_STOP=1 -f backend/schemas/seed_admin.sql
```

Windows PowerShell:

```powershell
psql -U <postgres_user> -d <database_name> -v ON_ERROR_STOP=1 -f backend/schemas/seed_admin.sql
```

7. Đăng xuất rồi đăng nhập lại Web Admin. Tài khoản phải có `role = ADMIN` và `status = ACTIVE`.

File `backend/schemas/seed_admin.sql` chỉ nâng quyền cho email mẫu đã được đồng bộ từ Firebase; file không tạo Firebase user và không lưu mật khẩu.

## Kiểm tra trong PostgreSQL

```sql
SELECT id, firebase_uid, email, full_name, role, status
FROM users
WHERE LOWER(email) = LOWER('admin.demo@nn-home.test');
```

Kết quả mong đợi: một dòng có `role = ADMIN` và `status = ACTIVE`.

## Lưu ý bảo mật

- Không dùng tài khoản mẫu này trên môi trường production hoặc dữ liệu thật.
- Không commit Firebase Service Account JSON, file `.env` hoặc mật khẩu riêng tư.
- Không cấp quyền Admin bằng cách cho phép client tự gửi role. Quyền phải được cập nhật trong PostgreSQL bởi người quản trị database.
- Khi dùng Firebase project dùng chung, kiểm tra email mẫu chưa thuộc tài khoản khác trước khi tạo.
- Nếu email đã tồn tại trong Firebase, không tạo trùng; hãy xác minh chủ sở hữu tài khoản trước khi cấp role Admin.

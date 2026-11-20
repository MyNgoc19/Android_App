# Tài khoản quản trị mẫu

## Thông tin đăng nhập dùng để thử nghiệm

| Thông tin | Giá trị |
|---|---|
| Email | `admin.demo@nn-home.test` |
| Mật khẩu | `DemoAdmin@12345` |
| Quyền | `ADMIN` |
| Trạng thái | `ACTIVE` |

Đây là tài khoản mẫu công khai, chỉ dùng trên máy cá nhân hoặc môi trường thử nghiệm. Không sử dụng tài khoản và mật khẩu này trên môi trường thật. Nếu tiếp tục dùng trong môi trường có nhiều người truy cập, hãy đổi mật khẩu ngay sau lần đăng nhập đầu tiên.

## Tạo tài khoản trên Firebase

Backend sử dụng Firebase Authentication để xác minh danh tính. Bảng PostgreSQL `users` chỉ lưu mã Firebase UID và quyền trong ứng dụng, không lưu mật khẩu. Vì vậy, cần tạo tài khoản trên Firebase trước khi cấp quyền quản trị trong cơ sở dữ liệu.

1. Mở Firebase Console và chọn đúng dự án Firebase được cấu hình trong `backend/.env` và `web-admin/.env`.
2. Mở **Authentication → Users → Add user**.
3. Tạo tài khoản bằng email và mật khẩu mẫu ở trên.
4. Khởi chạy Backend và Web Admin.
5. Đăng nhập Web Admin một lần bằng tài khoản vừa tạo. Backend sẽ xác minh Firebase ID Token và đồng bộ tài khoản vào bảng `users` với quyền mặc định là `USER`.
6. Mở terminal tại thư mục gốc của repository và chạy lệnh dưới đây để cấp quyền quản trị.

Linux/macOS:

```bash
psql -U <postgres_user> -d <database_name> -v ON_ERROR_STOP=1 -f backend/schemas/seed_admin.sql
```

Windows PowerShell:

```powershell
psql -U <postgres_user> -d <database_name> -v ON_ERROR_STOP=1 -f backend/schemas/seed_admin.sql
```

7. Đăng xuất rồi đăng nhập lại Web Admin. Tài khoản cần có `role = ADMIN` và `status = ACTIVE`.

Tệp `backend/schemas/seed_admin.sql` chỉ cấp quyền quản trị cho email mẫu đã được đồng bộ từ Firebase. Tệp này không tạo tài khoản Firebase và không lưu mật khẩu.

## Kiểm tra trong PostgreSQL

```sql
SELECT id, firebase_uid, email, full_name, role, status
FROM users
WHERE LOWER(email) = LOWER('admin.demo@nn-home.test');
```

Kết quả mong đợi: có một dòng dữ liệu với `role = ADMIN` và `status = ACTIVE`.

## Lưu ý bảo mật

- Không dùng tài khoản mẫu trên môi trường thật hoặc với dữ liệu thật.
- Không đưa tệp Firebase Service Account JSON, tệp `.env` hoặc mật khẩu riêng tư lên Git.
- Không cho phép ứng dụng phía người dùng tự gửi quyền quản trị. Quyền phải được cập nhật trong PostgreSQL bởi người có quyền quản trị cơ sở dữ liệu.
- Nếu dùng chung một dự án Firebase, hãy kiểm tra email mẫu chưa được người khác sử dụng trước khi tạo.
- Nếu email đã tồn tại trên Firebase, không tạo tài khoản trùng. Hãy xác minh chủ sở hữu trước khi cấp quyền quản trị.

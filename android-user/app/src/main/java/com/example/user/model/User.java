package com.example.user.model;

/**
 * Tài khoản (bảng users). Lấy từ GET auth/me, users/me.
 * JSON snake_case (monthly_price) được Gson tự map sang camelCase (monthlyPrice).
 */
public class User {

    private int id;
    private String firebaseUid;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private String gender;
    private String role;
    private String status;
    private String createdAt;

    public int getId() { return id; }
    public String getFirebaseUid() { return firebaseUid; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getGender() { return gender; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
}

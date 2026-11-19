
package com.example.user.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.user.R;
import com.example.user.model.User;
import com.example.user.network.ApiCallback;
import com.example.user.repository.AuthRepository;
import com.example.user.utils.BottomNavHelper;
import com.example.user.utils.SessionManager;
import com.example.user.appointment.MyAppointmentsActivity;
import com.example.user.invoice.InvoiceListActivity;
import com.example.user.rental.ContractActivity;
import com.example.user.rental.MyRoomActivity;
import com.example.user.room.FavoriteActivity;
import com.example.user.room.MaintenanceActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    // Thông tin người dùng
    private ImageView imgAvatar;
    private TextView txtProfileName;
    private TextView txtProfileEmail;
    private TextView txtProfileRole;

    // Các mục chức năng
    private android.view.View btnMyRoom;
    private android.view.View btnContract;
    private android.view.View btnInvoices;
    private android.view.View btnAppointments;
    private android.view.View btnFavorites;
    private android.view.View btnMaintenance;
    private android.view.View btnEditProfile;

    // Đăng xuất
    private MaterialButton btnLogout;

    // Thanh điều hướng
    private BottomNavigationView bottomNavigation;

    private final AuthRepository authRepository = new AuthRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupEvents();
        setupBottomNavigation();
    }

    // Ánh xạ các thành phần giao diện
    private void initViews() {

        imgAvatar = findViewById(R.id.imgAvatar);
        txtProfileName = findViewById(R.id.txtProfileName);
        txtProfileEmail = findViewById(R.id.txtProfileEmail);
        txtProfileRole = findViewById(R.id.txtProfileRole);

        btnMyRoom = findViewById(R.id.btnMyRoom);
        btnContract = findViewById(R.id.btnContract);
        btnInvoices = findViewById(R.id.btnInvoices);

        btnAppointments = findViewById(R.id.btnAppointments);
        btnFavorites = findViewById(R.id.btnFavorites);
        btnMaintenance = findViewById(R.id.btnMaintenance);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);

        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    // Hiển thị thông tin người dùng: hiện ngay từ phiên, rồi cập nhật từ API
    private void setupUserInfo() {

        SessionManager session = SessionManager.getInstance();
        bindUser(session.getFullName(), session.getEmail());
        txtProfileRole.setText("Khách thuê");

        imgAvatar.setImageResource(R.drawable.ic_user);
        String avatarUri = getSharedPreferences("NN_HOME_PROFILE", MODE_PRIVATE)
                .getString("avatar_uri", "");
        if (!avatarUri.isEmpty()) {
            try {
                imgAvatar.setImageURI(android.net.Uri.parse(avatarUri));
                imgAvatar.setImageTintList(null);
            } catch (Exception ignored) {
                imgAvatar.setImageResource(R.drawable.ic_user);
            }
        }

        authRepository.getProfile(new ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                bindUser(user.getFullName(), user.getEmail());
                // Ảnh đại diện trên server (avatar_url) nếu có
                if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    imgAvatar.setImageTintList(null);
                    com.example.user.utils.ImageLoader.load(imgAvatar, user.getAvatarUrl());
                }
            }

            @Override
            public void onError(String message) {
                // giữ thông tin đã lưu trên máy
            }
        });
    }

    private void bindUser(String fullName, String email) {
        txtProfileName.setText(fullName);
        txtProfileEmail.setText(email);
    }

    // Xử lý các sự kiện
    private void setupEvents() {

        // Phòng đang thuê
        btnMyRoom.setOnClickListener(v ->
                openActivity(MyRoomActivity.class)
        );

        // Hợp đồng thuê
        btnContract.setOnClickListener(v ->
                openActivity(ContractActivity.class)
        );

        // Hóa đơn và thanh toán
        btnInvoices.setOnClickListener(v ->
                openActivity(InvoiceListActivity.class)
        );

        // Lịch hẹn xem phòng
        btnAppointments.setOnClickListener(v ->
                openActivity(MyAppointmentsActivity.class)
        );

        // Phòng yêu thích
        btnFavorites.setOnClickListener(v ->
                openActivity(FavoriteActivity.class)
        );

        // Yêu cầu bảo trì
        btnMaintenance.setOnClickListener(v ->
                openActivity(MaintenanceActivity.class)
        );

        // Chỉnh sửa thông tin cá nhân
        btnEditProfile.setOnClickListener(v ->
                openActivity(EditProfileActivity.class)
        );

        // Đăng xuất
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    // Thiết lập thanh điều hướng
    private void setupBottomNavigation() {
        BottomNavHelper.setup(this, bottomNavigation, R.id.navProfile);
    }

    // Hiển thị hộp thoại xác nhận đăng xuất
    private void showLogoutDialog() {

        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi N&N Home?")
                .setNegativeButton("Hủy", (dialog, which) ->
                        dialog.dismiss()
                )
                .setPositiveButton("Đăng xuất", (dialog, which) ->
                        logout()
                )
                .show();
    }

    // Xử lý đăng xuất
    private void logout() {

        // Xóa token trên máy + báo backend hủy phiên
        authRepository.logout();

        Intent intent = new Intent(
                ProfileActivity.this,
                LoginActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    // Chuyển đến các màn hình chức năng
    private void openActivity(Class<?> activityClass) {

        Intent intent = new Intent(
                ProfileActivity.this,
                activityClass
        );

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavHelper.refresh(bottomNavigation, R.id.navProfile);
        // Cập nhật lại sau khi sửa hồ sơ
        setupUserInfo();
    }
}

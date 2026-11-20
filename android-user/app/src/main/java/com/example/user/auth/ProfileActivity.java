
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


    private ImageView imgAvatar;
    private TextView txtProfileName;
    private TextView txtProfileEmail;
    private TextView txtProfileRole;


    private android.view.View btnMyRoom;
    private android.view.View btnContract;
    private android.view.View btnInvoices;
    private android.view.View btnAppointments;
    private android.view.View btnFavorites;
    private android.view.View btnMaintenance;
    private android.view.View btnEditProfile;


    private MaterialButton btnLogout;


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

                if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    imgAvatar.setImageTintList(null);
                    com.example.user.utils.ImageLoader.load(imgAvatar, user.getAvatarUrl());
                }
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private void bindUser(String fullName, String email) {
        txtProfileName.setText(fullName);
        txtProfileEmail.setText(email);
    }


    private void setupEvents() {


        btnMyRoom.setOnClickListener(v ->
                openActivity(MyRoomActivity.class)
        );


        btnContract.setOnClickListener(v ->
                openActivity(ContractActivity.class)
        );


        btnInvoices.setOnClickListener(v ->
                openActivity(InvoiceListActivity.class)
        );


        btnAppointments.setOnClickListener(v ->
                openActivity(MyAppointmentsActivity.class)
        );


        btnFavorites.setOnClickListener(v ->
                openActivity(FavoriteActivity.class)
        );


        btnMaintenance.setOnClickListener(v ->
                openActivity(MaintenanceActivity.class)
        );


        btnEditProfile.setOnClickListener(v ->
                openActivity(EditProfileActivity.class)
        );


        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }


    private void setupBottomNavigation() {
        BottomNavHelper.setup(this, bottomNavigation, R.id.navProfile);
    }


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


    private void logout() {


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

        setupUserInfo();
    }
}

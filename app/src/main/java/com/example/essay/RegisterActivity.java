package com.example.essay;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase
        auth = FirebaseAuth.getInstance();

        // Ánh xạ View
        ImageButton btnBack = findViewById(R.id.btnBack);
        TextInputEditText edtFullName = findViewById(R.id.edtFullName);
        TextInputEditText edtEmail = findViewById(R.id.edtEmail);
        TextInputEditText edtPassword = findViewById(R.id.edtPassword);
        TextInputEditText edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        MaterialButton btnRegister = findViewById(R.id.btnRegister);
        TextView txtLogin = findViewById(R.id.txtLogin);

        // Quay lại
        btnBack.setOnClickListener(v -> finish());

        // Sang trang đăng nhập
        txtLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Đăng ký
        btnRegister.setOnClickListener(v -> {

            String fullName = edtFullName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString();
            String confirmPassword = edtConfirmPassword.getText().toString();

            // Kiểm tra dữ liệu
            if (fullName.isEmpty()) {
                edtFullName.setError("Vui lòng nhập họ tên");
                return;
            }

            if (email.isEmpty()) {
                edtEmail.setError("Vui lòng nhập email");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.setError("Email không hợp lệ");
                return;
            }

            if (password.length() < 6) {
                edtPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                return;
            }

            if (!password.equals(confirmPassword)) {
                edtConfirmPassword.setError("Mật khẩu không khớp");
                return;
            }

            // Tạo tài khoản Firebase
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {

                                // Lưu họ tên vào Firebase
                                UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setDisplayName(fullName).build();
                                user.updateProfile(profile).addOnCompleteListener(profileTask -> {

                                            Toast.makeText(
                                                    this,
                                                    "Tạo tài khoản thành công",
                                                    Toast.LENGTH_SHORT
                                            ).show();

                                            // Sang Home
                                            startActivity(
                                                    new Intent(
                                                            this,
                                                            HomeActivity.class
                                                    )
                                            );
                                            finishAffinity();
                                        });
                            }

                        } else {
                            Toast.makeText(
                                    this,
                                    "Đăng ký thất bại",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });
    }
}
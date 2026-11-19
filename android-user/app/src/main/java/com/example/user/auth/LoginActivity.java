package com.example.user.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.user.R;
import com.example.user.model.User;
import com.example.user.network.ApiCallback;
import com.example.user.repository.AuthRepository;
import com.example.user.room.HomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPassword;
    private TextInputLayout layoutEmail, layoutPassword;
    private MaterialButton btnLogin, btnGoogle;
    private TextView txtRegister, txtForgotPassword;

    private final AuthRepository authRepository = new AuthRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupEvents();
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        layoutEmail = findViewById(R.id.layoutEmail);
        layoutPassword = findViewById(R.id.layoutPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);

        txtRegister = findViewById(R.id.txtRegister);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
    }

    private void setupEvents() {

        btnLogin.setOnClickListener(v -> login());

        txtRegister.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    RegisterActivity.class
            );
            startActivity(intent);
        });

        // Quên mật khẩu: Firebase gửi email đặt lại mật khẩu
        txtForgotPassword.setOnClickListener(v -> resetPassword());

        btnGoogle.setOnClickListener(v -> loginWithGoogle());

        findViewById(R.id.btnBack)
                .setOnClickListener(v -> finish());
    }

    private void login() {

        String email = edtEmail.getText() == null
                ? ""
                : edtEmail.getText().toString().trim();

        String password = edtPassword.getText() == null
                ? ""
                : edtPassword.getText().toString();

        layoutEmail.setError(null);
        layoutPassword.setError(null);

        if (TextUtils.isEmpty(email)) {
            layoutEmail.setError("Vui lòng nhập Email");
            edtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            layoutPassword.setError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();
            return;
        }

        // Firebase đăng nhập -> POST auth/session -> GET auth/me
        setLoading(true);
        authRepository.login(email, password, new ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                goHome();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                layoutPassword.setError(message);
            }
        });
    }

    // Chọn tài khoản Google -> Firebase -> POST auth/session -> GET auth/me
    private void loginWithGoogle() {
        layoutPassword.setError(null);
        setGoogleLoading(true);
        GoogleSignInHelper.signIn(this, new GoogleSignInHelper.Listener() {
            @Override
            public void onIdToken(String idToken) {
                authRepository.signInWithGoogle(idToken, new ApiCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        setGoogleLoading(false);
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công",
                                Toast.LENGTH_SHORT).show();
                        goHome();
                    }

                    @Override
                    public void onError(String message) {
                        setGoogleLoading(false);
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancelled() {
                setGoogleLoading(false);
            }

            @Override
            public void onError(String message) {
                setGoogleLoading(false);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setGoogleLoading(boolean loading) {
        btnGoogle.setEnabled(!loading);
        btnLogin.setEnabled(!loading);
        btnGoogle.setText(loading ? "Đang đăng nhập..." : "Đăng nhập bằng Google");
    }

    private void resetPassword() {
        String email = edtEmail.getText() == null ? "" : edtEmail.getText().toString().trim();
        layoutEmail.setError(null);
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError("Nhập email của bạn để nhận link đặt lại mật khẩu");
            edtEmail.requestFocus();
            return;
        }
        authRepository.sendPasswordReset(email, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(LoginActivity.this,
                        "Đã gửi email đặt lại mật khẩu tới " + email, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String message) {
                layoutEmail.setError(message);
            }
        });
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Đang đăng nhập..." : "Đăng nhập");
    }

    // Vào trang chủ và xóa các màn hình đăng nhập khỏi back stack
    private void goHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

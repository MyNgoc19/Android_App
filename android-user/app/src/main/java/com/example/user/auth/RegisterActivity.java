
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

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edtFullName, edtEmail;
    private TextInputEditText edtPassword, edtConfirmPassword;

    private TextInputLayout layoutFullName, layoutEmail;
    private TextInputLayout layoutPassword, layoutConfirmPassword;

    private MaterialButton btnRegister, btnGoogle;
    private TextView txtLogin;

    private final AuthRepository authRepository = new AuthRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupEvents();
    }

    private void initViews() {

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        layoutFullName = findViewById(R.id.layoutFullName);
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutPassword = findViewById(R.id.layoutPassword);
        layoutConfirmPassword = findViewById(R.id.layoutConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        btnGoogle = findViewById(R.id.btnGoogle);
        txtLogin = findViewById(R.id.txtLogin);
    }

    private void setupEvents() {


        btnRegister.setOnClickListener(v -> register());


        txtLogin.setOnClickListener(v -> {
            Intent intent = new Intent(
                    RegisterActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);
            finish();
        });


        btnGoogle.setOnClickListener(v -> registerWithGoogle());


        findViewById(R.id.btnBack)
                .setOnClickListener(v -> finish());
    }

    private void register() {

        String fullName = getText(edtFullName);
        String email = getText(edtEmail);
        String password = getText(edtPassword);
        String confirmPassword = getText(edtConfirmPassword);


        layoutFullName.setError(null);
        layoutEmail.setError(null);
        layoutPassword.setError(null);
        layoutConfirmPassword.setError(null);


        if (TextUtils.isEmpty(fullName)) {
            layoutFullName.setError("Vui lòng nhập họ tên");
            edtFullName.requestFocus();
            return;
        }


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

        if (password.length() < 6) {
            layoutPassword.setError(
                    "Mật khẩu phải có ít nhất 6 ký tự"
            );
            edtPassword.requestFocus();
            return;
        }


        if (TextUtils.isEmpty(confirmPassword)) {
            layoutConfirmPassword.setError(
                    "Vui lòng xác nhận mật khẩu"
            );
            edtConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            layoutConfirmPassword.setError(
                    "Mật khẩu xác nhận không khớp"
            );
            edtConfirmPassword.requestFocus();
            return;
        }


        btnRegister.setEnabled(false);
        btnRegister.setText("Đang tạo tài khoản...");


        authRepository.register(fullName, email, password, new ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                Toast.makeText(RegisterActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Tạo tài khoản");
                if (message.contains("Email")) {
                    layoutEmail.setError(message);
                } else {
                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void registerWithGoogle() {
        setGoogleLoading(true);
        GoogleSignInHelper.signIn(this, new GoogleSignInHelper.Listener() {
            @Override
            public void onIdToken(String idToken) {
                authRepository.signInWithGoogle(idToken, new ApiCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        Toast.makeText(RegisterActivity.this, "Đăng nhập thành công",
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        setGoogleLoading(false);
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
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
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setGoogleLoading(boolean loading) {
        btnGoogle.setEnabled(!loading);
        btnRegister.setEnabled(!loading);
        btnGoogle.setText(loading ? "Đang xử lý..." : "Đăng ký bằng Google");
    }

    private String getText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }

        return editText.getText().toString().trim();
    }
}

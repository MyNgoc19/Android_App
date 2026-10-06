package com.example.essay;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);

        TextInputEditText edtEmail = findViewById(R.id.edtEmail);
        TextInputEditText edtPassword = findViewById(R.id.edtPassword);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        MaterialButton btnGoogle = findViewById(R.id.btnGoogle);

        // Đăng nhập Email/Password
        btnLogin.setOnClickListener(v -> {

            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString();

            if (email.isEmpty()) {
                edtEmail.setError("Vui lòng nhập email");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.setError("Email không hợp lệ");
                return;
            }

            if (password.isEmpty()) {
                edtPassword.setError("Vui lòng nhập mật khẩu");
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            openHome();
                        } else {
                            Toast.makeText(
                                    this,
                                    "Đăng nhập thất bại",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });

        // Đăng nhập Google
        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        // Quay lại
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Sang đăng ký
        findViewById(R.id.txtRegister).setOnClickListener(v -> {
                    startActivity(new Intent(this, RegisterActivity.class));
                });
    }


    // Mở danh sách tài khoản Google
    private void signInWithGoogle() {
        GetGoogleIdOption googleOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id)).build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleOption).build();

        credentialManager.getCredentialAsync(this, request, null,
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<
                        GetCredentialResponse,
                        GetCredentialException>() {

                    @Override
                    public void onResult(GetCredentialResponse result) {
                        runOnUiThread(() -> handleGoogleResult(result));
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        runOnUiThread(() ->
                                Toast.makeText(
                                        LoginActivity.this,
                                        "Đăng nhập Google thất bại",
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
                    }
                }
        );
    }


    // Lấy Google ID Token
    private void handleGoogleResult(GetCredentialResponse result) {
        Credential credential = result.getCredential();
        if (credential instanceof CustomCredential) {
            CustomCredential custom = (CustomCredential) credential;
            try {
                GoogleIdTokenCredential googleCredential = GoogleIdTokenCredential.createFrom(custom.getData());
                String idToken = googleCredential.getIdToken();
                loginFirebaseWithGoogle(idToken);
            } catch (Exception e) {
                Toast.makeText(
                        this,
                        "Đăng nhập Google thất bại",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }


    // Đăng nhập Firebase bằng Google
    private void loginFirebaseWithGoogle(String idToken) {
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).
                addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                openHome();
            } else {
                Toast.makeText(
                        this,
                        "Đăng nhập Google thất bại",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }


    // Chuyển sang Home
    private void openHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finishAffinity();
    }
}
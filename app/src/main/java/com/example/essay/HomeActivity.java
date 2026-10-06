package com.example.essay;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.essay.api.ApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private TextView txtUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        txtUserName = findViewById(R.id.txtUserName);

        // Lấy người dùng đang đăng nhập
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {

            // Hiển thị tên
            String name = user.getDisplayName();
            if (name != null) {
                txtUserName.setText("Xin chào, " + name);
            } else {
                txtUserName.setText(
                        "Xin chào, " + user.getEmail()
                );
            }

            // Lấy JWT từ Firebase
            user.getIdToken(false).addOnSuccessListener(result -> {
                        String token = result.getToken();
                        if (token != null) {
                            Log.d(
                                    "JWT_RESULT",
                                    "Đã lấy Firebase ID Token"
                            );
                            verifyJWT(token);
                        }
                    });
        }
    }


    // Gửi JWT sang FastAPI
    private void verifyJWT(String token) {
        Log.d(
                "JWT_RESULT",
                "Bắt đầu gửi JWT sang FastAPI"
        );

        ApiClient.getApi()
                .verifyFirebaseToken("Bearer " + token)
                .enqueue(new Callback<Map<String, Object>>() {

                    @Override
                    public void onResponse(
                            Call<Map<String, Object>> call,
                            Response<Map<String, Object>> response
                    ) {

                        if (response.isSuccessful()) {

                            Log.d(
                                    "JWT_RESULT",
                                    "FastAPI trả về: " + response.body()
                            );
                            Toast.makeText(
                                    HomeActivity.this,
                                    "Đăng nhập thành công",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(
                                HomeActivity.this,
                                "Không kết nối được FastAPI",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}
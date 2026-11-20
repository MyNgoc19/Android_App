package com.example.user;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.user.auth.LoginActivity;
import com.example.user.auth.RegisterActivity;
import com.example.user.room.HomeActivity;
import com.example.user.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCAL_NETWORK = 1001;

    private Button btnLogin;
    private Button btnRegister;
    private Class<?> pendingActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> openActivity(LoginActivity.class));
        btnRegister.setOnClickListener(v -> openActivity(RegisterActivity.class));

        if (SessionManager.getInstance().isLoggedIn()) {
            openActivity(HomeActivity.class);
        } else {
            requestLocalNetworkPermission(null);
        }
    }

    private void openActivity(Class<?> activityClass) {
        if (needsLocalNetworkPermission()
                && checkSelfPermission("android.permission.ACCESS_LOCAL_NETWORK")
                != PackageManager.PERMISSION_GRANTED) {
            requestLocalNetworkPermission(activityClass);
            return;
        }

        startActivity(new Intent(this, activityClass));
        if (activityClass == HomeActivity.class) {
            finish();
        }
    }

    private void requestLocalNetworkPermission(Class<?> activityAfterGrant) {
        if (!needsLocalNetworkPermission()
                || checkSelfPermission("android.permission.ACCESS_LOCAL_NETWORK")
                == PackageManager.PERMISSION_GRANTED) {
            if (activityAfterGrant != null) {
                openActivity(activityAfterGrant);
            }
            return;
        }

        pendingActivity = activityAfterGrant;
        requestPermissions(
                new String[]{"android.permission.ACCESS_LOCAL_NETWORK"},
                REQUEST_LOCAL_NETWORK
        );
    }

    private boolean needsLocalNetworkPermission() {
        return Build.VERSION.SDK_INT >= 37;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_LOCAL_NETWORK) {
            return;
        }

        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Class<?> destination = pendingActivity;
            pendingActivity = null;

            if (destination != null) {
                openActivity(destination);
            }
            return;
        }

        pendingActivity = null;
        Toast.makeText(
                this,
                "Local network access is required to connect to the backend.",
                Toast.LENGTH_LONG
        ).show();
    }
}

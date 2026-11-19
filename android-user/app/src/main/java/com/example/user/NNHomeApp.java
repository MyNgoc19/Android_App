package com.example.user;

import android.app.Application;

import com.example.user.network.ApiCaller;
import com.example.user.utils.SessionManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

/**
 * Chạy đầu tiên khi mở app (android:name=".NNHomeApp" trong Manifest).
 *
 * Firebase: nếu đã có file app/google-services.json + plugin google-services
 * thì Firebase tự khởi tạo. Nếu chưa, app khởi tạo bằng 3 giá trị FIREBASE_*
 * khai báo trong app/build.gradle.kts (lấy cùng project Firebase với Web Admin).
 */
public class NNHomeApp extends Application {

    /** false = chưa điền FIREBASE_* trong build.gradle.kts và chưa có google-services.json */
    public static boolean firebaseConfigured = true;

    @Override
    public void onCreate() {
        super.onCreate();

        if (FirebaseApp.getApps(this).isEmpty()) {
            firebaseConfigured = !BuildConfig.FIREBASE_API_KEY.startsWith("DIEN_");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApiKey(BuildConfig.FIREBASE_API_KEY)
                    .setApplicationId(BuildConfig.FIREBASE_APP_ID)
                    .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                    .build();
            FirebaseApp.initializeApp(this, options);
        }

        SessionManager.init(this);
        ApiCaller.init(this);
    }
}

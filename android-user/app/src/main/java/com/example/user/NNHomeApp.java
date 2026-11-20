package com.example.user;

import android.app.Application;

import com.example.user.network.ApiCaller;
import com.example.user.utils.SessionManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;








public class NNHomeApp extends Application {


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

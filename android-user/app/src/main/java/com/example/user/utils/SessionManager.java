package com.example.user.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.user.model.User;
import com.google.firebase.auth.FirebaseAuth;





public class SessionManager {

    private static final String PREF_NAME = "NN_HOME_SESSION";

    private static SessionManager instance;
    private final SharedPreferences prefs;

    private SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Chưa gọi SessionManager.init() trong NNHomeApp");
        }
        return instance;
    }

    public void saveUser(User user) {
        if (user == null) {
            return;
        }
        prefs.edit()
                .putInt("user_id", user.getId())
                .putString("full_name", user.getFullName())
                .putString("email", user.getEmail())
                .putString("phone", user.getPhone())
                .putString("avatar_url", user.getAvatarUrl())
                .putString("gender", user.getGender())
                .putString("role", user.getRole())
                .putString("status", user.getStatus())
                .apply();
    }


    public boolean isLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public int getUserId() { return prefs.getInt("user_id", 0); }
    public String getFullName() { return prefs.getString("full_name", ""); }
    public String getEmail() { return prefs.getString("email", ""); }
    public String getPhone() { return prefs.getString("phone", ""); }
    public String getAvatarUrl() { return prefs.getString("avatar_url", ""); }


    public String getGender() { return prefs.getString("gender", ""); }

    public void clear() {
        prefs.edit().clear().apply();
    }
}

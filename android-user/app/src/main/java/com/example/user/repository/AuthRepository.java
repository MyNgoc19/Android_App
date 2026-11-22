package com.example.user.repository;

import android.text.TextUtils;

import com.example.user.model.User;
import com.example.user.network.ApiCallback;
import com.example.user.network.ApiCaller;
import com.example.user.network.ApiService;
import com.example.user.network.RetrofitClient;
import com.example.user.utils.SessionManager;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;










public class AuthRepository {

    private final ApiService api = RetrofitClient.getApi();
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


    public void login(String email, String password, ApiCallback<User> callback) {
        if (notConfigured(callback)) return;
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser == null) {
                        callback.onError("Đăng nhập thất bại");
                        return;
                    }

                    ensureNameThenSync(firebaseUser, null, callback);
                })
                .addOnFailureListener(e -> callback.onError(firebaseError(e)));
    }




    public void signInWithGoogle(String idToken, ApiCallback<User> callback) {
        if (notConfigured(callback)) return;
        firebaseAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser == null) {
                        callback.onError("Đăng nhập Google thất bại");
                        return;
                    }

                    ensureNameThenSync(firebaseUser, null, callback);
                })
                .addOnFailureListener(e -> callback.onError(firebaseError(e)));
    }


    public void register(String fullName, String email, String password, ApiCallback<User> callback) {
        if (notConfigured(callback)) return;
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser == null) {
                        callback.onError("Không tạo được tài khoản");
                        return;
                    }
                    ensureNameThenSync(firebaseUser, fullName, new ApiCallback<User>() {
                        @Override
                        public void onSuccess(User user) {

                            if (!fullName.equals(user.getFullName())) {
                                updateProfile(fullName, null, callback);
                            } else {
                                callback.onSuccess(user);
                            }
                        }

                        @Override
                        public void onError(String message) {
                            callback.onError(message);
                        }
                    });
                })
                .addOnFailureListener(e -> callback.onError(firebaseError(e)));
    }








    private void ensureNameThenSync(FirebaseUser firebaseUser, String wantedName,
                                    ApiCallback<User> callback) {
        String currentName = firebaseUser.getDisplayName();
        String name = !TextUtils.isEmpty(wantedName) ? wantedName
                : !TextUtils.isEmpty(currentName) ? currentName
                  : nameFromEmail(firebaseUser.getEmail());

        if (name.equals(currentName)) {

            refreshTokenThenSync(firebaseUser, callback);
            return;
        }

        UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        firebaseUser.updateProfile(profile)
                .addOnCompleteListener(task -> refreshTokenThenSync(firebaseUser, callback));
    }

    private void refreshTokenThenSync(FirebaseUser firebaseUser, ApiCallback<User> callback) {
        firebaseUser.getIdToken(true)
                .addOnSuccessListener(tokenResult -> syncSession(callback))
                .addOnFailureListener(e -> {
                    firebaseAuth.signOut();
                    callback.onError(firebaseError(e));
                });
    }


    private static String nameFromEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return "Người dùng";
        }
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }





    public void syncSession(ApiCallback<User> callback) {
        ApiCaller.enqueue(api.createSession(), new ApiCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement data) {
                ApiCaller.enqueue(api.getMe(), new ApiCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        if (user == null) {
                            callback.onError("Không đọc được thông tin tài khoản");
                            return;
                        }
                        if ("BLOCKED".equalsIgnoreCase(user.getStatus())) {
                            logout();
                            callback.onError("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
                            return;
                        }
                        SessionManager.getInstance().saveUser(user);
                        callback.onSuccess(user);
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
            }

            @Override
            public void onError(String message) {

                firebaseAuth.signOut();
                callback.onError(message);
            }
        });
    }

    public void sendPasswordReset(String email, ApiCallback<Void> callback) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(firebaseError(e)));
    }

    public void logout() {
        firebaseAuth.signOut();
        SessionManager.getInstance().clear();
    }


    public void getProfile(ApiCallback<User> callback) {
        ApiCaller.enqueue(api.getProfile(), saveUser(callback));
    }


    public void updateProfile(String fullName, String phone, ApiCallback<User> callback) {
        updateProfile(fullName, phone, null, callback);
    }

    public void updateProfile(String fullName, String phone, String gender,
                              ApiCallback<User> callback) {
        updateProfile(fullName, phone, gender, null, callback);
    }

    public void updateProfile(String fullName, String phone, String gender,
                              String avatarUrl, ApiCallback<User> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("full_name", fullName);
        if (phone != null) body.put("phone", phone);
        if (gender != null) body.put("gender", gender);
        if (avatarUrl != null) body.put("avatar_url", avatarUrl);
        ApiCaller.enqueue(api.updateProfile(body), saveUser(callback));

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null && !TextUtils.isEmpty(fullName)) {
            firebaseUser.updateProfile(new UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build());
        }
    }

    private ApiCallback<User> saveUser(ApiCallback<User> callback) {
        return new ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                SessionManager.getInstance().saveUser(user);
                callback.onSuccess(user);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        };
    }

    private static boolean notConfigured(ApiCallback<?> callback) {
        if (!com.example.user.NNHomeApp.firebaseConfigured) {
            callback.onError("Chưa cấu hình Firebase: điền FIREBASE_API_KEY, FIREBASE_APP_ID, "
                    + "FIREBASE_PROJECT_ID trong app/build.gradle.kts");
            return true;
        }
        return false;
    }


    private static String firebaseError(Exception e) {
        if (e instanceof FirebaseAuthWeakPasswordException) {
            return "Mật khẩu quá yếu (tối thiểu 6 ký tự)";
        }
        if (e instanceof FirebaseAuthUserCollisionException) {
            return "Email đã được sử dụng. Hãy dùng Đăng nhập.";
        }
        if (e instanceof FirebaseAuthInvalidUserException) {
            return "Tài khoản không tồn tại hoặc đã bị vô hiệu hóa";
        }
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return "Email hoặc mật khẩu không đúng";
        }
        if (e instanceof FirebaseTooManyRequestsException) {
            return "Thử quá nhiều lần, vui lòng đợi một lát";
        }
        if (e instanceof FirebaseNetworkException) {
            return "Không có kết nối Internet";
        }
        return e.getMessage() != null ? e.getMessage() : "Đăng nhập thất bại";
    }
}

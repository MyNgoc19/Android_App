package com.example.user.auth;

import android.app.Activity;
import android.os.CancellationSignal;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;

import com.example.user.BuildConfig;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

/**
 * Hiện hộp chọn tài khoản Google (Credential Manager) và trả về Google ID Token.
 * ID Token này được đưa cho Firebase (AuthRepository.signInWithGoogle) để đăng nhập.
 *
 * Cần: GOOGLE_WEB_CLIENT_ID trong app/build.gradle.kts, bật Google trong
 * Firebase Authentication, và thêm SHA-1 của máy vào app Android trên Firebase.
 */
public final class GoogleSignInHelper {

    public interface Listener {
        void onIdToken(String idToken);

        /** Người dùng tự đóng hộp chọn tài khoản: không cần báo lỗi */
        void onCancelled();

        void onError(String message);
    }

    private GoogleSignInHelper() {
    }

    public static void signIn(Activity activity, Listener listener) {
        String webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID;
        if (TextUtils.isEmpty(webClientId) || webClientId.startsWith("DIEN_")) {
            listener.onError("Chưa cấu hình GOOGLE_WEB_CLIENT_ID trong app/build.gradle.kts");
            return;
        }

        // Luôn hiện nút/hộp "Đăng nhập bằng Google" để người dùng chọn tài khoản
        GetSignInWithGoogleOption option =
                new GetSignInWithGoogleOption.Builder(webClientId).build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build();

        CredentialManager credentialManager = CredentialManager.create(activity);
        credentialManager.getCredentialAsync(
                activity,
                request,
                new CancellationSignal(),
                ContextCompat.getMainExecutor(activity),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse response) {
                        handleCredential(response.getCredential(), listener);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        if (e instanceof GetCredentialCancellationException) {
                            listener.onCancelled();
                        } else if (e instanceof NoCredentialException) {
                            listener.onError("Máy chưa có tài khoản Google. "
                                    + "Vào Cài đặt > Tài khoản để thêm tài khoản Google.");
                        } else {
                            listener.onError(translate(e));
                        }
                    }
                });
    }

    private static void handleCredential(Credential credential, Listener listener) {
        if (credential instanceof CustomCredential
                && GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                .equals(credential.getType())) {
            try {
                GoogleIdTokenCredential google =
                        GoogleIdTokenCredential.createFrom(credential.getData());
                listener.onIdToken(google.getIdToken());
            } catch (Exception e) {
                listener.onError("Không đọc được thông tin tài khoản Google");
            }
            return;
        }
        listener.onError("Loại đăng nhập không được hỗ trợ");
    }

    private static String translate(GetCredentialException e) {
        String message = e.getMessage() == null ? "" : e.getMessage();
        // 28444 / 10: sai Web client ID hoặc chưa thêm SHA-1 trên Firebase
        if (message.contains("28444") || message.contains("Developer console")
                || message.contains("10:")) {
            return "Cấu hình Google chưa đúng: kiểm tra GOOGLE_WEB_CLIENT_ID "
                    + "và SHA-1 trong Firebase Console";
        }
        if (message.toLowerCase().contains("network")) {
            return "Không có kết nối Internet";
        }
        return message.isEmpty() ? "Đăng nhập Google thất bại" : message;
    }
}

package com.example.user.network;

import com.example.user.BuildConfig;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit dùng chung (singleton).
 * - Tự gắn "Authorization: Bearer <Firebase ID Token>" nếu đã đăng nhập.
 *   getIdToken(false) tự lấy token mới khi token cũ hết hạn.
 * - Nếu backend trả 401 => ép làm mới token (getIdToken(true)) và gửi lại 1 lần.
 * - Gson đổi snake_case <-> camelCase, và chấp nhận danh sách dạng [...] hoặc {"items": [...]}.
 */
public class RetrofitClient {

    private static ApiService apiService;

    // Dùng chung để tự đọc JSON (vd danh sách yêu thích)
    public static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapterFactory(new ListOrItemsAdapterFactory())
            .create();

    private RetrofitClient() {
    }

    public static synchronized ApiService getApi() {
        if (apiService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY); // Logcat: lọc "okhttp"

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        String token = getFirebaseToken(false);
                        Response response = chain.proceed(withToken(original, token));

                        // Token có thể vừa hết hạn => lấy token mới, gửi lại 1 lần
                        if (response.code() == 401 && token != null) {
                            String fresh = getFirebaseToken(true);
                            if (fresh != null && !fresh.equals(token)) {
                                response.close();
                                return chain.proceed(withToken(original, fresh));
                            }
                        }
                        return response;
                    })
                    .addInterceptor(logging)
                    .build();

            apiService = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(GSON))
                    .build()
                    .create(ApiService.class);
        }
        return apiService;
    }

    private static Request withToken(Request request, String token) {
        if (token == null) {
            return request;
        }
        return request.newBuilder().header("Authorization", "Bearer " + token).build();
    }

    /** Chạy trên luồng nền của OkHttp nên được phép chờ (Tasks.await) */
    private static String getFirebaseToken(boolean forceRefresh) throws IOException {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return null;
        }
        try {
            return Tasks.await(user.getIdToken(forceRefresh), 15, TimeUnit.SECONDS).getToken();
        } catch (Exception e) {
            throw new IOException("Không lấy được Firebase token", e);
        }
    }
}

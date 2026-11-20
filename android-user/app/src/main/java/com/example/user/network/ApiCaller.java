package com.example.user.network;

import android.content.Context;
import android.content.Intent;

import com.example.user.auth.LoginActivity;
import com.example.user.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;









public class ApiCaller {

    private static Context appContext;

    private ApiCaller() {
    }

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static <T> void enqueue(Call<T> call, ApiCallback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                    return;
                }
                String message = parseError(response);
                if (response.code() == 401) {
                    handleUnauthorized();
                }
                callback.onError(message);
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                if (call.isCanceled()) {
                    return;
                }
                callback.onError("Không kết nối được máy chủ. Kiểm tra Backend (cổng 8000) đã chạy và BASE_URL đúng chưa.");
            }
        });
    }


    public static String parseError(Response<?> response) {
        String detail = null;
        try {
            if (response.errorBody() != null) {
                JsonElement json = JsonParser.parseString(response.errorBody().string());
                if (json.isJsonObject()) {
                    JsonObject obj = json.getAsJsonObject();
                    JsonElement d = obj.get("detail");
                    if (d != null && d.isJsonPrimitive()) {
                        detail = d.getAsString();
                    } else if (d != null && d.isJsonArray() && d.getAsJsonArray().size() > 0) {
                        JsonElement first = d.getAsJsonArray().get(0);
                        if (first.isJsonObject() && first.getAsJsonObject().has("msg")) {
                            detail = first.getAsJsonObject().get("msg").getAsString();
                        }
                    } else if (obj.has("message")) {
                        detail = obj.get("message").getAsString();
                    }
                }
            }
        } catch (Exception ignored) {

        }

        detail = translate(detail);

        switch (response.code()) {
            case 401:
                return "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại";
            case 403:
                return detail != null ? "Không có quyền: " + detail
                        : "Tài khoản bị khóa hoặc không có quyền thực hiện";
            case 404:
                return detail != null ? detail : "Không tìm thấy dữ liệu";
            default:
                return detail != null ? detail : "Có lỗi xảy ra (mã " + response.code() + ")";
        }
    }


    private static String translate(String detail) {
        if (detail == null) {
            return null;
        }
        java.util.Map<String, String> vi = new java.util.HashMap<>();
        vi.put("You need an active rental contract to send a maintenance request.",
                "Bạn cần có hợp đồng thuê đang hiệu lực để gửi yêu cầu sửa chữa.");
        vi.put("You do not have an active rental contract.", "Bạn chưa có hợp đồng thuê đang hiệu lực.");
        vi.put("You already have a move-out request for this contract.", "Bạn đã gửi yêu cầu trả phòng cho hợp đồng này.");
        vi.put("Move-out date cannot be in the past.", "Ngày trả phòng không được ở quá khứ.");
        vi.put("Move-out date cannot be earlier than the contract start date.", "Ngày trả phòng không được trước ngày bắt đầu hợp đồng.");
        vi.put("Only pending maintenance requests can be cancelled.", "Chỉ hủy được yêu cầu sửa chữa đang chờ.");
        vi.put("Only pending move-out requests can be cancelled.", "Chỉ hủy được yêu cầu trả phòng đang chờ.");
        vi.put("You already have a pending request for this room.", "Bạn đã có một yêu cầu đang chờ với phòng này.");
        vi.put("This room is not currently available.", "Phòng này hiện không còn trống.");
        vi.put("Only available rooms can be added to favorites.", "Chỉ thêm được phòng còn trống vào yêu thích.");
        vi.put("Only pending rental requests can be cancelled.", "Chỉ hủy được yêu cầu thuê đang chờ.");
        vi.put("Room not found.", "Không tìm thấy phòng.");
        vi.put("Your account has been blocked.", "Tài khoản của bạn đã bị khóa.");
        String text = vi.get(detail);
        return text != null ? text : detail;
    }

    private static void handleUnauthorized() {
        FirebaseAuth.getInstance().signOut();
        SessionManager.getInstance().clear();
        if (appContext != null) {
            Intent intent = new Intent(appContext, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            appContext.startActivity(intent);
        }
    }
}

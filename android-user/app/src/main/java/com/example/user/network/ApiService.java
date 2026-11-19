package com.example.user.network;

import com.example.user.model.Contract;
import com.example.user.model.MaintenanceRequest;
import com.example.user.model.MoveOutRequest;
import com.example.user.model.Payment;
import com.example.user.model.RentalRequest;
import com.example.user.model.Room;
import com.example.user.model.User;
import com.google.gson.JsonElement;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * API của Backend FastAPI chung (xem file bàn giao ANDROID – BACKEND INTEGRATION HANDOFF).
 * BASE_URL = http://10.0.2.2:8000/api/v1/
 *
 * - Backend trả JSON trực tiếp (không bọc success/data).
 * - Lỗi trả về {"detail": "..."}.
 * - Danh sách có thể là mảng [...] hoặc {"items": [...], "total": ...}:
 *   RetrofitClient tự xử lý cả 2 dạng nên ở đây khai báo List<...>.
 * - Header Authorization: Bearer <Firebase ID Token> được gắn tự động.
 */
public interface ApiService {

    // ===== Authentication =====
    // Bắt buộc gọi sau khi đăng nhập/đăng ký Firebase để đồng bộ vào bảng users
    @POST("auth/session")
    Call<JsonElement> createSession();

    @GET("auth/me")
    Call<User> getMe();

    // ===== User profile =====
    @GET("users/me")
    Call<User> getProfile();

    // Chỉ gửi full_name, phone, avatar_url (không gửi role/status)
    @PATCH("users/me")
    Call<User> updateProfile(@Body Map<String, Object> body);

    // ===== Rooms (công khai) =====
    // query: keyword, page, limit, status ...
    @GET("rooms")
    Call<List<Room>> getRooms(@QueryMap Map<String, String> query);

    @GET("rooms/{room_id}")
    Call<Room> getRoom(@Path("room_id") int roomId);

    // ===== Favorites =====
    // Dạng phần tử chưa thống nhất (phòng hoặc {room_id, room}) => đọc JsonElement
    @GET("users/me/favorites")
    Call<JsonElement> getFavorites();

    @POST("users/me/favorites/{room_id}")
    Call<JsonElement> addFavorite(@Path("room_id") int roomId);

    @DELETE("users/me/favorites/{room_id}")
    Call<JsonElement> removeFavorite(@Path("room_id") int roomId);

    // ===== Rental requests =====
    // body: message, desired_move_in (yyyy-MM-dd)
    @POST("rooms/{room_id}/rental-requests")
    Call<JsonElement> createRentalRequest(@Path("room_id") int roomId,
                                          @Body Map<String, Object> body);

    @GET("users/me/rental-requests")
    Call<List<RentalRequest>> getRentalRequests();

    @GET("users/me/rental-requests/{request_id}")
    Call<RentalRequest> getRentalRequest(@Path("request_id") int requestId);

    @PATCH("users/me/rental-requests/{request_id}/cancel")
    Call<JsonElement> cancelRentalRequest(@Path("request_id") int requestId);

    // ===== Contracts =====
    @GET("users/me/contracts")
    Call<List<Contract>> getContracts();

    @GET("users/me/contracts/{contract_id}")
    Call<Contract> getContract(@Path("contract_id") int contractId);

    // ===== Payments =====
    @GET("users/me/payments")
    Call<List<Payment>> getPayments();

    // ===== Sửa chữa (routers/maintenance.py) =====
    @GET("users/me/maintenance-requests")
    Call<List<MaintenanceRequest>> getMaintenanceRequests();

    // body: title, description, priority (LOW|MEDIUM|HIGH)
    @POST("users/me/maintenance-requests")
    Call<MaintenanceRequest> createMaintenanceRequest(@Body Map<String, Object> body);

    @PATCH("users/me/maintenance-requests/{request_id}/cancel")
    Call<MaintenanceRequest> cancelMaintenanceRequest(@Path("request_id") int requestId);

    // ===== Trả phòng (routers/move_out.py) =====
    @GET("users/me/move-out-requests")
    Call<List<MoveOutRequest>> getMoveOutRequests();

    // body: move_out_date (yyyy-MM-dd), reason
    @POST("users/me/move-out-requests")
    Call<MoveOutRequest> createMoveOutRequest(@Body Map<String, Object> body);

    @PATCH("users/me/move-out-requests/{request_id}/cancel")
    Call<MoveOutRequest> cancelMoveOutRequest(@Path("request_id") int requestId);
}

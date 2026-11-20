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
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import retrofit2.http.Url;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;











public interface ApiService {



    @POST("auth/session")
    Call<JsonElement> createSession();

    @GET("auth/me")
    Call<User> getMe();


    @GET("users/me")
    Call<User> getProfile();


    @PATCH("users/me")
    Call<User> updateProfile(@Body Map<String, Object> body);

    @GET("users/me/avatar/signature")
    Call<JsonElement> getAvatarUploadSignature();

    @Multipart
    @POST
    Call<JsonElement> uploadAvatar(
            @Url String uploadUrl,
            @Part MultipartBody.Part file,
            @Part("api_key") RequestBody apiKey,
            @Part("timestamp") RequestBody timestamp,
            @Part("folder") RequestBody folder,
            @Part("signature") RequestBody signature
    );



    @GET("rooms")
    Call<List<Room>> getRooms(@QueryMap Map<String, String> query);

    @GET("rooms/{room_id}")
    Call<Room> getRoom(@Path("room_id") int roomId);



    @GET("users/me/favorites")
    Call<JsonElement> getFavorites();

    @POST("users/me/favorites/{room_id}")
    Call<JsonElement> addFavorite(@Path("room_id") int roomId);

    @DELETE("users/me/favorites/{room_id}")
    Call<JsonElement> removeFavorite(@Path("room_id") int roomId);



    @POST("rooms/{room_id}/rental-requests")
    Call<JsonElement> createRentalRequest(@Path("room_id") int roomId,
                                          @Body Map<String, Object> body);

    @GET("users/me/rental-requests")
    Call<List<RentalRequest>> getRentalRequests();

    @GET("users/me/rental-requests/{request_id}")
    Call<RentalRequest> getRentalRequest(@Path("request_id") int requestId);

    @PATCH("users/me/rental-requests/{request_id}/cancel")
    Call<JsonElement> cancelRentalRequest(@Path("request_id") int requestId);


    @GET("users/me/contracts")
    Call<List<Contract>> getContracts();

    @GET("users/me/contracts/{contract_id}")
    Call<Contract> getContract(@Path("contract_id") int contractId);


    @GET("users/me/payments")
    Call<List<Payment>> getPayments();


    @GET("users/me/maintenance-requests")
    Call<List<MaintenanceRequest>> getMaintenanceRequests();


    @POST("users/me/maintenance-requests")
    Call<MaintenanceRequest> createMaintenanceRequest(@Body Map<String, Object> body);

    @PATCH("users/me/maintenance-requests/{request_id}/cancel")
    Call<MaintenanceRequest> cancelMaintenanceRequest(@Path("request_id") int requestId);


    @GET("users/me/move-out-requests")
    Call<List<MoveOutRequest>> getMoveOutRequests();


    @POST("users/me/move-out-requests")
    Call<MoveOutRequest> createMoveOutRequest(@Body Map<String, Object> body);

    @PATCH("users/me/move-out-requests/{request_id}/cancel")
    Call<MoveOutRequest> cancelMoveOutRequest(@Path("request_id") int requestId);
}

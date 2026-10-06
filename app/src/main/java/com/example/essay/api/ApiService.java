package com.example.essay.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ApiService {

    @GET("protected")
    Call<Map<String, Object>> verifyFirebaseToken(
            @Header("Authorization") String authorization
    );
}
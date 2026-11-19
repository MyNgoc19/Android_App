package com.example.user.network;

/**
 * Callback dùng chung. Chạy trên main thread nên cập nhật giao diện trực tiếp được.
 */
public interface ApiCallback<T> {

    void onSuccess(T data);

    void onError(String message);
}

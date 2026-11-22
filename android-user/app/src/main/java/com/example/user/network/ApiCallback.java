package com.example.user.network;




public interface ApiCallback<T> {

    void onSuccess(T data);

    void onError(String message);
}

package com.phoneapp.phonepulse.data.network;

import android.content.SharedPreferences;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

@Singleton
public class AuthInterceptor implements Interceptor {

    private final SharedPreferences sharedPreferences;

    @Inject
    public AuthInterceptor(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = sharedPreferences.getString("auth_token", null);

        if (token != null) {
            Request requestWithAuth = original.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(requestWithAuth);
        }

        return chain.proceed(original);
    }
}

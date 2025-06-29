package com.phoneapp.phonepulse.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Constants {
    public static final String BASE_URL = "http://10.0.2.2:5000";
    public static final int TIMEOUT = 30; // seconds
    public static final String SHARED_PREFS = "app_prefs";
    public static final String TOKEN_KEY = "auth_token";
    public static final String PRODUCT_ID = "product_id";

    // Hàm lấy token từ SharedPreferences
    public static String getToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        return prefs.getString(TOKEN_KEY, null);
    }
}

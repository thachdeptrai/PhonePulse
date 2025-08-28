package com.phoneapp.phonepulse.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Constants {
     // public static final String BASE_URL = "http://192.168.1.9:5000";
  //  public static final String BASE_URL = "http://10.24.32.145:5000";
  //public static final String BASE_URL = "http://192.168.1.9:5000";
      //   public static final String BASE_URL = "http://192.168.100.188:5000"; // sơn home
       //  public static final String BASE_URL = "http://10.128.57.42:5000";

    //  public static final String BASE_URL = "http://10.128.57.42:5000"; x
      public static final String BASE_URL = "http://10.24.49.108:5000";



    public static final int TIMEOUT = 30;
    public static final String SHARED_PREFS = "app_prefs";
    public static final String TOKEN_KEY = "auth_token";
    public static final String PRODUCT_ID = "product_id";
    public static final String VARIANT_ID = "variant_id";

    // NEW: User Info Keys
    public static final String USER_NAME = "user_name";
    public static final String USER_EMAIL = "user_email";
    public static final String USER_PHONE = "user_phone";
    public static final String USER_IMAGE = "user_image";

    // Token
    public static String getToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        return prefs.getString(TOKEN_KEY, null);
    }

    public static void saveToken(Context context, String token) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(TOKEN_KEY, token).apply();
    }

    public static void clearToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        prefs.edit().remove(TOKEN_KEY).apply();
    }

    // NEW: User Info Save
    public static void saveUserInfo(Context context, String name, String email, String phone, String imageUrl) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(USER_NAME, name)
                .putString(USER_EMAIL, email)
                .putString(USER_PHONE, phone)
                .putString(USER_IMAGE, imageUrl)
                .apply();
    }

    // NEW: User Info Get
    public static String getUserName(Context context) {
        return context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE).getString(USER_NAME, "");
    }

    public static String getUserEmail(Context context) {
        return context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE).getString(USER_EMAIL, "");
    }

    public static String getUserPhone(Context context) {
        return context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE).getString(USER_PHONE, "");
    }

    public static String getUserImage(Context context) {
        return context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE).getString(USER_IMAGE, "");
    }

    // Optional: clear all
    public static void clearUserInfo(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(USER_NAME)
                .remove(USER_EMAIL)
                .remove(USER_PHONE)
                .remove(USER_IMAGE)
                .apply();
    }
}

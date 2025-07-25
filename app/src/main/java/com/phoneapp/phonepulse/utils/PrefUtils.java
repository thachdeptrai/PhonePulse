package com.phoneapp.phonepulse.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.phoneapp.phonepulse.models.User;
import com.phoneapp.phonepulse.repository.LoginResponse;

public class PrefUtils {

    private static final String PREF_NAME = "PhonePulsePrefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER = "user_data";

    public static void saveToken(Context context, String token) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public static String getToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_TOKEN, null);
    }

    public static void saveUser(Context context, User user) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = new Gson().toJson(user);
        prefs.edit().putString(KEY_USER, json).apply();
    }

    public static User getUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_USER, null);
        if (json == null) return null;
        return new Gson().fromJson(json, User.class);
    }

    public static void clearAll(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
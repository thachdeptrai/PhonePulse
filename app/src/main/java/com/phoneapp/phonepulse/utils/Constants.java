package com.phoneapp.phonepulse.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Constants {
    // Base URL for API calls. Use 10.0.2.2 for Android Emulator to connect to localhost.
    // Use 192.168.x.x for real devices connected to the same local network.
   //  public static final String BASE_URL = "http://10.0.2.2:5000";
    public static final String BASE_URL = "http://192.168.1.9:5000"; // Your current active base URL

    public static final int TIMEOUT = 30; // Network timeout in seconds
    public static final String SHARED_PREFS = "app_prefs"; // Name for SharedPreferences file
    public static final String TOKEN_KEY = "auth_token"; // Key for storing authentication token
    public static final String PRODUCT_ID = "product_id"; // Key for passing product ID in intents/bundles
    public static final String VARIANT_ID = "variant_id"; // Key for passing variant ID in intents/bundles

    /**
     * Retrieves the authentication token from SharedPreferences.
     * @param context The application context.
     * @return The stored authentication token, or null if not found.
     */
    public static String getToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        return prefs.getString(TOKEN_KEY, null);
    }

    /**
     * Saves the authentication token to SharedPreferences.
     * @param context The application context.
     * @param token The token string to be saved.
     */
    public static void saveToken(Context context, String token) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(TOKEN_KEY, token);
        editor.apply(); // Apply changes asynchronously
    }

    /**
     * Clears the authentication token from SharedPreferences.
     * This is useful during logout.
     * @param context The application context.
     */
    public static void clearToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(TOKEN_KEY);
        editor.apply(); // Apply changes asynchronously
    }
}
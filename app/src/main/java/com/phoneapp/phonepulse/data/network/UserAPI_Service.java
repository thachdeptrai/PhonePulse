package com.phoneapp.phonepulse.data.network;

import com.phoneapp.phonepulse.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserAPI_Service {

    // ---------- Auth ----------
    @POST("api/users/register")
    Call<API_UserResponse<User>> register(@Body RegisterRequest request);

    @POST("api/users/login")
    Call<API_UserResponse<User>> login(@Body LoginRequest request);

    @POST("api/users/logout")
    Call<API_UserResponse<MessageResponse>> logout(@Header("Authorization") String token);


    // ---------- User cá nhân ----------
    @GET("api/users/profile")
    Call<API_UserResponse<User>> getProfile(@Header("Authorization") String token);

    @PUT("api/users/profile")
    Call<API_UserResponse<User>> updateProfile(@Header("Authorization") String token, @Body UpdateProfileRequest request);

    @PUT("api/users/change-password")
    Call<API_UserResponse<MessageResponse>> changePassword(@Header("Authorization") String token, @Body ChangePasswordRequest request);

    @PUT("api/users/verify-self")
    Call<API_UserResponse<User>> verifySelf(@Header("Authorization") String token);

    @HTTP(method = "DELETE", path = "api/users/delete-self", hasBody = true)
    Call<API_UserResponse<MessageResponse>> deleteSelf(@Header("Authorization") String token, @Body DeleteSelfRequest request);


    // ---------- Admin ----------
    @GET("api/users/stats")
    Call<API_UserResponse<UserStats>> getUserStats(@Header("Authorization") String token);

    @GET("api/users")
    Call<API_UserResponse<UserListResponse>> getUsers(
            @Header("Authorization") String token,
            @Query("page") Integer page,
            @Query("limit") Integer limit,
            @Query("sort") String sort,
            @Query("order") String order,
            @Query("search") String search,
            @Query("status") Boolean status,
            @Query("role") Boolean role,
            @Query("is_verified") Boolean isVerified
    );

    @GET("api/users/{id}")
    Call<API_UserResponse<User>> getUserById(@Header("Authorization") String token, @Path("id") String userId);

    @PUT("api/users/{id}")
    Call<API_UserResponse<User>> updateUser(@Header("Authorization") String token, @Path("id") String userId, @Body UpdateUserRequest request);

    @DELETE("api/users/{id}")
    Call<API_UserResponse<MessageResponse>> deleteUser(@Header("Authorization") String token, @Path("id") String userId);

    @PUT("api/users/{id}/restore")
    Call<API_UserResponse<User>> restoreUser(@Header("Authorization") String token, @Path("id") String userId);

    @PUT("api/users/{id}/verify")
    Call<API_UserResponse<User>> verifyUser(@Header("Authorization") String token, @Path("id") String userId);
}

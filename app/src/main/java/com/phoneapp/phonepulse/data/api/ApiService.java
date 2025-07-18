package com.phoneapp.phonepulse.data.api;

import com.phoneapp.phonepulse.models.*;
import com.phoneapp.phonepulse.repository.LoginResponse;
import com.phoneapp.phonepulse.request.*;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // ========== AUTH ==========
    @POST("/api/users/register")
    Call<ApiResponse> register(@Body RegisterRequest body);

    @POST("/api/users/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest body);

    @POST("/api/users/logout")
    Call<ApiResponse> logout(@Header("Authorization") String token);

    @POST("api/auth/login-social")
    Call<LoginResponse> socialLogin(@Body SocialLoginRequest body);
    // ========== PROFILE ==========
    @GET("/api/users/profile")
    Call<User> getProfile(@Header("Authorization") String token);

    @PUT("/api/users/profile")
    Call<ApiResponse<User>> updateProfile(@Header("Authorization") String token, @Body User user);

    @PUT("/api/users/change-password")
    Call<ApiResponse> changePassword(@Header("Authorization") String token, @Body ChangePasswordRequest body);

    @DELETE("/api/users/delete-self")
    Call<ApiResponse> deleteAccount(@Header("Authorization") String token);


    // ========== PRODUCT ==========
    @GET("/api/products")
    Call<List<Product>> getAllProducts();
    @GET("/api/products/{id}")
    Call<Product> getProductById(@Path("id") String id);
    @GET("/api/products/search")
    Call<List<Product>> searchProducts(@Query("name") String keyword);

    // ========== CATEGORY ==========
    @GET("/api/categories")
    Call<List<Category>> getAllCategories();

    @GET("/api/categories/{id}")
    Call<Category> getCategoryById(@Path("id") String id);


    // ========== COLOR ==========
    @GET("/api/colors")
    Call<List<Color>> getAllColors();

    @GET("/api/colors/{id}")
    Call<Color> getColorById(@Path("id") String id);


    // ========== SIZE ==========
    @GET("/api/sizes")
    Call<List<Size>> getAllSizes();

    @GET("/api/sizes/{id}")
    Call<Size> getSizeById(@Path("id") String id);


    // ========== FAVOURITES ==========
    @GET("/api/favourites")
    Call<List<Product>> getFavourites(@Header("Authorization") String token);

    @POST("/api/favourites")
    Call<ApiResponse> addFavourite(@Header("Authorization") String token, @Body FavouriteRequest body);

    @HTTP(method = "DELETE", path = "/api/favourites", hasBody = true)
    Call<ApiResponse> removeFavourite(@Header("Authorization") String token, @Body FavouriteRequest body);


    // ========== CART ==========
    @GET("/api/cart")
    Call<List<CartItem>> getCart(@Header("Authorization") String token);

    @POST("/api/cart")
    Call<ApiResponse> addToCart(@Header("Authorization") String token, @Body CartRequest body);

    @PUT("/api/cart")
    Call<ApiResponse> updateCart(@Header("Authorization") String token, @Body CartRequest body);

    @HTTP(method = "DELETE", path = "/api/cart", hasBody = true)
    Call<ApiResponse> removeFromCart(@Header("Authorization") String token, @Body CartRequest body);


    // ========== ORDER ==========
    @POST("/api/orders")
    Call<ApiResponse<Order>> createOrder(@Header("Authorization") String token, @Body OrderRequest body);

    @GET("/api/orders")
    Call<List<Order>> getUserOrders(@Header("Authorization") String token);

    @PUT("/api/orders/{id}/cancel")
    Call<ApiResponse> cancelOrder(@Header("Authorization") String token, @Path("id") String id);


    // ========== VOUCHERS ==========
    @POST("/api/vouchers/apply")
    Call<ApiResponse<Voucher>> applyVoucher(@Header("Authorization") String token, @Body VoucherRequest body);


    // ========== REVIEWS ==========
    @GET("/api/reviews/{productId}")
    Call<List<Review>> getReviews(@Path("productId") String productId);

    @POST("/api/reviews")
    Call<ApiResponse<Review>> addReview(@Header("Authorization") String token, @Body Review review);

    @PUT("/api/reviews/{id}")
    Call<ApiResponse<Review>> updateReview(@Header("Authorization") String token, @Path("id") String id, @Body Review review);

    @DELETE("/api/reviews/{id}")
    Call<ApiResponse> deleteReview(@Header("Authorization") String token, @Path("id") String id);


    // ========== PRODUCT IMAGES ==========
    @GET("/api/products/{id}/images")
    Call<List<ProductImage>> getProductImages(@Path("id") String productId);

    @Multipart
    @POST("/api/products/{id}/images")
    Call<ApiResponse> uploadProductImage(@Path("id") String productId, @Part MultipartBody.Part image);

    @DELETE("/api/products/{id}/images")
    Call<ApiResponse> deleteProductImage(@Path("id") String productId, @Query("imageId") String imageId);


    // ========== VARIANTS ==========
    @GET("/api/products/{id}/variants")
    Call<List<Variant>> getVariants(@Path("id") String productId);

    @POST("/api/products/{id}/variants")
    Call<ApiResponse<Variant>> addVariant(@Path("id") String productId, @Body Variant variant);

    @GET("/api/products/{id}/variants/{variantId}")
    Call<Variant> getVariant(@Path("id") String productId, @Path("variantId") String variantId);

    @PUT("/api/products/{id}/variants/{variantId}")
    Call<ApiResponse<Variant>> updateVariant(@Path("id") String productId, @Path("variantId") String variantId, @Body Variant variant);

    @DELETE("/api/products/{id}/variants/{variantId}")
    Call<ApiResponse> deleteVariant(@Path("id") String productId, @Path("variantId") String variantId);


    // ========== NOTIFICATIONS ==========
    @GET("/api/notifications")
    Call<List<Notification>> getNotifications(@Header("Authorization") String token);

    @PUT("/api/notifications/{id}/read")
    Call<ApiResponse> markNotificationAsRead(@Header("Authorization") String token, @Path("id") String id);
}

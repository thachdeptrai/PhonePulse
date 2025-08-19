package com.phoneapp.phonepulse.data.api;

import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.Response.ApplyVoucherResponse;
import com.phoneapp.phonepulse.models.*;
import com.phoneapp.phonepulse.Response.LoginResponse;
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


    // ========== PROFILE ==========
    @GET("api/users/profile")
    Call<ApiResponse<User>> getProfile();

    @PUT("/api/users/profile")
    Call<ApiResponse<User>> updateProfile(@Header("Authorization") String token, @Body User user);

    @PUT("/api/users/change-password")
    Call<ApiResponse<Void>> changePassword(@Body ChangePasswordRequest body);

    @DELETE("/api/users/delete-self")
    Call<ApiResponse> deleteAccount(@Header("Authorization") String token);

    // ========== PRODUCTS ==========
    @GET("/api/products")
    Call<List<Product>> getAllProductsRaw();

    // Lấy Product gốc theo ID (vẫn dùng cho ProductDetailActivity để lấy discount)
    // Backend route: GET /api/products/{id}
    @GET("/api/products/{id}")
    Call<Product> getProductById(@Path("id") String id);


    // ========== CATEGORY ==========
    @GET("/api/categories")
    Call<List<Category>> getAllCategories();

    @GET("/api/categories/{id}")
    Call<Category> getCategoryById(@Path("id") String id);

    @GET("api/products")
    Call<List<ProductGirdItem>> getProductsByCategory(@Query("category") String categoryId);


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
//    @GET("/api/favourite")
//    Call<List<Product>> getFavourites(@Header("Authorization") String token);


    @GET("api/favourite") // Hoặc đường dẫn API đúng của bạn
    Call<ApiResponse<List<Favourite>>> getFavourites();

    @POST("/api/favourite")
    Call<ApiResponse> addFavourite(@Body FavouriteRequest body);

    @HTTP(method = "DELETE", path = "/api/favourite", hasBody = true)
    Call<ApiResponse> removeFavourite(@Body FavouriteRequest body);


    // ========== CART ==========
    @GET("/api/cart")
    Call<ApiResponse<Cart>> getCart();

    @POST("/api/cart")
        // Sử dụng CartRequest.AddToCart
    Call<ApiResponse<Cart>> addToCart(@Body CartRequest.AddToCart request);

    @PUT("/api/cart")
        // Sử dụng CartRequest.UpdateCartItem
    Call<ApiResponse<Cart>> updateCartItem(@Body CartRequest.UpdateCartItem request);

    @HTTP(method = "DELETE", path = "/api/cart", hasBody = true)
        // Sử dụng CartRequest.RemoveCartItem
    Call<ApiResponse<Cart>> removeFromCart(@Body CartRequest.RemoveCartItem request);


    // ========== ORDER ==========
    @POST("/api/orders")
    Call<ApiResponse<Order>> createOrder(@Body OrderRequest body);

    // Đã sửa lỗi kiểu trả về để khớp với backend
    @GET("/api/orders")
    Call<ApiResponse<List<Order>>> getUserOrders();


    @PUT("/api/orders/{id}/cancel")
    Call<ApiResponse> cancelOrder(@Header("Authorization") String token, @Path("id") String id);


    // ========== VOUCHERS ==========
    @POST("/api/vouchers/apply")
    Call<ApiResponse<Voucher>> applyVoucher(@Header("Authorization") String token, @Body VoucherRequest body);

    @GET("/api/vouchers")
    Call<ApiResponse<List<Voucher>>> getVouchers(@Header("Authorization") String token);

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


    // ========================================================================================
    // ========== VARIANTS (Đã điều chỉnh để khớp với Home_FRAGMENT mới nhất) ==========
    // ========================================================================================

    // Dùng cho Home_FRAGMENT để lấy variants cho TỪNG sản phẩm cụ thể
    // Backend route: GET /api/products/:id/variants
    // LƯU Ý: Hàm này chỉ populate color_id và size_id ở backend, không có product_name hay image_url.
    // Thêm vào interface ApiService
// THÊM: Nếu bạn có endpoint để lấy TẤT CẢ variants (không theo product ID)
    // Dùng cho fetchAllVariants() trong TatCaDonHang_FRAGMENT
    @GET("/api/variants") // Hoặc endpoint tương ứng
    Call<ApiResponse<List<Variant>>> getAllVariants(@Header("Authorization") String token);
    @GET("/api/products/{productId}/variants")
    Call<List<Variant>> getVariantsForProduct(@Path("productId") String productId);


    // Thêm variant mới cho một product_id cụ thể
    // Backend route: POST /api/products/:id/variants
    @POST("/api/products/{productId}/variants")
    Call<ApiResponse<Variant>> addVariantForProduct(@Path("productId") String productId, @Body Variant variant);
    // Lấy một variant cụ thể qua product_id và variant_id
    // LƯU Ý: Hàm này chỉ populate color_id và size_id ở backend, không có product_name hay image_url.
    @GET("/api/products/{productId}/variants/{variantId}")
    Call<ApiResponse<Variant>> getVariantById(@Path("productId") String productId, @Path("variantId") String variantId); // 👈 TÊN HÀM NÀY PHẢI KHỚP VỚI CÁI TÔI DÙNG

    // Lấy một variant cụ thể qua product_id và variant_id
    // Backend route: GET /api/products/:id/variants/:variantId
    // LƯU Ý: Hàm này chỉ populate color_id và size_id ở backend, không có product_name hay image_url.
    @GET("/api/products/{productId}/variants/{variantId}")
    Call<Variant> getVariantForProductById(@Path("productId") String productId, @Path("variantId") String variantId);

    // Cập nhật một variant cụ thể qua product_id và variant_id
    // Backend route: PUT /api/products/:id/variants/:variantId
    @PUT("/api/products/{productId}/variants/{variantId}")
    Call<ApiResponse<Variant>> updateVariantForProductById(@Path("productId") String productId,
                                                           @Path("variantId") String variantId, @Body Variant variant);

    // Xoá một variant cụ thể qua product_id và variant_id
    // Backend route: DELETE /api/products/:id/variants/:variantId
    @DELETE("/api/products/{productId}/variants/{variantId}")
    Call<ApiResponse> deleteVariantForProductById(@Path("productId") String productId, @Path("variantId") String variantId);


    // ===============================================================
    // API cho ProductDetailActivity (nếu bạn muốn nó lấy variant chi tiết bằng variantId)
    // Backend cần có route và controller tương ứng với aggregation.
    // Nếu không, ProductDetailActivity sẽ cần gọi getVariantForProductById VÀ getProductById riêng.
    // ===============================================================
    // @GET("/api/variants/{id}/details")
    // Call<Variant> getVariantDetailsById(@Path("id") String variantId);


    // ========== NOTIFICATIONS ==========
    @GET("/api/notifications")
    Call<List<Notification>> getNotifications(@Header("Authorization") String token);

    @PUT("/api/notifications/{id}/read")
    Call<ApiResponse> markNotificationAsRead(@Header("Authorization") String token, @Path("id") String id);
}

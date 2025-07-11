package com.phoneapp.phonepulse.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.phoneapp.phonepulse.Response.CartDataResponse;
import com.phoneapp.phonepulse.data.api.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.repository.CartRepository;
import com.phoneapp.phonepulse.request.CartDisplayItem;
import com.phoneapp.phonepulse.request.CartItem; // <-- Vẫn cần CartItem ban đầu
import com.phoneapp.phonepulse.request.CartRequest;
import com.phoneapp.phonepulse.retrofit.RetrofitClient;
import com.phoneapp.phonepulse.utils.Constants;

import com.phoneapp.phonepulse.models.Product; // Import Product model
import com.phoneapp.phonepulse.models.Variant; // Import Variant model

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch; // Dùng để đồng bộ các cuộc gọi API phụ

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartViewModel extends AndroidViewModel {

    // LiveData bây giờ sẽ chứa danh sách CartDisplayItem
    private final MutableLiveData<List<CartDisplayItem>> _cartItems = new MutableLiveData<>();
    public LiveData<List<CartDisplayItem>> getCartItems() {
        return _cartItems;
    }

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> getError() {
        return _error;
    }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> getIsLoading() {
        return _isLoading;
    }

    private final CartRepository cartRepository;
    private final ApiService apiService; // Cần ApiService trực tiếp để gọi getProduct/getVariant

    public CartViewModel(@NonNull Application application, CartRepository cartRepository, ApiService apiService) {
        super(application);
        this.cartRepository = cartRepository;
        this.apiService = apiService;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        @NonNull
        private final Application application;

        public Factory(@NonNull Application application) {
            this.application = application;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CartViewModel.class)) {
                ApiService apiService = RetrofitClient.getApiService(Constants.getToken(application.getApplicationContext()));
                CartRepository repository = new CartRepository(apiService);
                return (T) new CartViewModel(application, repository, apiService); // Truyền apiService
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }

    public void fetchCart() {
        _isLoading.postValue(true);
        String token = Constants.getToken(getApplication().getApplicationContext());
        if (token == null || token.isEmpty()) {
            _error.postValue("Vui lòng đăng nhập để xem giỏ hàng.");
            _isLoading.postValue(false);
            _cartItems.postValue(new ArrayList<>()); // Đảm bảo trả về list rỗng
            return;
        }

        cartRepository.getCart(token).enqueue(new Callback<ApiResponse<CartDataResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CartDataResponse>> call, @NonNull Response<ApiResponse<CartDataResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<CartDataResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<CartItem> rawCartItems = apiResponse.getData().getItems(); // Danh sách CartItem thô từ backend

                        if (rawCartItems == null || rawCartItems.isEmpty()) {
                            _cartItems.postValue(new ArrayList<>()); // Giỏ hàng trống
                            _isLoading.postValue(false);
                            return;
                        }

                        // overallLatch sẽ đếm ngược 1 lần cho mỗi rawCartItem sau khi tất cả các API phụ của nó hoàn tất
                        final CountDownLatch overallLatch = new CountDownLatch(rawCartItems.size());
                        // Sử dụng Collections.synchronizedList để đảm bảo an toàn luồng khi thêm các mục
                        final List<CartDisplayItem> displayItems = Collections.synchronizedList(new ArrayList<>(rawCartItems.size()));

                        for (final CartItem rawItem : rawCartItems) { // Đảm bảo rawItem là final hoặc effectively final
                            final CartDisplayItem displayItem = new CartDisplayItem(rawItem);
                            displayItems.add(displayItem); // Thêm vào danh sách trước (sẽ được cập nhật sau)

                            String productId = rawItem.getProductId();
                            String variantId = rawItem.getVariantId();

                            // itemDetailsLatch sẽ đếm ngược cho các API phụ (Product và Variant) của MỘT mục
                            final CountDownLatch itemDetailsLatch = new CountDownLatch((variantId != null && !variantId.isEmpty()) ? 2 : 1);

                            // Runnable để thực thi khi tất cả các chi tiết của MỘT mục đã được lấy xong (hoặc thất bại)
                            Runnable onItemDetailsComplete = () -> {
                                overallLatch.countDown(); // Giảm số lượng chờ của overallLatch
                                // Sau khi overallLatch = 0, cập nhật LiveData
                                if (overallLatch.getCount() == 0) {
                                    _cartItems.postValue(displayItems);
                                    _isLoading.postValue(false);
                                }
                            };

                            // --- BẮT ĐẦU: Gọi API để lấy thông tin chi tiết Product ---
                            apiService.getProductById(productId).enqueue(new Callback<Product>() {
                                @Override
                                public void onResponse(@NonNull Call<Product> productCall, @NonNull Response<Product> productResponse) {
                                    Product fetchedProduct = productResponse.body();
                                    if (fetchedProduct != null) {
                                        // Đồng bộ hóa khi cập nhật displayItem
                                        synchronized (displayItem) {
                                            displayItem.setProductName(fetchedProduct.getName());
                                            if (fetchedProduct.getProductImage() != null) {
                                                displayItem.setProductImageLUrl(fetchedProduct.getProductImage().getImageUrl());
                                            }
                                            // Chỉ cập nhật giá từ Product nếu giá biến thể ban đầu là 0.0
                                            if (displayItem.getVariantPrice() == 0.0) { // Gọi đúng phương thức getVariantPrice()
                                                displayItem.setVariantPrice(fetchedProduct.getPrice()); // Đặt đúng giá
                                            }
                                        }
                                    } else {
                                        Log.w("CartViewModel", "Product details not found for ID: " + productId);
                                    }
                                    itemDetailsLatch.countDown(); // Hoàn thành cuộc gọi Product
                                    if (itemDetailsLatch.getCount() == 0) {
                                        onItemDetailsComplete.run();
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<Product> productCall, @NonNull Throwable t) {
                                    Log.e("CartViewModel", "Error fetching product for " + productId + ": " + t.getMessage());
                                    itemDetailsLatch.countDown(); // Cuộc gọi Product thất bại
                                    if (itemDetailsLatch.getCount() == 0) {
                                        onItemDetailsComplete.run();
                                    }
                                }
                            });

                            // --- BẮT ĐẦU: Gọi API để lấy thông tin chi tiết Variant (nếu variantId tồn tại) ---
                            if (variantId != null && !variantId.isEmpty()) {
                                apiService.getVariant(productId, variantId).enqueue(new Callback<Variant>() {
                                    @Override
                                    public void onResponse(@NonNull Call<Variant> variantCall, @NonNull Response<Variant> variantResponse) {
                                        Variant fetchedVariant = variantResponse.body();
                                        if (fetchedVariant != null) {
                                            // Đồng bộ hóa khi cập nhật displayItem
                                            synchronized (displayItem) {
                                              // SỬA ĐỔI: Lấy tên màu từ Variant
                                                displayItem.setVariantPrice(fetchedVariant.getPrice()); // Đặt giá chính xác từ Variant
                                            }
                                        } else {
                                            Log.w("CartViewModel", "Variant details not found for ID: " + variantId);
                                        }
                                        itemDetailsLatch.countDown(); // Hoàn thành cuộc gọi Variant
                                        if (itemDetailsLatch.getCount() == 0) {
                                            onItemDetailsComplete.run();
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<Variant> variantCall, @NonNull Throwable t) {
                                        Log.e("CartViewModel", "Error fetching variant for " + variantId + ": " + t.getMessage());
                                        itemDetailsLatch.countDown(); // Cuộc gọi Variant thất bại
                                        if (itemDetailsLatch.getCount() == 0) {
                                            onItemDetailsComplete.run();
                                        }
                                    }
                                });
                            }
                        }
                    } else { // apiResponse.isSuccess() là false hoặc apiResponse.getData() là null
                        _cartItems.postValue(new ArrayList<>()); // Giỏ hàng trống
                        _error.postValue(apiResponse.getMessage() != null ? apiResponse.getMessage() : "Không thể lấy dữ liệu giỏ hàng.");
                        _isLoading.postValue(false);
                    }
                } else { // response.isSuccessful() là false (lỗi HTTP)
                    String errorMsg = "Lấy giỏ hàng thất bại: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) { /* ignore */ }
                    _error.postValue(errorMsg);
                    Log.e("CartViewModel", "HTTP Error fetching cart: " + errorMsg);
                    _isLoading.postValue(false);
                    _cartItems.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CartDataResponse>> call, @NonNull Throwable t) {
                _isLoading.postValue(false);
                _error.postValue("Lỗi kết nối mạng khi lấy giỏ hàng: " + t.getMessage());
                Log.e("CartViewModel", "Network error fetching cart", t);
                _cartItems.postValue(new ArrayList<>());
            }
        });
    }


    // --- Các phương thức thao tác giỏ hàng (đã được sửa để sử dụng CartDisplayItem làm đầu vào) ---

    public void addToCart(String productId, String variantId, int quantity) {
        _isLoading.postValue(true);
        String token = Constants.getToken(getApplication().getApplicationContext());
        if (token == null || token.isEmpty()) {
            _error.postValue("Vui lòng đăng nhập để mua hàng.");
            _isLoading.postValue(false);
            return;
        }

        CartRequest request = new CartRequest(productId, variantId, quantity);
        cartRepository.addToCart(token, request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                _isLoading.postValue(false);
                ApiResponse apiResponse = response.body();
                if (response.isSuccessful() && apiResponse != null && apiResponse.isSuccess()) {
                    fetchCart(); // Re-fetch cart to update UI
                } else {
                    String errorMessage = apiResponse != null ? apiResponse.getMessage() : "Thêm sản phẩm vào giỏ hàng thất bại.";
                    _error.postValue(errorMessage);
                    Log.e("CartViewModel", "API Error adding to cart: " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                _isLoading.postValue(false);
                _error.postValue("Lỗi mạng khi thêm sản phẩm vào giỏ hàng: " + t.getMessage());
                Log.e("CartViewModel", "Network error adding to cart", t);
            }
        });
    }

    public void updateCartItemQuantity(CartDisplayItem item, int newQuantity) { // <-- Thay đổi tham số
        _isLoading.postValue(true);
        String token = Constants.getToken(getApplication().getApplicationContext());
        if (token == null || token.isEmpty()) {
            _error.postValue("Vui lòng đăng nhập.");
            _isLoading.postValue(false);
            return;
        }

        // Lấy productId và variantId từ CartDisplayItem
        String productId = item.getProductId();
        String variantId = item.getVariantId();

        if (productId == null || variantId == null) {
            _error.postValue("Thông tin sản phẩm hoặc biến thể không hợp lệ để cập nhật.");
            _isLoading.postValue(false);
            return;
        }

        CartRequest request = new CartRequest(productId, variantId, newQuantity);
        cartRepository.updateCart(token, request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                _isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        fetchCart(); // Cập nhật lại giỏ hàng sau khi thành công
                    } else {
                        String errorMessage = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Cập nhật giỏ hàng thất bại.";
                        _error.postValue(errorMessage);
                        Log.e("CartViewModel", "API Error updating cart: " + errorMessage);
                    }
                } else {
                    String errorMsg = "Cập nhật giỏ hàng thất bại: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) { /* ignore */ }
                    }
                    _error.postValue(errorMsg);
                    Log.e("CartViewModel", "HTTP Error updating cart: " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                _isLoading.postValue(false);
                _error.postValue("Lỗi kết nối mạng khi cập nhật giỏ hàng: " + t.getMessage());
                Log.e("CartViewModel", "Network error updating cart", t);
            }
        });
    }

    public void removeCartItem(CartDisplayItem item) { // <-- Thay đổi tham số
        _isLoading.postValue(true);
        String token = Constants.getToken(getApplication().getApplicationContext());
        if (token == null || token.isEmpty()) {
            _error.postValue("Vui lòng đăng nhập.");
            _isLoading.postValue(false);
            return;
        }

        // Lấy productId và variantId từ CartDisplayItem
        String productId = item.getProductId();
        String variantId = item.getVariantId();

        if (productId == null || variantId == null) {
            _error.postValue("Thông tin sản phẩm hoặc biến thể không hợp lệ để xóa.");
            _isLoading.postValue(false);
            return;
        }

        CartRequest request = new CartRequest(productId, variantId, 0); // Quantity 0 để xóa
        cartRepository.removeCartItem(token, request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                _isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        fetchCart(); // Cập nhật lại giỏ hàng sau khi xóa thành công
                    } else {
                        String errorMessage = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Xóa sản phẩm khỏi giỏ hàng thất bại.";
                        _error.postValue(errorMessage);
                        Log.e("CartViewModel", "API Error removing cart item: " + errorMessage);
                    }
                } else {
                    String errorMsg = "Xóa sản phẩm khỏi giỏ hàng thất bại: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) { /* ignore */ }
                    }
                    _error.postValue(errorMsg);
                    Log.e("CartViewModel", "HTTP Error removing cart item: " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                _isLoading.postValue(false);
                _error.postValue("Lỗi kết nối mạng khi xóa sản phẩm khỏi giỏ hàng: " + t.getMessage());
                Log.e("CartViewModel", "Network error removing cart item", t);
            }
        });
    }
}
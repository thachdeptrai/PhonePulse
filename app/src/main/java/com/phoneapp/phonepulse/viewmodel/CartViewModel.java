package com.phoneapp.phonepulse.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.phoneapp.phonepulse.data.api.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.request.CartItem;   // For getCart response
import com.phoneapp.phonepulse.request.CartRequest; // For add/update/remove requests
import com.phoneapp.phonepulse.retrofit.RetrofitClient;
import com.phoneapp.phonepulse.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartViewModel extends AndroidViewModel {

    private final MutableLiveData<List<CartItem>> _cartItems = new MutableLiveData<>();
    public LiveData<List<CartItem>> getCartItems() {
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

    private final ApiService apiService;
    private final String authToken;

    public CartViewModel(@NonNull Application application) {
        super(application);
        this.authToken = Constants.getToken(application.getApplicationContext());
        Log.d("CartViewModel", "Auth Token retrieved: " + (authToken != null && authToken.length() > 10 ? authToken.substring(0, 10) + "..." : (authToken != null ? authToken : "null")));

        apiService = RetrofitClient.getApiService(authToken);
    }

    // --- ViewModel Factory (unchanged) ---
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
                return (T) new CartViewModel(application);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }

    // --- Business Logic Methods ---

    public void fetchCart() {
        if (authToken == null || authToken.isEmpty()) {
            _error.postValue("Người dùng chưa đăng nhập hoặc phiên đã hết hạn. Vui lòng đăng nhập lại.");
            _isLoading.postValue(false);
            _cartItems.postValue(new ArrayList<>());
            return;
        }

        _isLoading.postValue(true);
        // Call the 'getCart' method from ApiService, which returns ApiResponse<List<CartItem>>
        apiService.getCart(authToken).enqueue(new Callback<ApiResponse<List<CartItem>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CartItem>>> call, Response<ApiResponse<List<CartItem>>> response) {
                _isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<CartItem>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        _cartItems.postValue(apiResponse.getData());
                        _error.postValue(null);
                        Log.d("CartViewModel", "Fetched " + apiResponse.getData().size() + " cart items.");
                    } else {
                        String errorMessage = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Lỗi khi tải giỏ hàng từ máy chủ.";
                        _error.postValue(errorMessage);
                        _cartItems.postValue(new ArrayList<>());
                        Log.e("CartViewModel", "API Error fetching cart: " + errorMessage);
                    }
                } else {
                    String errorMsg = "Không thể tải giỏ hàng: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("CartViewModel", "Error parsing error body", e);
                        }
                    }
                    _error.postValue(errorMsg);
                    _cartItems.postValue(new ArrayList<>());
                    Log.e("CartViewModel", "HTTP Error fetching cart: " + response.code() + " " + response.message());

                    if (response.code() == 401) {
                        // Consider clearing token and redirecting to login if token is expired
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CartItem>>> call, Throwable t) {
                _isLoading.postValue(false);
                String errorMessage = "Lỗi kết nối mạng: " + t.getMessage();
                _error.postValue(errorMessage);
                _cartItems.postValue(new ArrayList<>());
                Log.e("CartViewModel", "Network error fetching cart", t);
            }
        });
    }

    /**
     * Updates the quantity of an item in the cart.
     * @param item The CartItem to update.
     * @param newQuantity The new quantity for the item.
     */
    public void updateCartItemQuantity(CartItem item, int newQuantity) {
        if (authToken == null || authToken.isEmpty()) {
            _error.postValue("Người dùng chưa đăng nhập. Vui lòng đăng nhập lại.");
            return;
        }
        if (newQuantity <= 0) {
            // This case should be handled by calling removeCartItem or showing a dialog
            _error.postValue("Số lượng phải lớn hơn 0.");
            return;
        }

        _isLoading.postValue(true);
        // Create a CartRequest for updating the quantity
        // Assuming CartItem contains necessary IDs for CartRequest
        String productId = item.getProduct().getId();
        String variantId = item.getVariant() != null ? item.getVariant().getId() : null; // Get variant ID if applicable
        CartRequest updateRequest = new CartRequest(productId, variantId, newQuantity);

        apiService.updateCart(authToken, updateRequest).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                _isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        _error.postValue(null);
                        Log.d("CartViewModel", "Quantity updated successfully for product: " + productId);
                        fetchCart(); // Re-fetch the cart to get the updated list and total price
                    } else {
                        String errorMessage = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Cập nhật số lượng thất bại.";
                        _error.postValue(errorMessage);
                        Log.e("CartViewModel", "API Error updating quantity: " + errorMessage);
                    }
                } else {
                    String errorMsg = "Cập nhật số lượng thất bại: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) { /* ignore */ }
                    }
                    _error.postValue(errorMsg);
                    Log.e("CartViewModel", "HTTP Error updating quantity: " + errorMsg);
                    fetchCart(); // Re-fetch cart on network/http failure to sync state
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                _isLoading.postValue(false);
                _error.postValue("Lỗi mạng khi cập nhật số lượng: " + t.getMessage());
                Log.e("CartViewModel", "Network error updating quantity", t);
                fetchCart(); // Re-fetch cart on network failure to sync state
            }
        });
    }

    /**
     * Removes an item from the cart.
     * @param item The CartItem to remove.
     */
    public void removeCartItem(CartItem item) {
        if (authToken == null || authToken.isEmpty()) {
            _error.postValue("Người dùng chưa đăng nhập. Vui lòng đăng nhập lại.");
            return;
        }

        _isLoading.postValue(true);
        // Create a CartRequest for removing the item
        String productId = item.getProduct().getId();
        String variantId = item.getVariant() != null ? item.getVariant().getId() : null; // Get variant ID if applicable
        // When removing, quantity might not be strictly needed by backend, but if CartRequest requires it, set to 0 or 1
        CartRequest removeRequest = new CartRequest(productId, variantId, 0); // Quantity 0 or 1 usually indicates removal

        apiService.removeFromCart(authToken, removeRequest).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                _isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        _error.postValue(null);
                        Log.d("CartViewModel", "Item removed successfully: " + productId);
                        fetchCart(); // Re-fetch the cart to get the updated list
                    } else {
                        String errorMessage = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Xóa sản phẩm thất bại.";
                        _error.postValue(errorMessage);
                        Log.e("CartViewModel", "API Error removing item: " + errorMessage);
                    }
                } else {
                    String errorMsg = "Xóa sản phẩm thất bại: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) { /* ignore */ }
                    }
                    _error.postValue(errorMsg);
                    Log.e("CartViewModel", "HTTP Error removing item: " + errorMsg);
                    fetchCart(); // Re-fetch cart on network/http failure to sync state
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                _isLoading.postValue(false);
                _error.postValue("Lỗi mạng khi xóa sản phẩm: " + t.getMessage());
                Log.e("CartViewModel", "Network error removing item", t);
                fetchCart(); // Re-fetch cart on network failure to sync state
            }
        });
    }

    /**
     * Adds a product to the cart.
     * @param productId The ID of the product to add.
     * @param variantId The ID of the variant (can be null if no variants).
     * @param quantity The quantity to add.
     */
    public void addToCart(String productId, String variantId, int quantity) {
        if (authToken == null || authToken.isEmpty()) {
            _error.postValue("Người dùng chưa đăng nhập. Vui lòng đăng nhập lại.");
            return;
        }
        if (quantity <= 0) {
            _error.postValue("Số lượng phải lớn hơn 0 để thêm vào giỏ hàng.");
            return;
        }

        _isLoading.postValue(true);
        CartRequest addRequest = new CartRequest(productId, variantId, quantity);

        apiService.addToCart(authToken, addRequest).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                _isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        _error.postValue(null);
                        Log.d("CartViewModel", "Product added to cart: " + productId + " qty: " + quantity);
                        fetchCart(); // Re-fetch cart to update UI
                    } else {
                        String errorMessage = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Thêm sản phẩm vào giỏ hàng thất bại.";
                        _error.postValue(errorMessage);
                        Log.e("CartViewModel", "API Error adding to cart: " + errorMessage);
                    }
                } else {
                    String errorMsg = "Thêm sản phẩm vào giỏ hàng thất bại: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) { /* ignore */ }
                    }
                    _error.postValue(errorMsg);
                    Log.e("CartViewModel", "HTTP Error adding to cart: " + errorMsg);
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

    // TODO: Implement toggleItemSelected and deleteSelectedItems if you add checkbox logic
    // You would pass a list of CartRequest or specific item IDs to a new API endpoint.
}
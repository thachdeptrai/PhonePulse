package com.phoneapp.phonepulse.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.phoneapp.phonepulse.repository.CartRepository;
import com.phoneapp.phonepulse.request.CartItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartViewModel extends ViewModel {
    private final CartRepository cartRepository;
    private final MutableLiveData<List<CartItem>> cartItems = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public CartViewModel(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public LiveData<List<CartItem>> getCartItems() {
        return cartItems;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void fetchCart(String token) {
        cartRepository.getCart(token).enqueue(new Callback<List<CartItem>>() {
            @Override
            public void onResponse(Call<List<CartItem>> call, Response<List<CartItem>> response) {
                if (response.isSuccessful()) {
                    cartItems.setValue(response.body());
                } else {
                    error.setValue("Không lấy được giỏ hàng");
                }
            }

            @Override
            public void onFailure(Call<List<CartItem>> call, Throwable t) {
                error.setValue(t.getMessage());
            }
        });
    }
}

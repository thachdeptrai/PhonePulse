// com.phoneapp.phonepulse.ui.cart.CartActivity.java
package com.phoneapp.phonepulse.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar; // Add ProgressBar
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phoneapp.phonepulse.Adapter.CartAdapter;
import com.phoneapp.phonepulse.Adapter.ProductGridAdapter;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.models.Product;
import com.phoneapp.phonepulse.request.CartItem; // This is the CartItem from the backend response
import com.phoneapp.phonepulse.ui.home.HomeActivity;
import com.phoneapp.phonepulse.viewmodel.CartViewModel; // Import CartViewModel

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemActionCallback { // Implement the interface

    private Toolbar toolbarCart;
    private RecyclerView rvCartItems;
    private RecyclerView rvRecommendations;
    private TextView tvTotalPrice;
    private Button btnCheckout;
    private CheckBox cbSelectAll;
    private ImageView ivDeleteSelected;
    private LinearLayout emptyCartView;
    private Button btnShopNow;
    private LinearLayout cartActionBar;
    private ProgressBar progressBar; // Added ProgressBar

    private CartAdapter cartAdapter;
    private ProductGridAdapter recommendationAdapter;
    private List<Product> recommendationList; // No need for cartItemList here, ViewModel manages it

    private CartViewModel cartViewModel;
    private NumberFormat numberFormat; // For formatting total price

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        toolbarCart = findViewById(R.id.toolbar_cart);
        setupToolbar();

        rvCartItems = findViewById(R.id.rv_cart_items);
        rvRecommendations = findViewById(R.id.rv_recommendations);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        btnCheckout = findViewById(R.id.btn_checkout);
        cbSelectAll = findViewById(R.id.cb_select_all);
        ivDeleteSelected = findViewById(R.id.iv_delete_selected);
        emptyCartView = findViewById(R.id.empty_cart_view);
        btnShopNow = findViewById(R.id.btn_shop_now);
        cartActionBar = findViewById(R.id.cart_action_bar);
        progressBar = findViewById(R.id.progressBar); // Assuming you have a ProgressBar in your layout

        // --- Setup ViewModel ---
        CartViewModel.Factory factory = new CartViewModel.Factory(getApplication());
        cartViewModel = new ViewModelProvider(this, factory).get(CartViewModel.class);

        // --- Setup Cart RecyclerView ---
        // Pass an empty list initially, ViewModel will update it
        cartAdapter = new CartAdapter(this, new ArrayList<>(), this); // Pass 'this' as callback
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);

        // --- Setup Recommendation RecyclerView ---
        recommendationList = new ArrayList<>();
        // TODO: You'll need to fetch real recommendations from another ViewModel/Repository
        recommendationAdapter = new ProductGridAdapter(this, recommendationList);
        rvRecommendations.setLayoutManager(new GridLayoutManager(this, 2));
        rvRecommendations.setAdapter(recommendationAdapter);

        // --- Observe ViewModel LiveData ---
        cartViewModel.getCartItems().observe(this, cartItems -> {
            cartAdapter.setCartItems(cartItems); // Update adapter with new data
            updateCartTotal(cartItems); // Update total price
            checkEmptyCartState(cartItems); // Check empty state based on observed data
        });

        cartViewModel.getError().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(CartActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        cartViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            // Optionally, disable other UI elements when loading
            rvCartItems.setAlpha(isLoading ? 0.5f : 1.0f);
            rvRecommendations.setAlpha(isLoading ? 0.5f : 1.0f);
            btnCheckout.setEnabled(!isLoading);
        });


        // --- Button Listeners ---
        btnShopNow.setOnClickListener(v -> {
            Intent homeIntent = new Intent(CartActivity.this, HomeActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
        });

        // Fetch cart data when activity starts
        cartViewModel.fetchCart();
    }

    /**
     * Thiết lập Toolbar làm ActionBar và cấu hình nút quay lại và tiêu đề.
     */
    private void setupToolbar() {
        setSupportActionBar(toolbarCart);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Giỏ hàng của bạn");
        }
        toolbarCart.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void checkEmptyCartState(List<CartItem> currentCartItems) {
        if (currentCartItems == null || currentCartItems.isEmpty()) {
            emptyCartView.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.GONE);
            cartActionBar.setVisibility(View.GONE);
            findViewById(R.id.bottom_checkout_bar).setVisibility(View.GONE);
        } else {
            emptyCartView.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
            cartActionBar.setVisibility(View.VISIBLE);
            findViewById(R.id.bottom_checkout_bar).setVisibility(View.VISIBLE);
        }
    }

    private void updateCartTotal(List<CartItem> cartItems) {
        double total = 0;
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                total += item.getItemTotalPrice(); // Use the helper method in CartItem
            }
        }
        tvTotalPrice.setText("₫" + numberFormat.format(total));
    }

    // --- CartAdapter.CartItemActionCallback Implementations ---
    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        cartViewModel.updateCartItemQuantity(item, newQuantity);
    }

    @Override
    public void onRemoveItem(CartItem item) {
        cartViewModel.removeCartItem(item);
    }

    @Override
    public void onCartTotalChanged() {
        // This callback is triggered by the adapter, but the ViewModel's LiveData observation
        // already handles updating the total, so this method just makes sure.
        // It's effectively redundant if the LiveData observer is always active.
        // If you were to not use LiveData's direct observation for total, this would be needed.
        // For now, the ViewModel's LiveData takes precedence.
    }
}
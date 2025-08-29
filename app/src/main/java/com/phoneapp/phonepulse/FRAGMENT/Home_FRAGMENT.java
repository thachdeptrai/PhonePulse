package com.phoneapp.phonepulse.FRAGMENT;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.phoneapp.phonepulse.Adapter.BannerAdapter;
import com.phoneapp.phonepulse.Adapter.CartAdapter;
import com.phoneapp.phonepulse.Adapter.ItemProduct_ADAPTER;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.VIEW.Cart_Activity;
import com.phoneapp.phonepulse.VIEW.DashBoar_Activity;
import com.phoneapp.phonepulse.VIEW.ProductDetailActivity;
import com.phoneapp.phonepulse.VIEW.SearchProductActivity;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Cart;
import com.phoneapp.phonepulse.models.Product;
import com.phoneapp.phonepulse.models.Variant;
import com.phoneapp.phonepulse.request.CartItem;
import com.phoneapp.phonepulse.request.CartRequest;
import com.phoneapp.phonepulse.request.DataConverter;
import com.phoneapp.phonepulse.request.ProductGirdItem;
import com.phoneapp.phonepulse.utils.Constants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home_FRAGMENT extends Fragment implements ItemProduct_ADAPTER.OnProductActionListener {
    private static final String TAG = "Home_FRAGMENT";

    private ViewPager2 vpBanner;
    private RecyclerView rvProductList;
    private EditText etSearchProduct;
    private ImageView ivCartIcon;
    private View frameCart;
    private TextView tv_cart_badge;

    private BannerAdapter bannerAdapter;
    private ItemProduct_ADAPTER productListAdapter;

    private List<ProductGirdItem> allProductsGridItems = new ArrayList<>();
    private List<ProductGirdItem> displayedProductsGridItems = new ArrayList<>();

    private ApiService apiService;
    private String authToken;
    private int cartItemCount = 0;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList = new ArrayList<>();

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Home_FRAGMENT() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Fragment creating view.");
        View fragmentView = inflater.inflate(R.layout.home_fragment, container, false);

        authToken = Constants.getToken(requireContext());
        if (authToken != null) {
            Log.d(TAG, "Auth Token loaded successfully.");
        } else {
            Log.w(TAG, "No auth token found. User might not be logged in or token expired.");
        }

        initFragmentViews(fragmentView);
        initApiService();
        setupRecyclerViews();
        setupViewPager();

        Log.d(TAG, "onCreateView: View created and data loading initiated.");
        return fragmentView;
    }

    private void updateCartBadge() {
        if (cartItemCount > 0) {
            tv_cart_badge.setVisibility(View.VISIBLE);
            tv_cart_badge.setText(String.valueOf(cartItemCount));
        } else {
            tv_cart_badge.setVisibility(View.GONE);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: Fragment view created.");

        if (getActivity() instanceof DashBoar_Activity) {
            DashBoar_Activity hostingActivity = (DashBoar_Activity) getActivity();

            etSearchProduct = hostingActivity.findViewById(R.id.et_search_product);
            ivCartIcon = hostingActivity.findViewById(R.id.iv_cart_icon);
            frameCart = hostingActivity.findViewById(R.id.frame_cart);
            cartAdapter = new CartAdapter(cartItemList);

            if (etSearchProduct != null) {
                setupSearchFunction();
                Log.d(TAG, "Search function setup.");
            }

            if (frameCart != null) {
                frameCart.setOnClickListener(v -> {
                    Toast.makeText(hostingActivity, "Đang mở giỏ hàng...", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Cart icon clicked. Navigating to Cart_Activity.");
                    Intent cartIntent = new Intent(requireContext(), Cart_Activity.class);
                    startActivity(cartIntent);
                });
            }
        }

        fetchProductData();
    }

    private void initFragmentViews(View view) {
        vpBanner = view.findViewById(R.id.vp_banner);
        rvProductList = view.findViewById(R.id.rv_product_list);
        tv_cart_badge = view.findViewById(R.id.tv_cart_badge);
    }

    private void setupRecyclerViews() {
        productListAdapter = new ItemProduct_ADAPTER(requireContext(), displayedProductsGridItems);
        productListAdapter.setOnProductActionListener(this);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        rvProductList.setLayoutManager(gridLayoutManager);
        rvProductList.setAdapter(productListAdapter);
        rvProductList.setNestedScrollingEnabled(false);
    }

    private void setupViewPager() {

    }

    private void setupSearchFunction() {
        if (etSearchProduct != null) {
            etSearchProduct.setFocusable(false);
            etSearchProduct.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), SearchProductActivity.class);
                intent.putExtra("product_list", (Serializable) allProductsGridItems);
                startActivity(intent);
            });
        }
    }

    private void initApiService() {
        apiService = RetrofitClient.getApiService(authToken);
    }

    private void fetchProductData() {
        if (apiService == null) {
            showError("API Service is not initialized. Please log in.");
            return;
        }

        Log.d(TAG, "Fetching all products from API...");
        apiService.getAllProductsRaw().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> rawProducts = response.body();
                    String rawProductsJson = gson.toJson(rawProducts);

                    allProductsGridItems = DataConverter.convertProductsToGridItems(rawProducts);
                    displayedProductsGridItems.clear();
                    displayedProductsGridItems.addAll(allProductsGridItems);

                    productListAdapter.notifyDataSetChanged();

                    if (displayedProductsGridItems.isEmpty()) {
                        Toast.makeText(requireContext(), "Không tìm thấy sản phẩm nào.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMessage = "Failed to fetch products: " + response.code();
                    showError(errorMessage);
                    Log.e(TAG, "API call failed: " + errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                showError("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Showing Toast error: " + message);
        }
    }

    // ================== IMPLEMENT LISTENER ==================

    @Override
    public void onAddToCartClick(ProductGirdItem item) {
        if (isAdded() && getContext() != null) {
            if (item.get_id() == null || item.getVariant_id() == null) {
                Toast.makeText(requireContext(), "Không thể thêm sản phẩm này vào giỏ hàng (thiếu ID).", Toast.LENGTH_SHORT).show();
                return;
            }
            fetchVariantAndAddToCart(item.get_id(), item.getVariant_id(), 1);
        }
    }

    @Override
    public void onItemClick(ProductGirdItem item) {
        if (isAdded() && getContext() != null) {
            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra(Constants.PRODUCT_ID, item.get_id());
            intent.putExtra(Constants.VARIANT_ID, item.getVariant_id());
            startActivity(intent);
        }
    }

    @Override
    public void onVariantSelected(ProductGirdItem productItem, Variant selectedVariant) {
        if (selectedVariant != null) {
            productItem.setVariant_id(selectedVariant.getId());
            Toast.makeText(requireContext(),
                    "Đã chọn biến thể: " + selectedVariant.getSize(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    // ================== CART HANDLING ==================

    private void fetchVariantAndAddToCart(String productId, String variantId, int addedQuantity) {
        if (authToken == null || authToken.isEmpty()) {
            Toast.makeText(requireContext(), "Bạn cần đăng nhập để thêm sản phẩm vào giỏ hàng.", Toast.LENGTH_LONG).show();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(authToken);

        apiService.getCart().enqueue(new Callback<ApiResponse<Cart>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Cart>> call, @NonNull Response<ApiResponse<Cart>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Cart currentCart = response.body().getData();

                    int existingQuantity = 0;
                    if (currentCart.getItems() != null) {
                        for (CartItem item : currentCart.getItems()) {
                            if (item.getVariant() != null && variantId.equals(item.getVariant().getId())) {
                                existingQuantity = item.getQuantity();
                                break;
                            }
                        }
                    }

                    int finalExistingQuantity = existingQuantity;
                    apiService.getVariantForProductById(productId, variantId).enqueue(new Callback<Variant>() {
                        @Override
                        public void onResponse(@NonNull Call<Variant> call, @NonNull Response<Variant> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Variant variant = response.body();
                                int stockQuantity = variant.getQuantity();

                                int totalRequestedQuantity = finalExistingQuantity + addedQuantity;
                                if (totalRequestedQuantity > stockQuantity) {
                                    Toast.makeText(requireContext(),
                                            "Không thể thêm. Số lượng vượt quá tồn kho (" + stockQuantity + ").",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    callAddToCartApi(productId, variantId, addedQuantity);
                                }
                            } else {
                                Toast.makeText(requireContext(), "Không thể lấy thông tin biến thể sản phẩm.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Variant> call, @NonNull Throwable t) {
                            Toast.makeText(requireContext(), "Lỗi mạng khi lấy biến thể: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                } else {
                    Toast.makeText(requireContext(), "Không thể lấy giỏ hàng hiện tại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Cart>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Lỗi mạng khi lấy giỏ hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void callAddToCartApi(String productId, String variantId, int quantity) {
        CartRequest.AddToCart request = new CartRequest.AddToCart(productId, variantId, quantity);
        apiService.addToCart(request).enqueue(new Callback<ApiResponse<Cart>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Cart>> call, @NonNull Response<ApiResponse<Cart>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(requireContext(), "Thêm vào giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Lỗi khi thêm vào giỏ hàng.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Cart>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Lỗi mạng khi thêm vào giỏ hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.phoneapp.phonepulse.FRAGMENT;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.phoneapp.phonepulse.Adapter.BannerAdapter;
import com.phoneapp.phonepulse.Adapter.ItemProduct_ADAPTER;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Cart;
import com.phoneapp.phonepulse.models.Product;
import com.phoneapp.phonepulse.request.CartRequest;
import com.phoneapp.phonepulse.request.DataConverter;
import com.phoneapp.phonepulse.request.ProductGirdItem;
import com.phoneapp.phonepulse.utils.Constants;
import com.phoneapp.phonepulse.VIEW.DashBoar_Activity;
import com.phoneapp.phonepulse.ui.cart.Cart_Activity;
import com.phoneapp.phonepulse.ui.product.ProductDetailActivity;

import com.google.gson.Gson; // <-- THÊM IMPORT NÀY
import com.google.gson.GsonBuilder; // <-- THÊM IMPORT NÀY
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home_FRAGMENT extends Fragment implements ItemProduct_ADAPTER.OnItemClickListener {
    private static final String TAG = "Home_FRAGMENT";

    private ViewPager2 vpBanner;
    private RecyclerView rvProductList;
    private RecyclerView rvFlashSale;
    private EditText etSearchProduct;
    private ImageView ivCartIcon;
    private View frameCart;

    private BannerAdapter bannerAdapter;
    private ItemProduct_ADAPTER productListAdapter;

    private List<ProductGirdItem> allProductsGridItems = new ArrayList<>();
    private List<ProductGirdItem> displayedProductsGridItems = new ArrayList<>();

    private List<String> bannerImages = new ArrayList<>();

    private ApiService apiService;
    private String authToken;

    // Khởi tạo Gson ở đây
    private Gson gson = new GsonBuilder().setPrettyPrinting().create(); // <-- THÊM DÒNG NÀY

    public Home_FRAGMENT() {
        // Required empty public constructor
    }

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: Fragment view created.");

        if (getActivity() instanceof DashBoar_Activity) {
            DashBoar_Activity hostingActivity = (DashBoar_Activity) getActivity();

            etSearchProduct = hostingActivity.findViewById(R.id.et_search_product);
            ivCartIcon = hostingActivity.findViewById(R.id.iv_cart_icon);
            frameCart = hostingActivity.findViewById(R.id.frame_cart);

            if (etSearchProduct != null) {
                setupSearchFunction();
                Log.d(TAG, "Search function setup.");
            } else {
                Log.e(TAG, "et_search_product not found in DashBoar_Activity's layout. Check its ID.");
            }

            if (frameCart != null) {
                frameCart.setOnClickListener(v -> {
                    Toast.makeText(hostingActivity, "Đang mở giỏ hàng...", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Cart icon clicked. Navigating to Cart_Activity.");
                    Intent cartIntent = new Intent(requireContext(), Cart_Activity.class);
                    startActivity(cartIntent);
                });
            } else {
                Log.e(TAG, "frame_cart not found in DashBoar_Activity's layout. Check its ID.");
            }

        } else {
            Log.w(TAG, "Home_FRAGMENT is not attached to DashBoar_Activity. Cannot access Toolbar elements.");
        }

        fetchProductData();
    }

    private void initFragmentViews(View view) {
        vpBanner = view.findViewById(R.id.vp_banner);
        rvProductList = view.findViewById(R.id.rv_product_list);
        Log.d(TAG, "Fragment views initialized.");
    }

    private void setupRecyclerViews() {
        productListAdapter = new ItemProduct_ADAPTER(requireContext(), new ArrayList<ProductGirdItem>());
        productListAdapter.setOnItemClickListener(this);

        // GridLayoutManager với 2 hàng, cuộn theo chiều ngang (trái sang phải)
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 1, RecyclerView.HORIZONTAL, false);

        rvProductList.setLayoutManager(gridLayoutManager);
        rvProductList.setAdapter(productListAdapter);
        rvProductList.setNestedScrollingEnabled(false);

        Log.d(TAG, "RecyclerViews and Adapters setup.");
    }


    private void setupViewPager() {
        // Sử dụng URL placeholder để tránh lỗi 404 từ tgdd.vn
        bannerImages = Arrays.asList(
                "https://placehold.co/1000x400/png",
                "https://placehold.co/1000x400/png",
                "https://placehold.co/1000x400/png"
        );
        bannerAdapter = new BannerAdapter(requireContext(), bannerImages);
        vpBanner.setAdapter(bannerAdapter);
        Log.d(TAG, "ViewPager setup with " + bannerImages.size() + " banners.");
    }

    private void setupSearchFunction() {
        if (etSearchProduct != null) {
            etSearchProduct.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }
                @Override
                public void afterTextChanged(Editable s) {
                    filterProducts(s.toString());
                }
            });
            Log.d(TAG, "Search EditText TextWatcher added.");
        }
    }

    private void filterProducts(String query) {
        String lowerCaseQuery = query.toLowerCase(Locale.getDefault());
        List<ProductGirdItem> filteredList = new ArrayList<>();

        if (lowerCaseQuery.isEmpty()) {
            filteredList.addAll(allProductsGridItems);
        } else {
            for (ProductGirdItem item : allProductsGridItems) {
                if (item.getProduct_name() != null && item.getProduct_name().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    filteredList.add(item);
                }
            }
        }
        displayedProductsGridItems.clear();
        displayedProductsGridItems.addAll(filteredList);
        productListAdapter.setData(displayedProductsGridItems);
        Log.d(TAG, "Product list filtered. Displaying " + displayedProductsGridItems.size() + " items.");
    }

    private void initApiService() {
        apiService = RetrofitClient.getApiService(authToken);
        Log.d(TAG, "ApiService initialized with token.");
    }

    private void fetchProductData() {
        if (apiService == null) {
            showError("API Service is not initialized. Please log in.");
            Log.e(TAG, "fetchProductData: ApiService is null.");
            return;
        }

        Log.d(TAG, "Fetching all products from API...");
        apiService.getAllProductsRaw().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> rawProducts = response.body();
                    Log.d(TAG, "API call successful. Received " + rawProducts.size() + " raw products.");
                    // Chuyển đổi List<Product> thành chuỗi JSON để in ra Logcat
                    String rawProductsJson = gson.toJson(rawProducts); // <-- DÒNG ĐÃ THAY ĐỔI
                    Log.d(TAG, "Raw Products from API (JSON): " + rawProductsJson); // <-- DÒNG ĐÃ THAY ĐỔI

                    allProductsGridItems = DataConverter.convertProductsToGridItems(rawProducts);
                    displayedProductsGridItems.clear();
                    displayedProductsGridItems.addAll(allProductsGridItems);

                    productListAdapter.setData(displayedProductsGridItems);
                    Log.d(TAG, "Product list updated with " + displayedProductsGridItems.size() + " grid items.");

                    if (displayedProductsGridItems.isEmpty()) {
                        Toast.makeText(requireContext(), "Không tìm thấy sản phẩm nào.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMessage = "Failed to fetch products: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body", e);
                        }
                    }
                    showError(errorMessage);
                    Log.e(TAG, "API call failed: " + errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                showError("Lỗi kết nối mạng: " + t.getMessage());
                Log.e(TAG, "API call failed (network error): ", t);
            }
        });
    }

    private void showError(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Showing Toast error: " + message);
        }
    }

    @Override
    public void onAddToCartClick(ProductGirdItem item) {
        if (isAdded() && getContext() != null) {
            Log.d(TAG, "Add to cart clicked for: " + item.getProduct_name() + " with Product ID: " + item.get_id() + ", Variant ID: " + item.getVariant_id());

            if (item.get_id() == null || item.getVariant_id() == null) {
                Toast.makeText(requireContext(), "Không thể thêm sản phẩm này vào giỏ hàng (thiếu ID).", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Product ID or Variant ID is null for item: " + item.getProduct_name());
                return;
            }

            callAddToCartApi(item.get_id(), item.getVariant_id(), 1);
        }
    }

    private void callAddToCartApi(String productId, String variantId, int quantity) {
        if (authToken == null || authToken.isEmpty()) {
            Toast.makeText(requireContext(), "Bạn cần đăng nhập để thêm sản phẩm vào giỏ hàng.", Toast.LENGTH_LONG).show();
            return;
        }

        apiService = RetrofitClient.getApiService(authToken);

        CartRequest.AddToCart request = new CartRequest.AddToCart(productId, variantId, quantity);

        Toast.makeText(requireContext(), "Đang thêm sản phẩm vào giỏ hàng...", Toast.LENGTH_SHORT).show();

        apiService.addToCart(request).enqueue(new Callback<ApiResponse<Cart>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<com.phoneapp.phonepulse.models.Cart>> call, @NonNull Response<ApiResponse<com.phoneapp.phonepulse.models.Cart>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(requireContext(), "Thêm vào giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Product added to cart successfully. Current cart: " + response.body().getData());
                } else {
                    String errorMsg = "Lỗi khi thêm vào giỏ hàng.";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    } else if (response.errorBody() != null) {
                        try {
                            errorMsg += " " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body for add to cart", e);
                        }
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Add to cart API failed: " + response.code() + " - " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<com.phoneapp.phonepulse.models.Cart>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Lỗi mạng khi thêm vào giỏ hàng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Add to cart network failure: ", t);
            }
        });
    }


    @Override
    public void onItemClick(ProductGirdItem item) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(requireContext(), "Đang mở chi tiết: " + item.getProduct_name(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Item clicked: " + item.getProduct_name() + " Product ID: " + item.get_id() + ", Variant ID: " + item.getVariant_id());

            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);

            if (item.get_id() != null) {
                intent.putExtra(Constants.PRODUCT_ID, item.get_id());
            } else {
                Log.e(TAG, "Product ID is null for item: " + item.getProduct_name());
            }

            if (item.getVariant_id() != null) {
                intent.putExtra(Constants.VARIANT_ID, item.getVariant_id());
            } else {
                Log.w(TAG, "Variant ID is null for item: " + item.getProduct_name() + ". Proceeding without variant ID.");
            }

            startActivity(intent);
        }
    }
}
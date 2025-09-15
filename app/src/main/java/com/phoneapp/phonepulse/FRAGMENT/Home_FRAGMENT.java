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
import com.phoneapp.phonepulse.VIEW.Cart_Activity;
import com.phoneapp.phonepulse.VIEW.DashBoar_Activity;
import com.phoneapp.phonepulse.VIEW.ProductDetailActivity;
import com.phoneapp.phonepulse.VIEW.SearchProductActivity;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Product;
import com.phoneapp.phonepulse.models.Variant;
import com.phoneapp.phonepulse.request.CartItem;
import com.phoneapp.phonepulse.request.DataConverter;
import com.phoneapp.phonepulse.request.ProductGirdItem;
import com.phoneapp.phonepulse.utils.Constants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

        initFragmentViews(fragmentView);
        initApiService();
        setupRecyclerViews();
        setupViewPager();

        return fragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof DashBoar_Activity) {
            DashBoar_Activity hostingActivity = (DashBoar_Activity) getActivity();

            etSearchProduct = hostingActivity.findViewById(R.id.et_search_product);
            ivCartIcon = hostingActivity.findViewById(R.id.iv_cart_icon);
            frameCart = hostingActivity.findViewById(R.id.frame_cart);
            cartAdapter = new CartAdapter(cartItemList);

            if (etSearchProduct != null) {
                setupSearchFunction();
            }

            if (frameCart != null) {
                frameCart.setOnClickListener(v -> {
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
        // banner setup nếu cần
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
            showError("API Service is not initialized.");
            return;
        }

        apiService.getAllProductsRaw().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> rawProducts = response.body();
                    allProductsGridItems = DataConverter.convertProductsToGridItems(rawProducts);
                    displayedProductsGridItems.clear();
                    displayedProductsGridItems.addAll(allProductsGridItems);

                    productListAdapter.notifyDataSetChanged();
                } else {
                    showError("Không thể tải sản phẩm. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                showError("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        if (isAdded()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    // ================== LISTENER ==================
    @Override
    public void onAddToCartClick(ProductGirdItem item) {
        // 👉 Giờ chỉ mở ProductDetailActivity
        Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
        intent.putExtra(Constants.PRODUCT_ID, item.get_id());
        intent.putExtra(Constants.VARIANT_ID, item.getVariant_id());
        startActivity(intent);
    }

    @Override
    public void onItemClick(ProductGirdItem item) {
        // 👉 Cũng mở ProductDetailActivity
        Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
        intent.putExtra(Constants.PRODUCT_ID, item.get_id());
        intent.putExtra(Constants.VARIANT_ID, item.getVariant_id());
        startActivity(intent);
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
}

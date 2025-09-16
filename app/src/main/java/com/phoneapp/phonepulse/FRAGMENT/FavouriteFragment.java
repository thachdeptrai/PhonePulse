package com.phoneapp.phonepulse.FRAGMENT;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
// import androidx.recyclerview.widget.LinearLayoutManager; // Not used for GridLayout
import androidx.recyclerview.widget.RecyclerView;

import com.phoneapp.phonepulse.Adapter.FavouriteProductAdapter;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.VIEW.ProductDetailActivity;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Favourite; // Import model Favourite mới
import com.phoneapp.phonepulse.models.Product;
import com.phoneapp.phonepulse.models.Variant;
import com.phoneapp.phonepulse.utils.Constants;

import java.util.ArrayList;
// import java.util.Collections; // Bỏ nếu không dùng sort phức tạp
// import java.util.Comparator; // Bỏ nếu không dùng sort phức tạp
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavouriteFragment extends Fragment implements FavouriteProductAdapter.OnItemClickListener {

    private static final String TAG = "FavouriteFragment";

    private RecyclerView rvFavouriteItems;
    private TextView tvEmptyFavourites;
    private FavouriteProductAdapter favouriteAdapter;

    private List<Product> fullyDetailedFavouriteProductList; // Vẫn lưu Product đầy đủ chi tiết
    // private List<Product> initialFavouriteProducts; // Không cần nữa, vì Favourite model chứa Product cơ bản

    private ApiService apiService;
    private String authToken;
    private AtomicInteger detailFetchCounter;

    public FavouriteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null) {
            authToken = Constants.getToken(requireContext());
            apiService = RetrofitClient.getApiService(authToken);
        }
        fullyDetailedFavouriteProductList = new ArrayList<>();
        // initialFavouriteProducts = new ArrayList<>(); // Bỏ dòng này
        detailFetchCounter = new AtomicInteger(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favourite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFavouriteItems = view.findViewById(R.id.rv_favourite_items);
        tvEmptyFavourites = view.findViewById(R.id.tv_empty_favourites);

        setupRecyclerView();
        // Việc gọi loadInitialFavouriteList() sẽ được xử lý trong onResume
    }

    private void setupRecyclerView() {
        if (getContext() == null) {
            return;
        }
        favouriteAdapter = new FavouriteProductAdapter(requireContext(), this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvFavouriteItems.setLayoutManager(gridLayoutManager);
        rvFavouriteItems.setAdapter(favouriteAdapter);
        rvFavouriteItems.setHasFixedSize(true);
    }

    private void loadInitialFavouriteList() {
        if (authToken == null || authToken.isEmpty()) {
            displayLoginPrompt();
            updateAdapterWithFinalList(); // Cập nhật adapter với danh sách trống
            return;
        }

        tvEmptyFavourites.setText("Đang tải danh sách yêu thích...");
        tvEmptyFavourites.setVisibility(View.VISIBLE);
        rvFavouriteItems.setVisibility(View.GONE);
        fullyDetailedFavouriteProductList.clear();
        // initialFavouriteProducts.clear(); // Bỏ nếu không dùng
        updateAdapterWithFinalList();


        // THAY ĐỔI KIỂU DỮ LIỆU CỦA CALL VÀ CALLBACK
        apiService.getFavourites().enqueue(new Callback<ApiResponse<List<Favourite>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Favourite>>> call, @NonNull Response<ApiResponse<List<Favourite>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Favourite> favouriteEntries = response.body().getData(); // Danh sách các đối tượng Favourite

                    if (favouriteEntries != null && !favouriteEntries.isEmpty()) {
                        // Không cần initialFavouriteProducts nữa nếu dùng Favourite model trực tiếp
                        // initialFavouriteProducts.clear(); // Xóa cũ nếu có
                        // for (Favourite entry : favouriteEntries) { // Chuyển đổi nếu cần
                        //    if (entry.getProductDetails() != null) initialFavouriteProducts.add(entry.getProductDetails());
                        // }

                        detailFetchCounter.set(favouriteEntries.size());

                        if (detailFetchCounter.get() == 0) {
                            updateUIAfterAllDetailsFetched();
                            return;
                        }

                        for (Favourite favouriteEntry : favouriteEntries) {
                            Product basicProductInfo = favouriteEntry.getProductDetails(); // Lấy Product cơ bản
                            if (basicProductInfo != null && basicProductInfo.getId() != null) {
                                String actualProductId = basicProductInfo.getId(); // Đây là ID của Product
                                // Giữ originalIndex có thể không cần thiết nữa nếu không sort phức tạp
                                fetchFullProductDetails(actualProductId, favouriteEntries.indexOf(favouriteEntry));
                            } else {
                                decrementCounterAndCheckCompletion();
                            }
                        }
                    } else {
                        detailFetchCounter.set(0);
                        updateUIAfterAllDetailsFetched(); // Danh sách yêu thích trống
                    }
                } else {
                    tvEmptyFavourites.setText("Không thể tải danh sách yêu thích. Vui lòng thử lại.");
                    detailFetchCounter.set(0);
                    updateUIAfterAllDetailsFetched();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Favourite>>> call, @NonNull Throwable t) {
                tvEmptyFavourites.setText("Lỗi mạng. Vui lòng kiểm tra kết nối và thử lại.");
                detailFetchCounter.set(0);
                updateUIAfterAllDetailsFetched();
            }
        });
    }

    private void fetchFullProductDetails(String productId, final int originalIndex) {
        apiService.getProductById(productId).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(@NonNull Call<Product> call, @NonNull Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Product fullProduct = response.body();
                    // Nếu muốn giữ thứ tự ban đầu, bạn cần một cách để map `originalIndex`
                    // vào `fullProduct` và sort sau. Hiện tại, nó sẽ thêm vào cuối danh sách.
                    synchronized (fullyDetailedFavouriteProductList) {
                        fullyDetailedFavouriteProductList.add(fullProduct);
                    }
                }
                decrementCounterAndCheckCompletion();
            }

            @Override
            public void onFailure(@NonNull Call<Product> call, @NonNull Throwable t) {
                decrementCounterAndCheckCompletion();
            }
        });
    }

    private void decrementCounterAndCheckCompletion() {
        if (detailFetchCounter.decrementAndGet() <= 0) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::updateUIAfterAllDetailsFetched);
            }
        }
    }

    private void updateUIAfterAllDetailsFetched() {

        updateAdapterWithFinalList();

        if (fullyDetailedFavouriteProductList.isEmpty()) {
            tvEmptyFavourites.setText("Danh sách yêu thích của bạn đang trống.");
            tvEmptyFavourites.setVisibility(View.VISIBLE);
            rvFavouriteItems.setVisibility(View.GONE);
        } else {
            tvEmptyFavourites.setVisibility(View.GONE);
            rvFavouriteItems.setVisibility(View.VISIBLE);
        }
    }

    private void updateAdapterWithFinalList() {
        if (favouriteAdapter != null) {
            List<Product> newDisplayList = new ArrayList<>(fullyDetailedFavouriteProductList);
            favouriteAdapter.setData(newDisplayList);
        }
    }


    @Override
    public void onItemClick(Product productFromAdapter, String variantIdFromAdapter) {
        if (productFromAdapter == null || productFromAdapter.getId() == null) {
            Toast.makeText(getContext(), "Không thể mở chi tiết sản phẩm.", Toast.LENGTH_SHORT).show();
            return;
        }

        String finalVariantId = variantIdFromAdapter;
        if (finalVariantId == null) {
            if (productFromAdapter.getVariants() != null && !productFromAdapter.getVariants().isEmpty()) {
                Variant firstVariant = productFromAdapter.getVariants().get(0);
                if (firstVariant != null && firstVariant.getId() != null) {
                    finalVariantId = firstVariant.getId();
                }
            }
        }

        if (finalVariantId == null) {
            Toast.makeText(getContext(), "Sản phẩm này không có thông tin biến thể hợp lệ.", Toast.LENGTH_SHORT).show();
            // Cân nhắc mở chỉ với Product ID nếu ProductDetailActivity có thể xử lý
            // Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
            // intent.putExtra(Constants.PRODUCT_ID, productFromAdapter.getId());
            // startActivity(intent);
            return;
        }

        Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
        intent.putExtra(Constants.PRODUCT_ID, productFromAdapter.getId());
        intent.putExtra(Constants.VARIANT_ID, finalVariantId);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Đảm bảo các điều kiện để tải dữ liệu được kiểm tra đúng
        if (getContext() == null) {
            return; // Không làm gì nếu context null
        }

        if (authToken == null || authToken.isEmpty()) {
            authToken = Constants.getToken(requireContext());
        }

        if (apiService == null && authToken != null && !authToken.isEmpty()) {
            apiService = RetrofitClient.getApiService(authToken);
        }

        if (apiService != null && authToken != null && !authToken.isEmpty()) {
            loadInitialFavouriteList();
        } else {
            displayLoginPrompt();
        }
    }

    private void displayLoginPrompt() {
        tvEmptyFavourites.setText("Bạn cần đăng nhập để xem danh sách yêu thích.");
        tvEmptyFavourites.setVisibility(View.VISIBLE);
        rvFavouriteItems.setVisibility(View.GONE);
        fullyDetailedFavouriteProductList.clear();
        // initialFavouriteProducts.clear(); // Bỏ nếu không dùng
        if (favouriteAdapter != null) { // Kiểm tra adapter trước khi setData
            favouriteAdapter.setData(new ArrayList<>()); // Truyền danh sách trống mới
        }
    }
}


package com.phoneapp.phonepulse.FRAGMENT;

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
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.models.Product; // Import Product model for raw product fetching
import com.phoneapp.phonepulse.models.Variant;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.utils.Constants;
import com.phoneapp.phonepulse.VIEW.DashBoar_Activity; // Ensure this path is correct for your DashBoar_Activity

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch; // For synchronizing multiple API calls
import java.util.concurrent.atomic.AtomicInteger; // For counting successful fetches

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home_FRAGMENT extends Fragment {
    private static final String TAG = "Home_FRAGMENT";

    // UI elements
    private ViewPager2 vpBanner;
    private RecyclerView rvProductList;
    private EditText etSearchProduct;
    private ImageView ivCartIcon;
    private View frameCart;

    // Adapters
    private BannerAdapter bannerAdapter;
    private ItemProduct_ADAPTER productListAdapter;

    // Data lists
    // These lists will hold the variants for display and filtering.
    // They are not synchronized lists as all modifications will be done on the UI thread
    // after the background fetches are complete.
    private List<Variant> allVariants = new ArrayList<>(); // Stores all variants fetched from API for filtering
    private List<Variant> mainDisplayVariants = new ArrayList<>(); // List currently displayed in RecyclerView

    // Banner images
    private List<String> bannerImages = new ArrayList<>();

    // API service and authentication token
    private ApiService apiService;
    private String authToken;

    public Home_FRAGMENT() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Fragment creating view.");
        View fragmentView = inflater.inflate(R.layout.home_fragment, container, false);

        // Load authentication token from SharedPreferences
        authToken = Constants.getToken(requireContext());
        if (authToken != null) {
            Log.d(TAG, "Auth Token loaded successfully.");
        } else {
            Log.w(TAG, "No auth token found. User might not be logged in or token expired.");
            // Optionally, prompt user to log in or handle unauthorized access
        }

        // Initialize UI components from the fragment's view
        initFragmentViews(fragmentView);
        // Set up RecyclerViews and their adapters
        setupRecyclerViews();
        // Set up ViewPager for banners
        setupViewPager();
        // Initialize API service
        initApiService();
        // Load data from the API
        loadData();

        Log.d(TAG, "onCreateView: View created and data loading initiated.");
        return fragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: Fragment view created.");

        // Access toolbar elements from the hosting Activity (DashBoar_Activity)
        if (getActivity() instanceof DashBoar_Activity) {
            DashBoar_Activity hostingActivity = (DashBoar_Activity) getActivity();

            etSearchProduct = hostingActivity.findViewById(R.id.et_search_product);
            ivCartIcon = hostingActivity.findViewById(R.id.iv_cart_icon);
            frameCart = hostingActivity.findViewById(R.id.frame_cart);

            // Setup search functionality if EditText is found
            if (etSearchProduct != null) {
                setupSearchFunction();
                Log.d(TAG, "Search function setup.");
            } else {
                Log.e(TAG, "et_search_product not found in DashBoar_Activity's layout. Check its ID.");
            }

            // Setup cart icon click listener if View is found
            if (frameCart != null) {
                frameCart.setOnClickListener(v -> {
                    Toast.makeText(hostingActivity, "Giỏ hàng được nhấp!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Cart icon clicked.");
                    // TODO: Implement navigation to Cart Activity here
                });
            } else {
                Log.e(TAG, "frame_cart not found in DashBoar_Activity's layout. Check its ID.");
            }

        } else {
            Log.w(TAG, "Home_FRAGMENT is not attached to DashBoar_Activity. Cannot access Toolbar elements.");
        }
    }

    /**
     * Initializes the UI components of the fragment.
     * @param view The root view of the fragment.
     */
    private void initFragmentViews(View view) {
        vpBanner = view.findViewById(R.id.vp_banner);
        rvProductList = view.findViewById(R.id.rv_product_list);
        Log.d(TAG, "Fragment views initialized.");
    }

    /**
     * Sets up the RecyclerViews with their adapters and layout managers.
     */
    private void setupRecyclerViews() {
        // Initialize product list adapter with an empty list initially
        productListAdapter = new ItemProduct_ADAPTER(requireContext(), mainDisplayVariants);
        // Set up GridLayoutManager with 2 columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        rvProductList.setLayoutManager(gridLayoutManager);
        // Set the adapter to the RecyclerView
        rvProductList.setAdapter(productListAdapter);
        // Disable nested scrolling to prevent conflicts if RecyclerView is inside a ScrollView
        rvProductList.setNestedScrollingEnabled(false);
        Log.d(TAG, "RecyclerViews and Adapters setup.");
    }

    /**
     * Sets up the ViewPager2 for displaying banners.
     */
    private void setupViewPager() {
        // Define static banner image URLs (can be fetched from API in a real app)
        bannerImages = Arrays.asList(
                "https://via.placeholder.com/600x300/FF5733/FFFFFF?text=Banner+1",
                "https://via.placeholder.com/600x300/33FF57/FFFFFF?text=Banner+2",
                "https://via.placeholder.com/600x300/3357FF/FFFFFF?text=Banner+3"
        );
        // Initialize and set the banner adapter
        bannerAdapter = new BannerAdapter(requireContext(), bannerImages);
        vpBanner.setAdapter(bannerAdapter);
        Log.d(TAG, "ViewPager setup with " + bannerImages.size() + " banners.");
    }

    /**
     * Sets up the search functionality for the product list.
     */
    private void setupSearchFunction() {
        if (etSearchProduct != null) {
            etSearchProduct.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Not used
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Filter the product list as text changes
                    filterProductList(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Not used
                }
            });
            Log.d(TAG, "Search TextWatcher added.");
        }
    }

    /**
     * Initializes the Retrofit API service.
     */
    private void initApiService() {
        apiService = RetrofitClient.getApiService(authToken);
        Log.d(TAG, "ApiService initialized.");
    }

    /**
     * Initiates the data loading process for the fragment.
     */
    private void loadData() {
        Log.d(TAG, "loadData: Initiating API calls to load all products and their variants.");
        // Start loading products and their associated variants
        loadAllProductsAndVariants();
    }

    /**
     * Loads all raw Product objects from the API, then for each product,
     * fetches its associated variants. This approach is less efficient (N+1 problem)
     * but compatible with the current backend structure.
     */
    private void loadAllProductsAndVariants() {
        Log.d(TAG, "loadAllProductsAndVariants: Calling API service getAllProductsRaw().");

        // Call API to get all raw Product objects
        Call<List<Product>> call = apiService.getAllProductsRaw();

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body();
                    Log.d(TAG, "onResponse: API call successful. Received " + products.size() + " raw products.");

                    // Clear previous data from the lists before populating new data
                    allVariants.clear();
                    mainDisplayVariants.clear();

                    if (products.isEmpty()) {
                        Log.w(TAG, "onResponse: Received raw products list is EMPTY. No products to process.");
                        // If no products, update adapter with an empty list on UI thread
                        if (isAdded() && getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                productListAdapter.setData(new ArrayList<>()); // Pass a new empty list
                                Log.d(TAG, "Product list adapter updated with 0 items (empty products list).");
                            });
                        }
                        return; // Exit if no products to fetch variants for
                    }

                    // Use CountDownLatch to wait for all individual variant fetches to complete
                    final CountDownLatch latch = new CountDownLatch(products.size());
                    // List to collect all variants fetched from individual product calls
                    // No need for synchronizedList here as additions will be synchronized manually
                    final List<Variant> collectedVariants = new ArrayList<>();
                    // AtomicInteger to count successful variant fetches (for logging)
                    final AtomicInteger successfulFetches = new AtomicInteger(0);

                    // Iterate through each product and fetch its variants
                    for (Product product : products) {
                        if (product.getId() != null) {
                            // Call helper method to load variants for a single product
                            loadVariantsForSingleProduct(product.getId(), product.getName(),
                                    product.getProductImage() != null ? product.getProductImage().getImageUrl() : null,
                                    latch, collectedVariants, successfulFetches);
                        } else {
                            // If product ID is null, decrement latch to avoid deadlock
                            latch.countDown();
                            Log.e(TAG, "onResponse: Product ID is null for a product. Skipping variant fetch.");
                        }
                    }

                    // Start a new thread to wait for all variant fetches to complete
                    new Thread(() -> {
                        try {
                            latch.await(); // Wait until CountDownLatch reaches zero
                            Log.d(TAG, "All variant fetches completed. Total collected: " + collectedVariants.size() + " from " + successfulFetches.get() + " successful product variant fetches.");

                            // Ensure UI updates are done on the main thread
                            if (isAdded() && getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    // Populate main data lists with the collected variants
                                    allVariants.addAll(collectedVariants);
                                    mainDisplayVariants.addAll(collectedVariants);

                                    // Update the adapter with a NEW ArrayList instance of the collected variants
                                    // This prevents concurrent modification issues and ensures the adapter gets a fresh list.
                                    productListAdapter.setData(new ArrayList<>(collectedVariants));
                                    Log.d(TAG, "Final product list adapter updated with " + collectedVariants.size() + " items.");
                                });
                            }
                        } catch (InterruptedException e) {
                            Log.e(TAG, "CountDownLatch await interrupted: " + e.getMessage(), e);
                            Thread.currentThread().interrupt(); // Restore interrupt status
                        }
                    }).start();

                } else {
                    String errorMessage = "Failed to load raw products: " + response.code() + " - " + response.message();
                    Log.e(TAG, "onResponse: " + errorMessage);
                    showError("Không thể tải danh sách sản phẩm. Mã lỗi: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "onResponse: Error Body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e(TAG, "onResponse: Error parsing error body", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                String errorMessage = "Network error during raw product loading: " + t.getMessage();
                Log.e(TAG, "onFailure: " + errorMessage, t);
                showError("Lỗi kết nối mạng. Vui lòng thử lại.");
            }
        });
    }

    /**
     * Helper method to load variants for a single product.
     * Decrements the latch upon completion (success or failure).
     * @param productId The ID of the product.
     * @param productName The name of the product to assign to variants.
     * @param imageUrl The image URL of the product to assign to variants.
     * @param latch The CountDownLatch to decrement.
     * @param collectedVariants The shared list to add fetched variants to.
     * @param successfulFetches An AtomicInteger to count successful fetches.
     */
    private void loadVariantsForSingleProduct(String productId, String productName, String imageUrl,
                                              CountDownLatch latch, List<Variant> collectedVariants,
                                              AtomicInteger successfulFetches) {
        Log.d(TAG, "loadVariantsForSingleProduct: Calling API service getVariantsForProduct() for product ID: " + productId);

        apiService.getVariantsForProduct(productId).enqueue(new Callback<List<Variant>>() {
            @Override
            public void onResponse(@NonNull Call<List<Variant>> call, @NonNull Response<List<Variant>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Variant> variants = response.body();
                        Log.d(TAG, "onResponse: Received " + variants.size() + " variants for product ID: " + productId);

                        if (!variants.isEmpty()) {
                            // Manually set product_name and image_url for each variant
                            // because the backend's getVariantsForProduct does not perform aggregation for these fields.
                            for (Variant v : variants) {
                                v.setProductName(productName);
                                v.setImageUrl(imageUrl);
                                // Synchronize addition to the shared list as it's accessed by multiple threads
                                synchronized (collectedVariants) {
                                    collectedVariants.add(v);
                                }
                            }
                            successfulFetches.incrementAndGet(); // Increment count of successfully fetched variant lists
                        } else {
                            Log.w(TAG, "onResponse: No variants found for product ID: " + productId);
                        }

                    } else {
                        String errorMessage = "Failed to load variants for product " + productId + ": " + response.code() + " - " + response.message();
                        Log.e(TAG, "onResponse: " + errorMessage);
                        // Do not show Toast for every variant fetch error to avoid spamming the user
                    }
                } finally {
                    latch.countDown(); // Always decrement the latch, even on success or error
                    Log.d(TAG, "CountDown for product ID: " + productId + ". Latch count: " + latch.getCount());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Variant>> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: Network error during variant loading for product " + productId + ": " + t.getMessage(), t);
                try {
                    latch.countDown();
                    Log.d(TAG, "CountDown (onFailure) for product ID: " + productId + ". Latch count: " + latch.getCount());
                } catch (Exception e) {
                    Log.e(TAG, "Error in countDown on failure: " + e.getMessage(), e);
                }
            }
        });
    }

    /**
     * Filters the product list based on the search query.
     * @param query The search query string.
     */
    private void filterProductList(String query) {
        Log.d(TAG, "filterProductList: Filtering with query: '" + query + "'");
        List<Variant> filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            // If query is empty, display all collected variants
            filteredList.addAll(allVariants);
            Log.d(TAG, "filterProductList: Query empty, showing all " + allVariants.size() + " variants.");
        } else {
            String lowerCaseQuery = query.toLowerCase();
            int matchCount = 0;
            for (Variant variant : allVariants) {
                // Filter by product name (which was manually set)
                if (variant.getProductName() != null && variant.getProductName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(variant);
                    matchCount++;
                }
            }
            Log.d(TAG, "filterProductList: Found " + matchCount + " matching variants.");
        }
        if (productListAdapter != null) {
            // Update adapter with the filtered list
            productListAdapter.setData(filteredList);
            Log.d(TAG, "Product list adapter updated after filter. Displaying " + filteredList.size() + " items.");
        }
    }

    /**
     * Displays a Toast message for errors.
     * @param message The error message to display.
     */
    private void showError(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Showing Toast error: " + message);
        }
    }
}
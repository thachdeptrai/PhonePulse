package com.phoneapp.phonepulse.VIEW;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.phoneapp.phonepulse.Adapter.SearchProductAdapter;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.request.ProductGirdItem;
import com.phoneapp.phonepulse.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchProductActivity extends AppCompatActivity {

    private TextInputEditText et_search;
    private TextInputLayout til_search;
    private MaterialButton btn_sort_asc, btn_sort_desc;
    private TextView tv_no_results, tv_suggestions_title;
    private RecyclerView rv_products, rv_suggestions;
    private SearchProductAdapter productAdapter, suggestionAdapter;
    private List<ProductGirdItem> productList, filteredProductList, suggestionList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_product);

        // 1. Ánh xạ tất cả các View trước
        til_search = findViewById(R.id.til_search);
        et_search = findViewById(R.id.et_search);
        btn_sort_asc = findViewById(R.id.btn_sort_asc);
        btn_sort_desc = findViewById(R.id.btn_sort_desc);
        tv_no_results = findViewById(R.id.tv_no_results);
        tv_suggestions_title = findViewById(R.id.tv_suggestions_title);
        rv_products = findViewById(R.id.rv_products);
        rv_suggestions = findViewById(R.id.rv_suggestions);
        ImageButton backButton = findViewById(R.id.back_button);

        // 2. Khởi tạo danh sách dữ liệu chính
        productList = (ArrayList<ProductGirdItem>) getIntent().getSerializableExtra("product_list");
        if (productList == null) {
            productList = new ArrayList<>();
        }

        // 3. Khởi tạo các danh sách phụ (quan trọng: phải là 'new ArrayList<>()' hoặc 'new ArrayList<>(someOtherList)')
        // Để trống ban đầu hoặc sao chép từ productList tùy theo logic bạn muốn trước khi loadProducts()
        filteredProductList = new ArrayList<>(); // Khởi tạo để không bị null
        suggestionList = new ArrayList<>();    // Khởi tạo để không bị null

        // 4. Thiết lập RecyclerView và Adapters
        // Adapter cho sản phẩm tìm kiếm
        rv_products.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new SearchProductAdapter(this, filteredProductList); // filteredProductList đã được khởi tạo
        productAdapter.setOnItemClickListener(new SearchProductAdapter.OnItemClickListener() {
            @Override
            public void onAddToCartClick(ProductGirdItem item) {
                Toast.makeText(SearchProductActivity.this, "Đã thêm " + item.getProduct_name() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SearchProductActivity.this, Cart_Activity.class);
                intent.putExtra("product", item);
                startActivity(intent);
            }

            @Override
            public void onItemClick(ProductGirdItem item) {
                Log.d("CheckItem", "productId = " + item.get_id() + ", variantId = " + item.getVariant_id());
                Intent intent = new Intent(SearchProductActivity.this, ProductDetailActivity.class);
                intent.putExtra(Constants.PRODUCT_ID, item.get_id());
                intent.putExtra(Constants.VARIANT_ID, item.getVariant_id());
                startActivity(intent);
            }
        });
        rv_products.setAdapter(productAdapter);

        // Adapter cho gợi ý
        rv_suggestions.setLayoutManager(new LinearLayoutManager(this));
        suggestionAdapter = new SearchProductAdapter(this, suggestionList); // suggestionList đã được khởi tạo
        suggestionAdapter.setOnItemClickListener(new SearchProductAdapter.OnItemClickListener() {
            @Override
            public void onAddToCartClick(ProductGirdItem item) {
                Toast.makeText(SearchProductActivity.this, "Đã thêm " + item.getProduct_name() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SearchProductActivity.this, Cart_Activity.class);
                intent.putExtra("product", item);
                startActivity(intent);
            }

            @Override
            public void onItemClick(ProductGirdItem item) {
                // Ví dụ: khi nhấn vào gợi ý, điền vào ô tìm kiếm và thực hiện tìm kiếm
                et_search.setText(item.getProduct_name());
                filterProducts(item.getProduct_name());
                // Ẩn danh sách gợi ý sau khi chọn
                hideSuggestions();
                // Ẩn bàn phím
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null && et_search != null) {
                    imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
                }
            }
        });
        rv_suggestions.setAdapter(suggestionAdapter);

        // 5. Nạp dữ liệu vào các danh sách và cập nhật adapter
        // Bây giờ tất cả các list và adapter đã được khởi tạo và không còn null
        loadProducts(); // Chỉ gọi MỘT LẦN ở đây

        // 6. Thiết lập các listeners còn lại
        backButton.setOnClickListener(v -> onBackPressed());
        til_search.setStartIconOnClickListener(v -> finish());

        et_search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = et_search.getText().toString().trim();
                filterProducts(query);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null && v != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

        btn_sort_asc.setOnClickListener(v -> {
            Collections.sort(filteredProductList, (p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
            productAdapter.setData(filteredProductList); // productAdapter đã được khởi tạo
        });

        btn_sort_desc.setOnClickListener(v -> {
            Collections.sort(filteredProductList, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
            productAdapter.setData(filteredProductList); // productAdapter đã được khởi tạo
        });

        // Tự động hiển thị bàn phím
        et_search.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(et_search, InputMethodManager.SHOW_IMPLICIT);
        }
    }


    private void loadProducts() {
        // Kiểm tra an toàn, mặc dù với logic onCreate đúng, chúng không nên null
        if (filteredProductList == null || suggestionList == null || productList == null ||
                productAdapter == null || suggestionAdapter == null) {
            Log.e("SearchProductActivity", "Lỗi khởi tạo trong loadProducts. Một thành phần là null.");
            // Có thể thêm Toast hoặc xử lý khác ở đây nếu cần thiết
            // Nếu productList là null, không có gì để tải
            if (productList == null) {
                productList = new ArrayList<>(); // Khởi tạo để tránh lỗi sau đó
            }
            // Nếu các list phụ là null, khởi tạo chúng
            if (filteredProductList == null) {
                filteredProductList = new ArrayList<>();
            }
            if (suggestionList == null) {
                suggestionList = new ArrayList<>();
            }
            // Nếu adapter là null, không thể setData
            if (productAdapter == null || suggestionAdapter == null) {
                Log.e("SearchProductActivity", "Adapter chưa được khởi tạo trước khi gọi loadProducts.");
                return; // Không thể tiếp tục nếu adapter là null
            }
        }

        filteredProductList.clear();
        suggestionList.clear(); // Bạn có thể muốn logic khác cho suggestionList ban đầu

        // Chỉ thêm nếu productList không rỗng để tránh thao tác không cần thiết
        if (!productList.isEmpty()) {
            filteredProductList.addAll(productList);
            // Có thể bạn muốn suggestionList ban đầu hiển thị các sản phẩm bán chạy nhất thay vì toàn bộ productList
            // Ví dụ, gọi showSuggestions() ở đây nếu muốn hiển thị gợi ý mặc định,
            // hoặc để trống suggestionList và chỉ điền khi tìm kiếm không có kết quả.
            // Để đơn giản, ban đầu có thể làm giống filteredProductList hoặc để trống:
            // suggestionList.addAll(productList); // Hoặc để trống và xử lý trong showSuggestions()
        }

        productAdapter.setData(filteredProductList);
        suggestionAdapter.setData(suggestionList); // Cập nhật adapter gợi ý (có thể là danh sách rỗng ban đầu)

        // Quyết định xem có nên ẩn/hiện gợi ý ban đầu hay không
        if (filteredProductList.isEmpty() && !et_search.getText().toString().trim().isEmpty()) {
            // Nếu không có kết quả tìm kiếm VÀ đã có query -> hiển thị gợi ý
            showSuggestions();
        } else {
            // Mặc định ẩn gợi ý khi mới vào hoặc khi có kết quả
            hideSuggestions();
        }
    }



    private void filterProducts(String query) {
        filteredProductList.clear();
        if (query.isEmpty()) {
            filteredProductList.addAll(productList);
            hideSuggestions();
        } else {
            String lowerQuery = query.toLowerCase();
            for (ProductGirdItem product : productList) {
                if (product.getProduct_name().toLowerCase().contains(lowerQuery)
                        || (product.getCategory_id() != null && product.getCategory_id().toLowerCase().contains(lowerQuery))) {
                    filteredProductList.add(product);
                }
            }

            if (filteredProductList.isEmpty()) {
                showSuggestions();
            } else {
                hideSuggestions();
            }
        }
        productAdapter.setData(filteredProductList);
    }
    private void showSuggestions() {
        tv_no_results.setVisibility(View.VISIBLE);
        tv_suggestions_title.setVisibility(View.VISIBLE);
        rv_suggestions.setVisibility(View.VISIBLE);

        List<ProductGirdItem> top = new ArrayList<>(productList);
        Collections.sort(top, (p1, p2) -> Integer.compare(p2.getSold_count(), p1.getSold_count()));

        suggestionList.clear();
        suggestionList.addAll(top.subList(0, Math.min(5, top.size())));
        suggestionAdapter.setData(suggestionList);
    }

    private void hideSuggestions() {
        tv_no_results.setVisibility(View.GONE);
        tv_suggestions_title.setVisibility(View.GONE);
        rv_suggestions.setVisibility(View.GONE);
    }


}
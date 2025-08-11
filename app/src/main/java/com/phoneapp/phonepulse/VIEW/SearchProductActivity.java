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

        // Ánh xạ view
        til_search = findViewById(R.id.til_search);
        et_search = findViewById(R.id.et_search);
        btn_sort_asc = findViewById(R.id.btn_sort_asc);
        btn_sort_desc = findViewById(R.id.btn_sort_desc);
        tv_no_results = findViewById(R.id.tv_no_results);
        tv_suggestions_title = findViewById(R.id.tv_suggestions_title);
        rv_products = findViewById(R.id.rv_products);
        rv_suggestions = findViewById(R.id.rv_suggestions);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Quay lại màn hình trước đó
            }
        });


        productList = (ArrayList<ProductGirdItem>) getIntent().getSerializableExtra("product_list");
        if (productList == null) {
            productList = new ArrayList<>();
        }
        filteredProductList = new ArrayList<>(productList);
        suggestionList = new ArrayList<>(productList);

        // Thiết lập RecyclerView cho sản phẩm
        rv_products.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new SearchProductAdapter(this, filteredProductList);
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

        // Thiết lập RecyclerView cho gợi ý
        rv_suggestions.setLayoutManager(new LinearLayoutManager(this));
        suggestionAdapter = new SearchProductAdapter(this, suggestionList);
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
                Toast.makeText(SearchProductActivity.this, "Đã nhấn vào " + item.getProduct_name(), Toast.LENGTH_SHORT).show();
            }
        });
        rv_suggestions.setAdapter(suggestionAdapter);

        loadProducts();

        // Xử lý sự kiện nhấn vào biểu tượng quay lại
        til_search.setStartIconOnClickListener(v -> finish());

        // Xử lý tìm kiếm
        et_search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = et_search.getText().toString().trim();
                filterProducts(query);
                // Ẩn bàn phím
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        // Xử lý lọc giá tăng dần
        btn_sort_asc.setOnClickListener(v -> {
            Collections.sort(filteredProductList, (p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
            productAdapter.setData(filteredProductList);
        });

        // Xử lý lọc giá giảm dần
        btn_sort_desc.setOnClickListener(v -> {
            Collections.sort(filteredProductList, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
            productAdapter.setData(filteredProductList);
        });

        // Tự động hiển thị bàn phím
        et_search.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(et_search, InputMethodManager.SHOW_IMPLICIT);
    }

    private void loadProducts() {
        filteredProductList.clear();
        suggestionList.clear();

        filteredProductList.addAll(productList);
        suggestionList.addAll(productList);

        productAdapter.setData(filteredProductList);
        suggestionAdapter.setData(suggestionList);
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
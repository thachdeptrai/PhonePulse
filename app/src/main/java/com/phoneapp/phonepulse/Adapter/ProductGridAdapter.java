package com.phoneapp.phonepulse.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.data.api.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.models.Product;
import com.phoneapp.phonepulse.request.CartRequest;
import com.phoneapp.phonepulse.retrofit.RetrofitClient;
import com.phoneapp.phonepulse.ui.product.ProductDetailActivity;
import com.phoneapp.phonepulse.utils.Constants;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductGridAdapter extends RecyclerView.Adapter<ProductGridAdapter.ProductViewHolder> {
    private Context context;
    private List<Product> productList;
    private NumberFormat numberFormat;
    private ApiService apiService;

    public ProductGridAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        this.apiService = RetrofitClient.getApiService(Constants.getToken(context));
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_grid, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(productList.get(position));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateProducts(List<Product> newProducts) {
        this.productList = newProducts;
        notifyDataSetChanged();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private CardView cardProduct;
        private ImageView ivProductImage;
        private TextView tvProductName;
        private TextView tvOriginalPrice;
        private TextView tvDiscountPrice;
        private TextView tvDiscountPercent;
        private TextView tvSold;
        private Button btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            cardProduct = itemView.findViewById(R.id.card_product);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvDiscountPrice = itemView.findViewById(R.id.tv_discount_price);
            tvDiscountPercent = itemView.findViewById(R.id.tv_discount_percent);
            tvSold = itemView.findViewById(R.id.tv_sold);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
        }

        public void bind(Product product) {
            Glide.with(context)
                    .load(product.getCategory() != null ? product.getCategory().getIcon() : "")
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(ivProductImage);

            tvProductName.setText(product.getName());

            double price = 1000000;
            int discount = 0;

            if (discount > 0) {
                tvDiscountPercent.setVisibility(View.VISIBLE);
                tvOriginalPrice.setVisibility(View.VISIBLE);

                double discountedPrice = price * (100 - discount) / 100;

                tvOriginalPrice.setText("₫" + numberFormat.format(price));
                tvDiscountPrice.setText("₫" + numberFormat.format(discountedPrice));
                tvDiscountPercent.setText("-" + discount + "%");

                tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                tvDiscountPercent.setVisibility(View.GONE);
                tvOriginalPrice.setVisibility(View.GONE);
                tvDiscountPrice.setText("₫" + numberFormat.format(price));
            }

            int soldCount = (int) (Math.random() * 500);
            tvSold.setText("Đã bán " + soldCount);

            cardProduct.setOnClickListener(v -> {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra(Constants.PRODUCT_ID, product.getId());
                context.startActivity(intent);
            });

            btnAddToCart.setOnClickListener(v -> {
                if (Constants.getToken(context) == null || Constants.getToken(context).isEmpty()) {
                    Toast.makeText(context, "Vui lòng đăng nhập để mua hàng", Toast.LENGTH_SHORT).show();
                    return;
                }

                String variantId = ""; // Tạm thời để trống vì sản phẩm demo chưa có biến thể

                CartRequest request = new CartRequest(product.getId(), variantId, 1);

                apiService.addToCart(Constants.getToken(context), request).enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Lỗi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        Toast.makeText(context, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("AddToCart", t.getMessage());
                    }
                });
            });

        }
    }
}

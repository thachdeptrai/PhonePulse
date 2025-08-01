package com.phoneapp.phonepulse.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.models.Order;

import java.util.List;

public class CheckoutProductAdapter extends RecyclerView.Adapter<CheckoutProductAdapter.ViewHolder> {

    private List<Order.OrderItem> productList;

    public CheckoutProductAdapter(List<Order.OrderItem> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkout_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order.OrderItem item = productList.get(position);
        // Giả lập dữ liệu sản phẩm chi tiết
        holder.tvProductName.setText("Điện thoại GGGGG");
        holder.tvProductVariant.setText("Màu: blue, Kích thước: 6 inch, Bộ nhớ: 64GB");
        holder.tvProductQuantity.setText("Số lượng: " + item.getQuantity());
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductVariant, tvProductQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductVariant = itemView.findViewById(R.id.tv_product_variant);
            tvProductQuantity = itemView.findViewById(R.id.tv_product_quantity);
        }
    }
}
package com.phoneapp.phonepulse.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.request.OrderItem;

import java.util.List;

import io.reactivex.annotations.NonNull;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderViewHolder> {

    private List<OrderItem> orderItems;

    public OrderItemAdapter(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_product, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);
        holder.tvName.setText(item.getName());
        holder.tvQuantity.setText("Số lượng: " + item.getQuantity());
        holder.tvPrice.setText("Giá: " + formatCurrency(item.getPrice()));
        holder.tvVariant.setVisibility(View.GONE); // Nếu không có biến thể, ẩn dòng đó

        // Load ảnh bằng Glide hoặc Picasso
        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .placeholder(R.drawable.placeholder_product)
                .into(holder.ivImage);
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvVariant, tvQuantity, tvPrice;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_product_image);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvVariant = itemView.findViewById(R.id.tv_variant_details);
            tvQuantity = itemView.findViewById(R.id.tv_product_quantity);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
        }
    }

    private String formatCurrency(int amount) {
        return String.format("%,d đ", amount).replace(",", ".");
    }
}


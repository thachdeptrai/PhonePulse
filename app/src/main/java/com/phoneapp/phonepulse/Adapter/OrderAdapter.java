package com.phoneapp.phonepulse.Adapter;

import android.util.Log;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.models.Order;
import com.phoneapp.phonepulse.request.OrderItem;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import io.reactivex.annotations.NonNull;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<Order> orderList;
    private static final String TAG = "OrderAdapter";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public OrderAdapter(Context context, List<Order> orderList) {
        this.orderList = orderList;
        Log.d(TAG, "✅ Khởi tạo OrderAdapter - Tổng số đơn hàng: "
                + (orderList != null ? orderList.size() : 0));
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "📦 onCreateViewHolder() - viewType: " + viewType);
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        if (orderList == null || orderList.isEmpty()) {
            Log.w(TAG, "⚠ Không có dữ liệu đơn hàng để hiển thị");
            return;
        }

        Order order = orderList.get(position);
        Log.d(TAG, "----------------------------------------------");
        Log.d(TAG, "📌 ĐANG BIND ĐƠN HÀNG [" + position + "]");
        Log.d(TAG, "🆔 ID: " + order.getId());
        Log.d(TAG, "👤 UserID: " + order.getUserId());
        Log.d(TAG, "📅 Ngày tạo: " + order.getCreatedDate());
        Log.d(TAG, "💰 Tổng tiền: " + order.getFinalPrice());
        Log.d(TAG, "🎯 Trạng thái: " + order.getStatus());
        Log.d(TAG, "🚚 ShippingStatus: " + order.getShippingStatus());
        Log.d(TAG, "💳 PaymentStatus: " + order.getPaymentStatus());
        Log.d(TAG, "🏠 Địa chỉ giao hàng: " + order.getShippingAddress());
        Log.d(TAG, "📝 Ghi chú: " + order.getNote());

        // Bind UI
        holder.tvOrderId.setText("Đơn hàng #" + order.getId());
        holder.tvOrderDate.setText("Ngày đặt: " +
                (order.getCreatedDate() != null ? dateFormat.format(order.getCreatedDate()) : "N/A"));
        holder.tvOrderStatus.setText("Trạng thái: " + (order.getStatus() != null ? order.getStatus() : "Chưa xác định"));
        holder.tvOrderTotal.setText("Tổng tiền: " + formatCurrency((int) order.getFinalPrice()));

        // Log chi tiết các sản phẩm trong đơn
        List<OrderItem> items = order.getItems();
        if (items != null && !items.isEmpty()) {
            Log.d(TAG, "📦 Số sản phẩm trong đơn: " + items.size());
            for (int i = 0; i < items.size(); i++) {
                OrderItem it = items.get(i);
                Log.d(TAG, "   ├─ Sản phẩm [" + i + "]");
                Log.d(TAG, "   │   ID: " + it.getId());
                Log.d(TAG, "   │   Tên: " + it.getName());
                Log.d(TAG, "   │   SL: " + it.getQuantity());
                Log.d(TAG, "   │   Giá: " + it.getPrice());
                Log.d(TAG, "   │   Thành tiền: " + (it.getQuantity() * it.getPrice()));
                Log.d(TAG, "   │   Variant: " + it.getVariant());
                Log.d(TAG, "   │   ProductID: " + it.getProductId());
                Log.d(TAG, "   │   VariantID: " + it.getVariantId());
                Log.d(TAG, "   │   ImageURL: " + it.getImageUrl());
            }
        } else {
            Log.w(TAG, "⚠ order.getItems() rỗng hoặc null cho đơn hàng: " + order.getId());
        }

        // Adapter cho danh sách sản phẩm
        OrderItemAdapter itemAdapter = new OrderItemAdapter(items);
        holder.rvOrderItems.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.rvOrderItems.setAdapter(itemAdapter);
    }

    @Override
    public int getItemCount() {
        int count = orderList != null ? orderList.size() : 0;
        Log.d(TAG, "📊 getItemCount() = " + count);
        return count;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvOrderStatus, tvOrderTotal;
        RecyclerView rvOrderItems;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvOrderTotal = itemView.findViewById(R.id.tv_order_total);
            rvOrderItems = itemView.findViewById(R.id.rv_order_items);
        }
    }

    private String formatCurrency(int amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }
}

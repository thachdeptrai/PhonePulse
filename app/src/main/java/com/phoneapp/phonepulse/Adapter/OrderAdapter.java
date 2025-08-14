package com.phoneapp.phonepulse.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull; // ✅ Import đúng
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.models.Order;
import com.phoneapp.phonepulse.request.OrderItem;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<Order> orderList;
    private static final String TAG = "OrderAdapter";
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

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
        Log.d(TAG, "📌 ĐANG BIND ĐƠN HÀNG [" + position + "] - ID: " + order.getId());

        // Bind UI
        holder.tvOrderId.setText("Đơn hàng #" + order.getId());
        holder.tvOrderDate.setText("Ngày đặt: " + formatDate(order.getCreatedDate()));
        holder.tvOrderStatus.setText("Trạng thái: " + safeString(order.getStatus()));
        holder.tvOrderTotal.setText("Tổng tiền: " + formatCurrency(order.getFinalPrice()));

        // Adapter con cho sản phẩm
        List<OrderItem> items = order.getItems();
        if (items != null && !items.isEmpty()) {
            holder.rvOrderItems.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
            holder.rvOrderItems.setAdapter(new OrderItemAdapter(items));
        } else {
            Log.w(TAG, "⚠ order.getItems() rỗng hoặc null cho đơn hàng: " + order.getId());
        }
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
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

    private String safeString(String text) {
        return text != null ? text : "N/A";
    }

    private String formatDate(Object dateObj) {
        if (dateObj == null) return "N/A";

        if (dateObj instanceof Date) {
            return outputFormat.format((Date) dateObj);
        } else if (dateObj instanceof String) {
            try {
                Date date = inputFormat.parse((String) dateObj);
                return outputFormat.format(date);
            } catch (ParseException e) {
                return dateObj.toString();
            }
        }
        return dateObj.toString();
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }
}

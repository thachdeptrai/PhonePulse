package com.phoneapp.phonepulse.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.models.Order;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<Order> orderList;
    private final Context context;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        // Set mã đơn hàng
        holder.tvOrderId.setText("Đơn hàng #" + order.get_id());

        // Set ngày đặt
        if (order.getCreated_date() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvOrderDate.setText("Ngày đặt: " + dateFormat.format(order.getCreated_date()));
        }

        // Set tổng tiền
        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.tvOrderTotal.setText("Tổng tiền: " + formatter.format(order.getFinal_price()) + "đ");

        // Set trạng thái
        String status = order.getStatus();
        holder.tvOrderStatus.setText("Trạng thái: " + getStatusText(status));
        holder.tvOrderStatus.setTextColor(context.getResources().getColor(getStatusColor(status)));
        // holder.ivOrderStatus.setImageResource(getStatusIcon(status)); // Cần có icon tương ứng trong drawable

        // Setup RecyclerView cho các sản phẩm trong đơn hàng
        holder.rvOrderItems.setLayoutManager(new LinearLayoutManager(context));
        // holder.rvOrderItems.setAdapter(new OrderItemsAdapter(order.getItems()));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvOrderTotal, tvOrderStatus;
        ImageView ivOrderStatus;
        RecyclerView rvOrderItems;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvOrderTotal = itemView.findViewById(R.id.tv_order_total);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            ivOrderStatus = itemView.findViewById(R.id.iv_order_status_icon);
            rvOrderItems = itemView.findViewById(R.id.rv_order_items);
        }
    }

    private String getStatusText(String status) {
        switch (status) {
            case "pending": return "Đang xử lý";
            case "confirmed": return "Đã xác nhận";
            case "cancelled": return "Đã hủy";
            default: return "Không rõ";
        }
    }

    private int getStatusColor(String status) {
        switch (status) {
            case "pending": return R.color.orange;
            case "confirmed": return R.color.green;
            case "cancelled": return R.color.red;
            default: return R.color.black;
        }
    }
}
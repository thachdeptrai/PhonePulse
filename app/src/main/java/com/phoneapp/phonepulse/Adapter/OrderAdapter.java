package com.phoneapp.phonepulse.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.models.Order;
import com.phoneapp.phonepulse.request.OrderItem;
import com.phoneapp.phonepulse.utils.OrderCanceledEvent;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// ✅ Import EventBus và Event Class
import org.greenrobot.eventbus.EventBus;


public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<Order> orderList;
    private static final String TAG = "OrderAdapter";

    private final SimpleDateFormat inputFormat =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat outputFormat =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public OrderAdapter(Context context, List<Order> orderList) {
        this.orderList = orderList;
        Log.d(TAG, "✅ Khởi tạo OrderAdapter - Tổng số đơn hàng: "
                + (orderList != null ? orderList.size() : 0));
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        if (orderList == null || orderList.isEmpty()) return;

        Order order = orderList.get(position);

        // ===== Bind dữ liệu đơn hàng =====
        holder.tvOrderId.setText("Đơn hàng #" + order.getId());
        holder.tvOrderDate.setText("Ngày đặt: " + formatDate(order.getCreatedDate()));
        holder.tvOrderStatus.setText("Trạng thái: " + mapTrangThaiDonHang(order.getStatus()));
        holder.tvOrderTotal.setText("Tổng tiền: " + formatCurrency(order.getFinalPrice()));

        // ===== Trạng thái thanh toán =====
        String paymentStatus = safeString(order.getPaymentStatus()).toLowerCase(Locale.ROOT);
        switch (paymentStatus) {
            case "paid":
                holder.tvPaymentStatus.setText("Đã thanh toán");
                holder.tvPaymentStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                break;
            case "refunded":
                holder.tvPaymentStatus.setText("Hoàn tiền");
                holder.tvPaymentStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));
                break;
            case "unpaid":
            default:
                holder.tvPaymentStatus.setText("Chưa thanh toán");
                holder.tvPaymentStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
                break;
        }

        // ===== Hiển thị danh sách sản phẩm =====
        List<OrderItem> items = order.getItems();
        if (items != null && !items.isEmpty()) {
            if (holder.rvOrderItems.getAdapter() == null) {
                holder.rvOrderItems.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
            }
            holder.rvOrderItems.setAdapter(new OrderItemAdapter(items));
        }

        // ===== Xử lý nút hủy đơn và hiển thị/ẩn nút =====
        String orderStatus = safeString(order.getStatus()).toLowerCase(Locale.ROOT);
        String shippingStatus = safeString(order.getShippingStatus()).toLowerCase(Locale.ROOT);

        // Chỉ hiển thị nút hủy nếu trạng thái là 'pending', 'confirmed' hoặc 'shipping'
        if (("pending".equals(orderStatus) || "confirmed".equals(orderStatus)) && !"shipped".equals(shippingStatus)) {
            holder.btnCancelOrder.setVisibility(View.VISIBLE);
            holder.btnCancelOrder.setOnClickListener(v -> {
                showCancelBottomSheet(v.getContext(), order.getId());
            });
        } else if ("shipping".equals(shippingStatus)) { // Nếu đang giao hàng cũng cho phép hủy
            holder.btnCancelOrder.setVisibility(View.VISIBLE);
            holder.btnCancelOrder.setOnClickListener(v -> {
                showCancelBottomSheet(v.getContext(), order.getId());
            });
        }
        else {
            holder.btnCancelOrder.setVisibility(View.GONE); // Ẩn nút nếu không thể hủy
        }
    }

    // ================== BottomSheet hủy đơn ==================
    private void showCancelBottomSheet(Context context, String orderId) {
        View view = LayoutInflater.from(context).inflate(R.layout.bottomsheet_cancel_order, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(view);

        RadioGroup radioGroup = view.findViewById(R.id.radioGroupReasons);
        MaterialButton btnConfirmCancel = view.findViewById(R.id.btnConfirmCancel);

        btnConfirmCancel.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selected = view.findViewById(selectedId);
                String reason = selected.getText().toString();
                Log.d(TAG, "❌ Hủy đơn " + orderId + " với lý do: " + reason);

                // ✅ Phát ra sự kiện EventBus khi người dùng xác nhận hủy
                EventBus.getDefault().post(new OrderCanceledEvent(orderId, reason));

                bottomSheetDialog.dismiss();
            } else {
                // Tùy chọn: Thông báo người dùng chọn lý do
                // Toast.makeText(context, "Vui lòng chọn lý do hủy.", Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheetDialog.show();
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    // ================== ViewHolder ==================
    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvOrderStatus, tvOrderTotal, tvPaymentStatus;
        RecyclerView rvOrderItems;
        MaterialButton btnCancelOrder;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvOrderTotal = itemView.findViewById(R.id.tv_order_total);
            tvPaymentStatus = itemView.findViewById(R.id.tv_payment_status);
            rvOrderItems = itemView.findViewById(R.id.rv_order_items);
            btnCancelOrder = itemView.findViewById(R.id.btn_cancel_order);
        }
    }

    // ================== Utils ==================
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
        formatter.setCurrency(Currency.getInstance("VND"));
        return formatter.format(amount);
    }

    private String mapTrangThaiDonHang(String status) {
        if (status == null) return "Không xác định";

        switch (status.toLowerCase()) {
            case "pending":
                return "Đang chờ xử lý";
            case "processing":
                return "Đang xử lý";
            case "confirmed":
                return "Đã xác nhận";
            case "shipping":
                return "Đang giao hàng";
            case "delivered":
                return "Hoàn thành";
            case "cancelled":
                return "Đã hủy";
            case "returned":
                return "Đã trả hàng";
            default:
                return status;
        }
    }
}
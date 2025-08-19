package com.phoneapp.phonepulse.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.models.Voucher;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    private final List<Voucher> voucherList;
    private final OnVoucherClickListener listener;

    public interface OnVoucherClickListener {
        void onVoucherClick(Voucher voucher);
    }

    public VoucherAdapter(List<Voucher> voucherList, OnVoucherClickListener listener) {
        this.voucherList = voucherList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = voucherList.get(position);

        // 1. Mã code
        holder.tvCode.setText(voucher.getCode());

        // 2. Giảm giá
        String discountText;
        if ("percent".equalsIgnoreCase(voucher.getDiscountType())) {
            discountText = String.format(Locale.getDefault(),
                    "Giảm %.0f%% (tối đa %,d₫)",
                    voucher.getDiscountValue(),
                    (long) voucher.getMaxDiscount());
        } else {
            discountText = String.format(Locale.getDefault(),
                    "Giảm %,d₫",
                    (long) voucher.getDiscountValue());
        }
        holder.tvDiscount.setText(discountText);

        // 3. Điều kiện áp dụng
        String conditionText = String.format(Locale.getDefault(),
                "Áp dụng cho đơn từ %,d₫",
                (long) voucher.getMinOrderValue());
        holder.tvCondition.setText(conditionText);

        // 4. Thời gian hiệu lực
        String dateText = formatDate(voucher.getStartDate()) + " - " + formatDate(voucher.getEndDate());
        holder.tvDate.setText(dateText);

        // Click
        holder.itemView.setOnClickListener(v -> listener.onVoucherClick(voucher));
    }

    @Override
    public int getItemCount() {
        return voucherList != null ? voucherList.size() : 0;
    }

    public static class VoucherViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvDiscount, tvCondition, tvDate;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvVoucherCode);
            tvDiscount = itemView.findViewById(R.id.tvVoucherDiscount);
            tvCondition = itemView.findViewById(R.id.tvVoucherCondition);
            tvDate = itemView.findViewById(R.id.tvVoucherDate);
        }
    }

    private String formatDate(String rawDate) {
        try {
            // giả định server trả ISO-8601 (yyyy-MM-dd...)
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = input.parse(rawDate);
            return output.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return rawDate;
        }
    }
}
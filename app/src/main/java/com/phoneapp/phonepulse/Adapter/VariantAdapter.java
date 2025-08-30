package com.phoneapp.phonepulse.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.models.Variant;
import java.util.List;

public class VariantAdapter extends RecyclerView.Adapter<VariantAdapter.VariantViewHolder> {

    private final List<Variant> variants;
    private final OnVariantClickListener onVariantClickListener;
    private int selectedPosition = RecyclerView.NO_POSITION; // vị trí được chọn

    public interface OnVariantClickListener {
        void onVariantClick(Variant variant);
    }

    public VariantAdapter(List<Variant> variants, OnVariantClickListener listener) {
        this.variants = variants;
        this.onVariantClickListener = listener;
    }

    @NonNull
    @Override
    public VariantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_variant, parent, false);
        return new VariantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VariantViewHolder holder, int position) {
        Variant variant = variants.get(position);

        // Hiển thị tên size + màu
        String variantText = "";
        if (variant.getSize() != null && variant.getSize().getSizeName() != null) {
            variantText += variant.getSize().getSizeName();
        }
        if (variant.getColor() != null && variant.getColor().getColorName() != null) {
            if (!variantText.isEmpty()) {
                variantText += " - ";
            }
            variantText += variant.getColor().getColorName();
        }
        holder.tvVariantName.setText(variantText);

        // Highlight item được chọn
        if (position == selectedPosition) {
            holder.itemView.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_variant_selected));
            holder.tvVariantName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.shopee_orange));
        } else {
            holder.itemView.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_variant_default));
            holder.tvVariantName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.black));
        }

        // Handle click
        holder.itemView.setOnClickListener(v -> {
            int prevSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            notifyItemChanged(prevSelected); // refresh item cũ
            notifyItemChanged(selectedPosition); // refresh item mới

            if (onVariantClickListener != null) {
                onVariantClickListener.onVariantClick(variant);
            }
        });
    }


    @Override
    public int getItemCount() {
        return variants.size();
    }

    static class VariantViewHolder extends RecyclerView.ViewHolder {
        final TextView tvVariantName;

        VariantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVariantName = itemView.findViewById(R.id.tv_variant_name);
        }
    }
}

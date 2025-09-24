package com.phoneapp.phonepulse.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.models.Address;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<Address> addressList;
    private int selectedPosition = -1; // vị trí được chọn
    private OnAddressSelectedListener listener;

    // Giao diện callback khi chọn địa chỉ
    public interface OnAddressSelectedListener {
        void onAddressSelected(Address address);
    }

    public AddressAdapter(List<Address> addressList, OnAddressSelectedListener listener) {
        this.addressList = addressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addressList.get(position);

        holder.tvFullName.setText(address.getFullName());
        holder.tvPhone.setText(address.getPhoneNumber());
        holder.tvShippingAddress.setText(address.getFullAddress());

        // ✅ Đảm bảo chỉ duy nhất 1 radio được chọn
        holder.radioSelect.setChecked(position == selectedPosition);

        // Click cả item
        holder.itemView.setOnClickListener(v -> selectAddress(holder.getAdapterPosition()));

        // Click riêng RadioButton
        holder.radioSelect.setOnClickListener(v -> selectAddress(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return addressList != null ? addressList.size() : 0;
    }

    // ✅ Hàm chọn địa chỉ
    private void selectAddress(int position) {
        selectedPosition = position;
        notifyDataSetChanged(); // refresh toàn bộ để cập nhật radio
        if (listener != null && position >= 0 && position < addressList.size()) {
            listener.onAddressSelected(addressList.get(position));
        }
    }

    // ✅ Trả về địa chỉ đang chọn
    public Address getSelectedAddress() {
        if (selectedPosition >= 0 && selectedPosition < addressList.size()) {
            return addressList.get(selectedPosition);
        }
        return null;
    }

    // ViewHolder
    static class AddressViewHolder extends RecyclerView.ViewHolder {
        RadioButton radioSelect;
        TextView tvFullName, tvPhone, tvShippingAddress;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            radioSelect = itemView.findViewById(R.id.radioSelect);
            tvFullName = itemView.findViewById(R.id.tv_full_name);
            tvPhone = itemView.findViewById(R.id.tv_phone_number);
            tvShippingAddress = itemView.findViewById(R.id.tv_shipping_address);
        }
    }
}




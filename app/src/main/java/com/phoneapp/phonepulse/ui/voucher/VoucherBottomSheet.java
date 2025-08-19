package com.phoneapp.phonepulse.ui.voucher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.phoneapp.phonepulse.Adapter.VoucherAdapter;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.models.Voucher;

import java.util.List;

public class VoucherBottomSheet extends BottomSheetDialogFragment {

    private RecyclerView rvVouchers;
    private Button btnApply;

    private VoucherAdapter adapter;
    private List<Voucher> voucherList;
    private OnVoucherSelectedListener listener;

    // Truyền danh sách voucher + callback
    public VoucherBottomSheet(List<Voucher> vouchers, OnVoucherSelectedListener listener) {
        this.voucherList = vouchers;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottomsheet_voucher, container, false);

        rvVouchers = view.findViewById(R.id.rv_vouchers);
        btnApply = view.findViewById(R.id.btn_apply_voucher);

        // Setup RecyclerView
        rvVouchers.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VoucherAdapter(voucherList, voucher -> {
            // Callback khi chọn voucher
            if (listener != null) {
                listener.onVoucherSelected(voucher);
            }
        });
        rvVouchers.setAdapter(adapter);

        // Nút áp dụng
        btnApply.setOnClickListener(v -> dismiss());

        return view;
    }

    // Interface callback
    public interface OnVoucherSelectedListener {
        void onVoucherSelected(Voucher voucher);
    }
}

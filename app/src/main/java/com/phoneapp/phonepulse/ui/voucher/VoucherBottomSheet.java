package com.phoneapp.phonepulse.ui.voucher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.phoneapp.phonepulse.Adapter.VoucherAdapter;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.models.Voucher;

import java.util.List;

public class VoucherBottomSheet extends BottomSheetDialogFragment {

    private RecyclerView rvVouchers;
    private Button btnApply, btnCancel;

    private VoucherAdapter adapter;
    private final List<Voucher> voucherList;
    private final OnVoucherSelectedListener listener;

    // Lưu ý: bạn đã dùng constructor truyền dữ liệu, giữ nguyên cho phù hợp với code hiện tại
    public VoucherBottomSheet(@NonNull List<Voucher> vouchers,
                              @NonNull OnVoucherSelectedListener listener) {
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
        btnApply   = view.findViewById(R.id.btn_apply_voucher);
        btnCancel  = view.findViewById(R.id.btn_cancel_voucher);

        // Không cho chạm ra ngoài để tự đóng
        setCancelable(false);

        // RecyclerView + Adapter (bản có RadioButton, KHÔNG truyền lambda)
        rvVouchers.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VoucherAdapter(voucherList);
        rvVouchers.setAdapter(adapter);

        // Áp dụng voucher: chỉ callback khi người dùng bấm nút này
        btnApply.setOnClickListener(v -> {
            Voucher selected = adapter.getSelectedVoucher();
            if (selected == null) {
                Toast.makeText(getContext(), "Bạn chưa chọn mã giảm giá.", Toast.LENGTH_SHORT).show();
                return;
            }
            listener.onVoucherSelected(selected);
            dismiss();
        });

        // Không dùng mã: trả về null
        btnCancel.setOnClickListener(v -> {
            listener.onVoucherSelected(null);
            dismiss();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Chặn bấm ra ngoài để đóng
        if (getDialog() instanceof BottomSheetDialog) {
            ((BottomSheetDialog) getDialog()).setCanceledOnTouchOutside(false);
        }
    }

    public interface OnVoucherSelectedListener {
        void onVoucherSelected(@Nullable Voucher voucher);
    }
}
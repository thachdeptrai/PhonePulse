package com.phoneapp.phonepulse.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.phoneapp.phonepulse.FRAGMENT.HISTORY_ORDER_FRAGMENT.Dahuy_FRAGMENT;
import com.phoneapp.phonepulse.FRAGMENT.HISTORY_ORDER_FRAGMENT.Danggiaohang_FRAGMENT;
import com.phoneapp.phonepulse.FRAGMENT.HISTORY_ORDER_FRAGMENT.Dangxuly_FRAGMENT;
import com.phoneapp.phonepulse.FRAGMENT.HISTORY_ORDER_FRAGMENT.Hoanthanh_FRAGMENT;
import com.phoneapp.phonepulse.FRAGMENT.HISTORY_ORDER_FRAGMENT.TatCaDonHang_FRAGMENT;
import com.phoneapp.phonepulse.FRAGMENT.OrderHistory_FRAGMENT;

public class OrderPagerAdapter extends FragmentStateAdapter {

    public OrderPagerAdapter(@NonNull OrderHistory_FRAGMENT fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new TatCaDonHang_FRAGMENT();
            case 1: return new Dangxuly_FRAGMENT();
            case 2: return new Danggiaohang_FRAGMENT();
            case 3: return new Hoanthanh_FRAGMENT();
            case 4: return new Dahuy_FRAGMENT();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 5; // số lượng tab
    }
}

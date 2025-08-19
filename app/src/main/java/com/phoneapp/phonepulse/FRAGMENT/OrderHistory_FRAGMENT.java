package com.phoneapp.phonepulse.FRAGMENT;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.phoneapp.phonepulse.Adapter.OrderPagerAdapter;
import com.phoneapp.phonepulse.R;

public class OrderHistory_FRAGMENT extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.orderhistory_fragment, container, false);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        OrderPagerAdapter adapter = new OrderPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Tất cả đơn hàng "); break;
                case 1: tab.setText("Đang xử lý"); break;
                case 2: tab.setText("Đang giao"); break;
                case 3: tab.setText("Hoàn thành"); break;
                case 4: tab.setText("Đã huỷ"); break;
            }
        }).attach();

        return view;
    }
}

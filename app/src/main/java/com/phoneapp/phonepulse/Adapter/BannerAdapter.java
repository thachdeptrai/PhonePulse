package com.phoneapp.phonepulse.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.phoneapp.phonepulse.R;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
    private Context context;
    private List<String> bannerImages;

    public BannerAdapter(Context context, List<String> bannerImages) {
        this.context = context;
        this.bannerImages = bannerImages;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        String imageUrl = bannerImages.get(position);
        holder.bind(imageUrl);
    }

    @Override
    public int getItemCount() {
        return bannerImages.size();
    }

    class BannerViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivBanner;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBanner = itemView.findViewById(R.id.iv_banner);
        }

        public void bind(String imageUrl) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_banner)
                    .error(R.drawable.placeholder_banner)
                    .centerCrop()
                    .into(ivBanner);

            // Optional: Add click listener for banner actions
            ivBanner.setOnClickListener(v -> {
                // Handle banner click - navigate to specific screen or open URL
            });
        }
    }
}
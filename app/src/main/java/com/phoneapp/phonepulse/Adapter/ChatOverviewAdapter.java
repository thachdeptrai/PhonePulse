package com.phoneapp.phonepulse.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.models.ChatOverviewItem;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatOverviewAdapter extends RecyclerView.Adapter<ChatOverviewAdapter.ChatOverviewViewHolder> {

    private List<ChatOverviewItem> chatItems;
    private OnChatItemClickListener listener;

    public interface OnChatItemClickListener {
        void onChatItemClick(ChatOverviewItem item);
    }

    public ChatOverviewAdapter(List<ChatOverviewItem> chatItems, OnChatItemClickListener listener) {
        this.chatItems = chatItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatOverviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_overview, parent, false);
        return new ChatOverviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatOverviewViewHolder holder, int position) {
        ChatOverviewItem currentItem = chatItems.get(position);
        holder.bind(currentItem, listener);
    }

    @Override
    public int getItemCount() {
        return chatItems == null ? 0 : chatItems.size();
    }

    // Phương thức để cập nhật danh sách (trong trường hợp này sẽ chỉ là một item)
    public void updateChatItems(List<ChatOverviewItem> newItems) {
        this.chatItems.clear();
        if (newItems != null) {
            this.chatItems.addAll(newItems);
        }
        notifyDataSetChanged(); // Hoặc sử dụng DiffUtil nếu danh sách lớn hơn
    }


    static class ChatOverviewViewHolder extends RecyclerView.ViewHolder {
        TextView displayNameText;
        TextView lastMessagePreviewText;
        TextView lastActivityTimeText;

        ChatOverviewViewHolder(@NonNull View itemView) {
            super(itemView);
            displayNameText = itemView.findViewById(R.id.tv_chat_display_name);
            lastMessagePreviewText = itemView.findViewById(R.id.tv_chat_last_message_preview);
            lastActivityTimeText = itemView.findViewById(R.id.tv_chat_last_activity_time);
        }

        void bind(final ChatOverviewItem item, final OnChatItemClickListener listener) {
            displayNameText.setText(item.getDisplayName());
            lastMessagePreviewText.setText(item.getLastMessagePreview());

            if (item.getLastActivityTimestamp() > 0) {
                lastActivityTimeText.setText(formatTimestamp(item.getLastActivityTimestamp()));
                lastActivityTimeText.setVisibility(View.VISIBLE);
            } else {
                lastActivityTimeText.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChatItemClick(item);
                }
            });
        }

        private String formatTimestamp(long timestamp) {
            // Ví dụ: hiển thị "HH:mm dd/MM/yyyy"
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}

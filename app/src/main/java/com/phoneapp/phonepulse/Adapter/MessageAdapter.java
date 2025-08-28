package com.phoneapp.phonepulse.Adapter;

import android.content.Context;
import android.util.Log; // Thêm import này
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.models.Message;

import java.text.ParseException; // Thêm import này
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone; // Thêm import này

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final String TAG = "MessageAdapter"; // Thêm TAG để log

    private Context context;
    private List<Message> messageList;
    private String currentUserId; // Để xác định tin nhắn nào là của người dùng hiện tại

    public MessageAdapter(Context context, List<Message> messageList, String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (message.getSenderId() != null && message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else { // VIEW_TYPE_MESSAGE_RECEIVED
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        String formattedTime = " "; // Giá trị mặc định hoặc placeholder

        String isoTimestampString = message.getTimestamp(); // Đây là chuỗi ISO 8601

        if (isoTimestampString != null && !isoTimestampString.isEmpty()) {
            try {
                // Định dạng server gửi là ISO 8601, ví dụ: "2025-08-25T13:37:48.734Z"
                // Chữ 'Z' ở cuối biểu thị múi giờ UTC (Zulu)
                SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC")); // Quan trọng: Đảm bảo parse đúng múi giờ UTC

                Date date = iso8601Format.parse(isoTimestampString);
                if (date != null) {
                    long timestampMillis = date.getTime(); // Lấy timestamp dạng long (milliseconds)
                    formattedTime = formatTimestamp(timestampMillis); // Gọi phương thức formatTimestamp hiện có của bạn
                } else {
                    Log.w(TAG, "Parsed date was null for timestamp string: " + isoTimestampString);
                    // formattedTime sẽ giữ giá trị mặc định
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing timestamp string: " + isoTimestampString, e);
                // formattedTime sẽ giữ giá trị mặc định trong trường hợp lỗi parse
            }
        } else {
            Log.w(TAG, "Timestamp string from message was null or empty.");
            // formattedTime sẽ giữ giá trị mặc định
        }

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message, formattedTime);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message, formattedTime);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messageList == null ? 0 : messageList.size();
    }

    private String formatTimestamp(long timestampMillis) {
        // Phương thức này giữ nguyên, nó nhận long và format thành HH:mm
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestampMillis));
    }

    // --- ViewHolders ---

    private static class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tv_message_text_sent);
            timeText = itemView.findViewById(R.id.tv_message_timestamp_sent);
        }

        void bind(Message message, String formattedTime) {
            messageText.setText(message.getMessageText());
            timeText.setText(formattedTime);
        }
    }

    private static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;
        // TextView senderNameText; // Nếu bạn muốn hiển thị tên người gửi

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tv_message_text_received);
            timeText = itemView.findViewById(R.id.tv_message_timestamp_received);
            // senderNameText = itemView.findViewById(R.id.tv_sender_name_received); // Nếu có
        }

        void bind(Message message, String formattedTime) {
            messageText.setText(message.getMessageText());
            timeText.setText(formattedTime);
            // if (senderNameText != null) {
            //     // TODO: Lấy tên người gửi dựa trên message.getSenderId() nếu cần
            //     senderNameText.setText(message.getSenderId()); // Tạm thời hiển thị ID
            // }
        }
    }
}

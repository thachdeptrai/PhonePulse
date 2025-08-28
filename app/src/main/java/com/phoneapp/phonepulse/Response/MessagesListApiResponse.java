package com.phoneapp.phonepulse.Response;

import com.google.gson.annotations.SerializedName;
import com.phoneapp.phonepulse.models.Message; // Đảm bảo import đúng
import java.util.List;

public class MessagesListApiResponse {
    @SerializedName("success")
    private boolean success;

    // API trả về key "messages" cho danh sách tin nhắn
    @SerializedName("messages")
    private List<Message> messages;

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public List<Message> getMessages() {
        return messages;
    }
}

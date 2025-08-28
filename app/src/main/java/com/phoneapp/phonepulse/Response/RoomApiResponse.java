package com.phoneapp.phonepulse.Response;
import com.google.gson.annotations.SerializedName;
import com.phoneapp.phonepulse.models.ChatRoom; // Import

public class RoomApiResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("room")
    private ChatRoom room; // Sử dụng ChatRoom

    public boolean isSuccess() { return success; }
    public ChatRoom getRoom() { return room; }
}
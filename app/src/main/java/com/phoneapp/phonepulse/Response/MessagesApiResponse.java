package com.phoneapp.phonepulse.Response; // Or your appropriate package

import com.google.gson.annotations.SerializedName;
import com.phoneapp.phonepulse.models.Message; // Assuming you have a Message model

import java.util.List;

public class MessagesApiResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("messages")
    private List<Message> messages;

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
    
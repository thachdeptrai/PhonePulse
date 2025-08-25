package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;

public class Message {
    @SerializedName(value = "_id", alternate = {"messageId"})
    private String messageId;
    @SerializedName("senderId")
    private String senderId;
    @SerializedName("roomId")
    private String roomId; // Có thể cần nếu server trả về hoặc bạn muốn lưu
    @SerializedName("message") // Giả sử key trong JSON là "message" cho messageText
    private String messageText;
    @SerializedName("timestamp")
    private String timestamp; // << ĐỔI THÀNH String
    @SerializedName("messageType")
    private String messageType; // Ví dụ: "text", "image"
    private boolean isSentByCurrentUser; // Để adapter biết cách hiển thị (trái/phải)
                                        // Trường này thường không có trong JSON từ server,
                                        // mà được tính toán ở client.

    // Constructor trống cho một số thư viện (ví dụ: Firebase) hoặc để tạo thủ công
    public Message() {
    }

    // Constructor đầy đủ
    public Message(String messageId, String senderId, String roomId, String messageText, String timestamp, String messageType, boolean isSentByCurrentUser) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.roomId = roomId;
        this.messageText = messageText;
        this.timestamp = timestamp; // << Kiểu String
        this.messageType = messageType;
        this.isSentByCurrentUser = isSentByCurrentUser;
    }

    // Getters
    public String getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getTimestamp() { // << Trả về String
        return timestamp;
    }

    public String getMessageType() {
        return messageType;
    }

    public boolean isSentByCurrentUser() {
        return isSentByCurrentUser;
    }

    // Setters
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public void setTimestamp(String timestamp) { // << Nhận String
        this.timestamp = timestamp;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setSentByCurrentUser(boolean sentByCurrentUser) {
        isSentByCurrentUser = sentByCurrentUser;
    }
}

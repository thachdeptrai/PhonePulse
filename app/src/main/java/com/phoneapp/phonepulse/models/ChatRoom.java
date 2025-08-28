// In package: com.phoneapp.phonepulse.models
package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;

public class ChatRoom {

    @SerializedName("roomId")
    private String roomId;

    @SerializedName("userId")
    private String userId;

    @SerializedName("adminId")
    private String adminId; // Có thể null

    @SerializedName("status")
    private String status; // Ví dụ: "waiting", "active", "closed"

    @SerializedName("createdAt")
    private String createdAt; // << SỬA THÀNH String

    @SerializedName("updatedAt")
    private String updatedAt; // << SỬA THÀNH String

    // Getters and Setters
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; } // << Sửa kiểu trả về
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; } // << Sửa kiểu tham số

    public String getUpdatedAt() { return updatedAt; } // << Sửa kiểu trả về
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; } // << Sửa kiểu tham số
}
   
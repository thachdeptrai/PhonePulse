package com.phoneapp.phonepulse.models;

public class ChatOverviewItem {
    private String roomId;
    private String displayName; // Ví dụ: "Hỗ trợ khách hàng", "Đang chờ hỗ trợ"
    private String lastMessagePreview; // Ví dụ: "Nhấn để xem tin nhắn"
    private long lastActivityTimestamp; // Thời gian hoạt động cuối, dùng để hiển thị và sắp xếp (nếu có nhiều item sau này)
    private String status; // Trạng thái của phòng chat từ API (ví dụ: "waiting", "active")
    private String userId; // ID của người dùng, có thể cần để khởi chạy ChatSupportActivity
    private String adminId; // ID của admin (nếu có), có thể cần để khởi chạy ChatSupportActivity hoặc logic khác

    // Constructors
    public ChatOverviewItem(String roomId, String displayName, String lastMessagePreview, long lastActivityTimestamp, String status, String userId, String adminId) {
        this.roomId = roomId;
        this.displayName = displayName;
        this.lastMessagePreview = lastMessagePreview;
        this.lastActivityTimestamp = lastActivityTimestamp;
        this.status = status;
        this.userId = userId;
        this.adminId = adminId;
    }

    // Getters
    public String getRoomId() {
        return roomId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLastMessagePreview() {
        return lastMessagePreview;
    }

    public long getLastActivityTimestamp() {
        return lastActivityTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public String getUserId() {
        return userId;
    }

    public String getAdminId() {
        return adminId;
    }

    // Setters (Nếu cần thiết, ví dụ nếu bạn tạo đối tượng rỗng rồi set giá trị sau)
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setLastMessagePreview(String lastMessagePreview) {
        this.lastMessagePreview = lastMessagePreview;
    }

    public void setLastActivityTimestamp(long lastActivityTimestamp) {
        this.lastActivityTimestamp = lastActivityTimestamp;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }
}

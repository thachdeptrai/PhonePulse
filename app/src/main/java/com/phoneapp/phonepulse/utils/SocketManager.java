package com.phoneapp.phonepulse.utils; // Hoặc package phù hợp

import android.util.Log;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONObject; // Sử dụng org.json cho JSONObject
import java.net.URISyntaxException;

public class SocketManager {
    private static final String TAG = "SocketManager";
    // private static final String SOCKET_URL = "http://YOUR_BACKEND_IP_OR_DOMAIN:PORT"; // THAY THẾ BẰNG URL THẬT
    private static final String SOCKET_URL = "http://10.0.2.2:5000"; // Ví dụ cho emulator kết nối localhost của máy host

    private static Socket mSocket;
    private static String currentRoomId;

    public static synchronized Socket getSocket() {
        if (mSocket == null) {
            // Sẽ được gọi khi kết nối thực sự (connectSocket)
            Log.e(TAG, "Socket chưa được khởi tạo. Gọi connectSocket() trước.");
        }
        return mSocket;
    }

    public static synchronized void connectSocket(String token) {
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token không hợp lệ, không thể kết nối socket.");
            return;
        }

        if (mSocket != null && mSocket.connected()) {
            Log.d(TAG, "Socket đã kết nối.");
            // Nếu đã kết nối, không cần làm gì thêm, hoặc có thể kiểm tra logic join lại room nếu cần
            return;
        }

        try {
            IO.Options opts = new IO.Options();
            opts.forceNew = true; // Buộc tạo kết nối mới
            opts.reconnection = true; // Tự động kết nối lại
            opts.timeout = 10000; // Timeout 10 giây

            // --- CÁCH XÁC THỰC JWT - CẦN XÁC NHẬN VỚI BACKEND ---
            // Giả định: gửi token qua query parameter tên là "token"
            opts.query = "token=" + token;
            // Nếu backend yêu cầu header:
            // Map<String, List<String>> headers = new HashMap<>();
            // headers.put("Authorization", Collections.singletonList("Bearer " + token));
            // opts.extraHeaders = headers;

            mSocket = IO.socket(SOCKET_URL, opts);
            Log.d(TAG, "Đang kết nối tới Socket Server: " + SOCKET_URL);
            // Bạn sẽ đăng ký các listener (EVENT_CONNECT, etc.) ở nơi sử dụng socket (ví dụ ChatDetailActivity)
            mSocket.connect();

        } catch (URISyntaxException e) {
            Log.e(TAG, "Lỗi URI cú pháp Socket: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static synchronized void disconnectSocket() {
        if (mSocket != null) {
            // (Tùy chọn) Có thể emit sự kiện "leave_room" trước khi disconnect nếu cần
            // if (currentRoomId != null) {
            //     leaveRoom(currentRoomId);
            // }
            mSocket.disconnect();
            mSocket = null; // Quan trọng: đặt lại thành null để có thể kết nối lại
            currentRoomId = null;
            Log.d(TAG, "Socket đã ngắt kết nối.");
        }
    }

    public static boolean isConnected() {
        return mSocket != null && mSocket.connected();
    }

    // --- Các hàm tiện ích để emit sự kiện ---

    public static void joinRoom(String roomId, String userId, String userType) {
        if (mSocket != null && mSocket.connected() && roomId != null) {
            currentRoomId = roomId; // Lưu lại roomId hiện tại
            JSONObject data = new JSONObject();
            try {
                data.put("roomId", roomId);
                data.put("userId", userId);
                data.put("userType", userType); // "user" hoặc "admin"
                mSocket.emit("join_room", data);
                Log.d(TAG, "Đã gửi sự kiện join_room: " + data.toString());
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi tạo JSON cho join_room: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "Socket chưa kết nối hoặc roomId null, không thể join_room.");
        }
    }

    public static void sendMessage(String roomId, String senderId, String senderType, String message, String messageType) {
        if (mSocket != null && mSocket.connected()) {
            JSONObject data = new JSONObject();
            try {
                data.put("roomId", roomId);
                data.put("senderId", senderId);
                data.put("senderType", senderType);
                data.put("message", message);
                data.put("messageType", messageType); // "text" hoặc "image"
                mSocket.emit("send_message", data);
                Log.d(TAG, "Đã gửi sự kiện send_message: " + data.toString());
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi tạo JSON cho send_message: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "Socket chưa kết nối, không thể send_message.");
        }
    }

    public static void sendTypingEvent(String roomId, String userId, String userType, boolean isTyping) {
        if (mSocket != null && mSocket.connected()) {
            JSONObject data = new JSONObject();
            try {
                data.put("roomId", roomId);
                data.put("userId", userId);
                data.put("userType", userType);
                data.put("isTyping", isTyping);
                mSocket.emit("typing", data);
                // Log.d(TAG, "Đã gửi sự kiện typing: " + data.toString()); // Có thể log nhiều
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi tạo JSON cho typing: " + e.getMessage());
            }
        }
    }

    public static void closeRoom(String roomId, String closedByUserId) {
        if (mSocket != null && mSocket.connected()) {
            JSONObject data = new JSONObject();
            try {
                data.put("roomId", roomId);
                data.put("closedBy", closedByUserId); // ID của người đóng phòng
                mSocket.emit("close_room", data);
                Log.d(TAG, "Đã gửi sự kiện close_room: " + data.toString());
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi tạo JSON cho close_room: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "Socket chưa kết nối, không thể close_room.");
        }
    }

    // (Tùy chọn) Thêm hàm leave_room nếu backend có sự kiện này
    // public static void leaveRoom(String roomId) {
    //     if (mSocket != null && mSocket.connected() && roomId != null) {
    //         JSONObject data = new JSONObject();
    //         try {
    //             data.put("roomId", roomId);
    //             mSocket.emit("leave_room", data); // Cần backend hỗ trợ sự kiện này
    //             Log.d(TAG, "Đã gửi sự kiện leave_room cho: " + roomId);
    //         } catch (Exception e) {
    //             Log.e(TAG, "Lỗi khi tạo JSON cho leave_room: " + e.getMessage());
    //         }
    //     }
    // }
}

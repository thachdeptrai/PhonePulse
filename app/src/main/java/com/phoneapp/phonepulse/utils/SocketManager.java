package com.phoneapp.phonepulse.utils; // Hoặc package phù hợp

import android.text.TextUtils; // << ĐẢM BẢO IMPORT NÀY CÓ MẶT >>
import android.util.Log;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URISyntaxException;

public class SocketManager {
    private static final String TAG = "SocketManager";
    private static final String SOCKET_URL = "http://10.0.2.2:5000";

    private static Socket mSocket;

    public static synchronized Socket getSocket() {
        if (mSocket == null) {
            Log.e(TAG, "Socket chưa được khởi tạo. Gọi connectSocket() trước.");
        }
        return mSocket;
    }

    public static synchronized void connectSocket(String token) {
        if (TextUtils.isEmpty(token)) {
            Log.e(TAG, "Token không hợp lệ, không thể kết nối socket.");
            return;
        }

        if (mSocket != null && mSocket.connected()) {
            Log.d(TAG, "Socket đã kết nối (ID: " + mSocket.id() + "). Không thực hiện kết nối lại.");
            return;
        }

        if (mSocket != null) {
            Log.d(TAG, "Socket instance tồn tại nhưng không connected, sẽ tạo lại.");
            mSocket.disconnect();
            mSocket.off();
            mSocket = null;
        }

        try {
            IO.Options opts = new IO.Options();
            opts.reconnection = true;
            opts.timeout = 10000;
            opts.query = "token=" + token;

            Log.d(TAG, "Đang khởi tạo kết nối mới tới Socket Server: " + SOCKET_URL + " với token query.");
            mSocket = IO.socket(SOCKET_URL, opts);
            Log.d(TAG, "Socket instance được tạo. Gọi mSocket.connect() ngay.");
            mSocket.connect();

        } catch (URISyntaxException e) {
            Log.e(TAG, "Lỗi URI cú pháp Socket: " + e.getMessage());
            mSocket = null;
            throw new RuntimeException(e);
        }  catch (Exception e) {
            Log.e(TAG, "Lỗi không xác định khi tạo socket: " + e.getMessage(), e);
            mSocket = null;
        }
    }

    public static synchronized void disconnectSocket() {
        if (mSocket != null) {
            Log.d(TAG, "Yêu cầu ngắt kết nối socket (ID: " + (mSocket.id() != null ? mSocket.id() : "N/A") + ")");
            mSocket.disconnect();
            mSocket = null;
            Log.d(TAG, "Socket đã được yêu cầu ngắt kết nối và instance bị null hóa.");
        } else {
            Log.d(TAG, "disconnectSocket được gọi nhưng mSocket đã là null.");
        }
    }

    public static boolean isConnected() {
        return mSocket != null && mSocket.connected();
    }

    public interface CreateRoomCallback {
        void onSuccess(String roomId, String roomStatus);
        void onError(String message);
    }

    public static void createOrGetRoomOnSocket(String userId, final CreateRoomCallback callbackListener) {
        if (!isConnected()) {
            Log.w(TAG, "Cannot emit 'createRoom': Socket is not connected.");
            if (callbackListener != null) {
                callbackListener.onError("Socket not connected.");
            }
            return;
        }

        // <<<====== SỬA Ở ĐÂY: KHỞI TẠO BIẾN NGAY LẬP TỨC ======>>>
        final String createRoomEventName = "createRoom"; // Khởi tạo ngay với giá trị cố định
        // <<<====================================================>>>

        try {
            JSONObject payload = new JSONObject();
            payload.put("userId", userId);

            // createRoomEventName đã được khởi tạo ở trên

            Log.d(TAG, "Attempting to emit event '" + createRoomEventName + "' with payload: " + payload.toString() + " for socket ID: " + (mSocket != null ? mSocket.id() : "N/A"));

            mSocket.emit(createRoomEventName, payload, new Ack() {
                @Override
                public void call(Object... args) {
                    // Sử dụng final createRoomEventName từ scope ngoài
                    Log.d(TAG, "'" + createRoomEventName + "' ack received from server. Args length: " + args.length);
                    if (args.length > 0 && args[0] != null) {
                        Log.d(TAG, "Ack data from server [0]: " + args[0].toString());
                        if (args[0] instanceof JSONObject) {
                            JSONObject response = (JSONObject) args[0];
                            try {
                                String status = response.optString("status");
                                if ("success".equals(status) && response.has("room")) {
                                    JSONObject roomObj = response.getJSONObject("room");
                                    String receivedRoomId = roomObj.optString("roomId");
                                    String roomStatus = roomObj.optString("status");

                                    if (TextUtils.isEmpty(receivedRoomId)) {
                                        Log.e(TAG, "createRoom ack success but roomId is empty in room object!");
                                        if (callbackListener != null)
                                            callbackListener.onError("Server returned success but missing roomId.");
                                        return;
                                    }

                                    Log.i(TAG, "Room created/fetched successfully via socket. RoomID: " + receivedRoomId + ", Status: " + roomStatus);
                                    if (callbackListener != null) {
                                        callbackListener.onSuccess(receivedRoomId, roomStatus);
                                    }
                                } else {
                                    String errorMessage = response.optString("message", "Unknown error from createRoom ack");
                                    Log.e(TAG, "createRoom ack reported error or missing room data: " + errorMessage + ", Full response: " + response.toString());
                                    if (callbackListener != null) {
                                        callbackListener.onError(errorMessage);
                                    }
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "JSONException parsing createRoom ack: " + e.getMessage());
                                if (callbackListener != null) {
                                    callbackListener.onError("Error parsing server response for createRoom.");
                                }
                            }
                        } else {
                            Log.e(TAG, "createRoom ack data is not a JSONObject: " + args[0].toString());
                            if (callbackListener != null) {
                                callbackListener.onError("Invalid server response format for createRoom.");
                            }
                        }
                    } else {
                        Log.e(TAG, "createRoom ack received with no data or null data.");
                        if (callbackListener != null) {
                            callbackListener.onError("No response data from server for createRoom.");
                        }
                    }
                }
            });
            Log.i(TAG, "Event '" + createRoomEventName + "' emitted for user: " + userId);

        } catch (JSONException e) {
            // createRoomEventName đã được khởi tạo và có giá trị ở đây
            Log.e(TAG, "JSONException while creating payload for " + createRoomEventName + ": " + e.getMessage());
            if (callbackListener != null) {
                callbackListener.onError("Error creating request for createRoom.");
            }
        } catch (Exception e) {
            // createRoomEventName đã được khởi tạo và có giá trị ở đây
            Log.e(TAG, "Exception during mSocket.emit in " + createRoomEventName + ": " + e.getMessage(), e);
            if (callbackListener != null) {
                // Bạn có thể truyền tên sự kiện vào đây nếu muốn, hoặc một thông báo chung hơn
                callbackListener.onError("Error emitting " + createRoomEventName + " event.");
            }
        }
    }

    /* // PHƯƠNG THỨC joinRoom CŨ ĐÃ ĐƯỢC XÓA HOẶC COMMENT OUT
    public static void joinRoom(String roomId, String userId, String userType) {
        // ...
    }
    */

    public static void sendMessageViaSocket(String roomId, String senderId, String senderType, String message, String messageType) {
        if (!isConnected()) {
            Log.e(TAG, "Socket not connected, cannot send message via socket.");
            return;
        }
        try {
            JSONObject data = new JSONObject();
            data.put("roomId", roomId);
            data.put("senderId", senderId);
            data.put("senderType", senderType);
            data.put("message", message);
            data.put("messageType", messageType);

            mSocket.emit("send_message", data);
            Log.d(TAG, "Emitted 'send_message' event via socket: " + data.toString());
        } catch (JSONException e) {
            Log.e(TAG, "JSONException creating payload for sendMessageViaSocket: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception emitting sendMessageViaSocket: " + e.getMessage(), e);
        }
    }

    public static void sendMessage(String roomId, String senderId, String senderType, String message, String messageType) {
        if (!isConnected()) {
            Log.e(TAG, "Socket not connected, cannot send message (from original sendMessage).");
            return;
        }
        JSONObject data = new JSONObject();
        try {
            data.put("roomId", roomId);
            data.put("senderId", senderId);
            data.put("senderType", senderType);
            data.put("message", message);
            data.put("messageType", messageType);
            mSocket.emit("send_message", data);
            Log.d(TAG, "Emitted 'send_message' event via original sendMessage method: " + data.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error creating/emitting JSON for original sendMessage: " + e.getMessage());
        }
    }

    public static void sendTypingEvent(String roomId, String userId, String userType, boolean isTyping) {
        if (!isConnected()) {
            Log.w(TAG, "Cannot send typing event, socket not connected.");
            return;
        }
        try {
            JSONObject data = new JSONObject();
            data.put("roomId", roomId);
            data.put("userId", userId);
            data.put("userType", userType);
            data.put("isTyping", isTyping);
            mSocket.emit("typing", data);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tạo JSON cho typing: " + e.getMessage());
        }
    }

    public static void closeRoom(String roomId, String closedByUserId) {
        if (!isConnected()) {
            Log.w(TAG, "Cannot send close_room event, socket not connected.");
            return;
        }
        try {
            JSONObject data = new JSONObject();
            data.put("roomId", roomId);
            data.put("closedBy", closedByUserId);
            mSocket.emit("close_room", data);
            Log.d(TAG, "Đã gửi sự kiện close_room: " + data.toString());
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tạo JSON cho close_room: " + e.getMessage());
        }
    }
}

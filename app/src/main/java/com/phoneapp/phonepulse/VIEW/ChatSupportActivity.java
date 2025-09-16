package com.phoneapp.phonepulse.VIEW;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phoneapp.phonepulse.Adapter.MessageAdapter;
import com.phoneapp.phonepulse.R;
// import com.phoneapp.phonepulse.Response.ApiResponse; // Sẽ không dùng trực tiếp ở loadMessageHistory nữa
import com.phoneapp.phonepulse.Response.RoomApiResponse;
import com.phoneapp.phonepulse.Response.MessagesListApiResponse; // << IMPORT MỚI
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.ChatRoom;
import com.phoneapp.phonepulse.models.Message;
import com.phoneapp.phonepulse.request.UserIdRequest;
import com.phoneapp.phonepulse.utils.SocketManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatSupportActivity extends AppCompatActivity {

    private static final String TAG = "ChatSupportActivity";

    private Toolbar toolbarChatSupport;
    private RecyclerView rvMessages;
    private EditText etMessageInput;
    private ImageButton btnSendMessage;

    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private Socket mLocalSocket;
    private String userAuthToken;
    private String currentUserIdForChat;
    private String currentRoomIdForChat; // Sẽ được lấy từ API hoặc Intent

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_support);

        toolbarChatSupport = findViewById(R.id.toolbar_chat_support);
        setSupportActionBar(toolbarChatSupport);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        rvMessages = findViewById(R.id.rv_chat_messages);
        etMessageInput = findViewById(R.id.et_chat_message_input);
        btnSendMessage = findViewById(R.id.btn_chat_send_message);

        userAuthToken = getIntent().getStringExtra("AUTH_TOKEN");
        currentUserIdForChat = getIntent().getStringExtra("USER_ID_FOR_CHAT");
        String passedRoomId = getIntent().getStringExtra("ROOM_ID_FOR_CHAT");

        if (userAuthToken == null || userAuthToken.isEmpty()) {
            Toast.makeText(this, "Lỗi xác thực. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (currentUserIdForChat == null || currentUserIdForChat.isEmpty()) {
            Toast.makeText(this, "Lỗi thông tin người dùng. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (passedRoomId != null && !passedRoomId.isEmpty()) {
        }

        apiService = RetrofitClient.getApiService(userAuthToken);
        setupRecyclerView();

        if (passedRoomId != null && !passedRoomId.isEmpty()) {
            this.currentRoomIdForChat = passedRoomId;
            connectToSocket();
            loadMessageHistory(this.currentRoomIdForChat);
        } else {
            fetchOrCreateRoomAndLoadData(currentUserIdForChat);
        }

        btnSendMessage.setOnClickListener(v -> attemptSendMessage());
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList, currentUserIdForChat);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);
    }

    private void fetchOrCreateRoomAndLoadData(String userId) {
        Call<RoomApiResponse> call = apiService.createOrGetRoom(new UserIdRequest(userId));
        call.enqueue(new Callback<RoomApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<RoomApiResponse> call, @NonNull Response<RoomApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RoomApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getRoom() != null) {
                        ChatRoom roomInfo = apiResponse.getRoom();
                        if (roomInfo.getRoomId() != null && !roomInfo.getRoomId().isEmpty()) {
                            currentRoomIdForChat = roomInfo.getRoomId();
                            if (getSupportActionBar() != null && roomInfo.getStatus() != null) {
                                // getSupportActionBar().setTitle("Hỗ trợ (" + roomInfo.getStatus() + ")");
                            }
                            connectToSocket();
                            loadMessageHistory(currentRoomIdForChat);
                        } else {
                            Toast.makeText(ChatSupportActivity.this, "Không thể lấy thông tin ID phòng chat.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        String logMessage = "API call for fetch/create room was successful but RoomApiResponse indicates failure or room data is missing.";
                        if (apiResponse != null) {
                            if (!apiResponse.isSuccess()) logMessage += " apiResponse.isSuccess() is false.";
                            if (apiResponse.getRoom() == null) logMessage += " apiResponse.getRoom() is null.";
                        } else {
                            logMessage = "API call for fetch/create room was successful but response.body() (RoomApiResponse) is null.";
                        }
                        Log.e(TAG, logMessage);
                        Toast.makeText(ChatSupportActivity.this, "Không thể xử lý phản hồi từ server.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    handleApiError("fetch/create room", response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<RoomApiResponse> call, @NonNull Throwable t) {
                Toast.makeText(ChatSupportActivity.this, "Lỗi mạng khi lấy phòng chat.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadMessageHistory(String roomId) {
        if (roomId == null || roomId.isEmpty()) {
            return;
        }
        if (apiService == null) {
            return;
        }

        // SỬA ĐỔI BẮT ĐẦU TỪ ĐÂY
        apiService.getMessagesByRoomId(roomId).enqueue(new Callback<MessagesListApiResponse>() { // <-- Sửa kiểu ở đây
            @Override
            public void onResponse(@NonNull Call<MessagesListApiResponse> call, @NonNull Response<MessagesListApiResponse> response) { // <-- Sửa kiểu ở đây
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Message> history = response.body().getMessages(); // <-- Sửa ở đây, dùng getMessages()
                    if (history != null) {
                        Log.d(TAG, "Message history loaded: " + history.size() + " messages.");
                        messageList.clear();
                        messageList.addAll(history);
                        // Đảo ngược danh sách nếu API trả về theo thứ tự mới nhất -> cũ nhất
                        // Collections.reverse(messageList);
                        messageAdapter.notifyDataSetChanged();
                        if (!messageList.isEmpty()) {
                            rvMessages.scrollToPosition(messageList.size() - 1);
                        }
                    } else {
                    }
                } else {
                    handleApiError("load message history", response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MessagesListApiResponse> call, @NonNull Throwable t) { // <-- Sửa kiểu ở đây
                Toast.makeText(ChatSupportActivity.this, "Lỗi mạng khi tải lịch sử tin nhắn.", Toast.LENGTH_LONG).show();
            }
        });
        // SỬA ĐỔI KẾT THÚC Ở ĐÂY
    }

    private void handleApiError(String context, Response<?> response) {
        String defaultMessage = "Lỗi không xác định";
        String logMessage = "Lỗi API gọi lên   " + context + ". Code: " + response.code() + ", Message: " + response.message();
        if (response.errorBody() != null) {
            try {
                String errorBodyString = response.errorBody().string();
                logMessage += ", Error Body: " + errorBodyString;
                try {
                    JSONObject errorJson = new JSONObject(errorBodyString);
                    if (errorJson.has("message")) {
                        defaultMessage = errorJson.getString("message");
                    }
                } catch (JSONException e) {
                    // Không phải JSON hoặc không có trường message
                }
            } catch (Exception e) {
            }
        }
        Log.e(TAG, logMessage);
        Toast.makeText(ChatSupportActivity.this, "Lỗi " + context + ": " + defaultMessage + " (" + response.code() + ")", Toast.LENGTH_LONG).show();
    }

    private void connectToSocket() {
        if (currentRoomIdForChat == null || currentRoomIdForChat.isEmpty()) {
            Toast.makeText(this, "Lỗi phòng chat, không thể kết nối.", Toast.LENGTH_SHORT).show();
            return;
        }

        SocketManager.connectSocket(userAuthToken);
        mLocalSocket = SocketManager.getSocket();

        if (mLocalSocket == null) {
            Toast.makeText(this, "Lỗi khởi tạo kết nối chat.", Toast.LENGTH_LONG).show();
            return;
        }
        setupSocketListeners();
    }

    private void setupSocketListeners() {
        if (mLocalSocket == null) {
            return;
        }
        mLocalSocket.off(Socket.EVENT_CONNECT, onConnect);
        mLocalSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mLocalSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mLocalSocket.off("newMessage", onNewMessage);
        mLocalSocket.off("receive_message", onNewMessage);

        mLocalSocket.on(Socket.EVENT_CONNECT, onConnect);
        mLocalSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mLocalSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mLocalSocket.on("newMessage", onNewMessage);
        mLocalSocket.on("receive_message", onNewMessage);
    }

    private final Emitter.Listener onConnect = args -> runOnUiThread(() -> {
        Toast.makeText(getApplicationContext(), "Đã kết nối với hỗ trợ.", Toast.LENGTH_SHORT).show();

        if (currentRoomIdForChat != null && currentUserIdForChat != null) {
            Log.d(TAG, "Socket connected. Attempting to join room: " + currentRoomIdForChat + " with user: " + currentUserIdForChat);
            SocketManager.joinRoom(currentRoomIdForChat, currentUserIdForChat, "user");
        } else {
            Log.e(TAG, "Socket connected, but Room ID or User ID is null, cannot join room via socket.");
        }
    });

    private final Emitter.Listener onDisconnect = args -> runOnUiThread(() -> {
        Toast.makeText(getApplicationContext(), "Đã ngắt kết nối.", Toast.LENGTH_SHORT).show();
    });

    private final Emitter.Listener onConnectError = args -> runOnUiThread(() -> {
        String errorMsg = "Lỗi kết nối Socket: ";
        if (args.length > 0 && args[0] != null) {
            Object error = args[0];
            if (error instanceof Exception) {
                errorMsg += ((Exception) error).getMessage();
            } else {
                errorMsg += error.toString();
            }
        }
        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
    });

    private final Emitter.Listener onNewMessage = args -> runOnUiThread(() -> {
        if (args.length > 0 && args[0] instanceof JSONObject) {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "Raw Message Data from socket: " + data.toString());
            try {
                String msgId = data.optString("_id", java.util.UUID.randomUUID().toString());
                String senderId = data.optString("senderId");
                String text = data.optString("message");
                // Giả sử timestamp từ socket là String ISO 8601 hoặc milliseconds dạng String
                String timestampString = data.optString("timestamp", String.valueOf(System.currentTimeMillis()));
                String msgType = data.optString("messageType", "text");
                String receivedRoomId = data.optString("roomId");

                if (currentRoomIdForChat != null && currentRoomIdForChat.equals(receivedRoomId)) {
                    boolean messageExists = false;
                    for (Message existingMsg : messageList) {
                        if (existingMsg.getMessageId().equals(msgId)) {
                            messageExists = true;
                            break;
                        }
                    }

                    if (!messageExists) {
                        Message newMessage = new Message(
                                msgId,
                                senderId,
                                receivedRoomId,
                                text,
                                timestampString, // Truyền String
                                msgType,
                                senderId != null && senderId.equals(currentUserIdForChat)
                        );

                        messageList.add(newMessage);
                        messageAdapter.notifyItemInserted(messageList.size() - 1);
                        rvMessages.scrollToPosition(messageList.size() - 1);
                    } else {
                        Log.d(TAG, "Duplicate message received from socket or already added optimistically: " + msgId);
                    }
                } else {
                    Log.w(TAG, "Received message for a different room via socket. Current: " + currentRoomIdForChat + ", Received for: " + receivedRoomId);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error parsing new message JSON from socket or adding to adapter", e);
            }
        }
    });

    private void attemptSendMessage() {
        String messageText = etMessageInput.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        if (!SocketManager.isConnected() || mLocalSocket == null) {
            Toast.makeText(this, "Chưa kết nối. Vui lòng đợi.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentRoomIdForChat == null || currentUserIdForChat == null) {
            Toast.makeText(this, "Lỗi thông tin phòng/người dùng.", Toast.LENGTH_SHORT).show();
            return;
        }

        String tempMessageId = "temp_" + java.util.UUID.randomUUID().toString();
        // Gửi timestamp là String ISO 8601 UTC để nhất quán
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US);
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        String currentTimeString = sdf.format(new java.util.Date());

        Message sentMessage = new Message(
                tempMessageId,
                currentUserIdForChat,
                currentRoomIdForChat,
                messageText,
                currentTimeString, // Truyền String
                "text",
                true
        );
        messageList.add(sentMessage);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.scrollToPosition(messageList.size() - 1);
        etMessageInput.setText("");

        SocketManager.sendMessage(currentRoomIdForChat, currentUserIdForChat, "user", messageText, "text");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocalSocket != null) {
            mLocalSocket.off(Socket.EVENT_CONNECT, onConnect);
            mLocalSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
            mLocalSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mLocalSocket.off("newMessage", onNewMessage);
            mLocalSocket.off("receive_message", onNewMessage);
            SocketManager.disconnectSocket();
            mLocalSocket = null;
        }
    }
}

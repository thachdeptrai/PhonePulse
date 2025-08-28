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
// import com.phoneapp.phonepulse.Response.RoomApiResponse; // Không dùng nữa nếu lấy room qua socket
import com.phoneapp.phonepulse.Response.MessagesListApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
// import com.phoneapp.phonepulse.models.ChatRoom; // Không dùng nữa nếu lấy room qua socket
import com.phoneapp.phonepulse.models.Message;
// import com.phoneapp.phonepulse.request.UserIdRequest; // Không dùng nữa nếu lấy room qua socket
import com.phoneapp.phonepulse.utils.SocketManager;

import org.json.JSONException; // Giữ lại nếu handleApiError dùng
import org.json.JSONObject;   // Giữ lại nếu handleApiError dùng

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatSupportActivity extends AppCompatActivity {

    private static final String TAG = "ChatSupportActivity";
    private static final String SOCKET_EVENT_NEW_MESSAGE = "receive_message"; // Khớp với server

    private Toolbar toolbarChatSupport;
    private RecyclerView rvMessages;
    private EditText etMessageInput;
    private ImageButton btnSendMessage;

    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private Socket mLocalSocket;
    private String userAuthToken;
    private String currentUserIdForChat;
    private String currentRoomIdForChat; // Sẽ được lấy từ callback của SocketManager.createOrGetRoomOnSocket

    private ApiService apiService; // Vẫn dùng để loadMessageHistory

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_support);
        Log.d(TAG, "onCreate: Activity starting.");

        // --- Setup UI ---
        toolbarChatSupport = findViewById(R.id.toolbar_chat_support);
        setSupportActionBar(toolbarChatSupport);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        rvMessages = findViewById(R.id.rv_chat_messages);
        etMessageInput = findViewById(R.id.et_chat_message_input);
        btnSendMessage = findViewById(R.id.btn_chat_send_message);

        // --- Lấy dữ liệu từ Intent ---
        userAuthToken = getIntent().getStringExtra("AUTH_TOKEN");
        currentUserIdForChat = getIntent().getStringExtra("USER_ID_FOR_CHAT");
        // String passedRoomIdFromIntent = getIntent().getStringExtra("ROOM_ID_FOR_CHAT"); // Có thể không cần dùng nữa

        // --- Kiểm tra dữ liệu cần thiết ---
        if (TextUtils.isEmpty(userAuthToken)) {
            Log.e(TAG, "onCreate: Auth token is missing. Cannot proceed.");
            Toast.makeText(this, "Lỗi xác thực. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (TextUtils.isEmpty(currentUserIdForChat)) {
            Log.e(TAG, "onCreate: User ID is missing. Cannot proceed.");
            Toast.makeText(this, "Lỗi thông tin người dùng. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Log.d(TAG, "onCreate: Auth Token: " + userAuthToken);
        Log.d(TAG, "onCreate: User ID for chat: " + currentUserIdForChat);

        // --- Khởi tạo ApiService và RecyclerView ---
        apiService = RetrofitClient.getApiService(userAuthToken);
        if (apiService == null) {
            Log.e(TAG, "onCreate: ApiService is null. Check RetrofitClient.");
            Toast.makeText(this, "Lỗi khởi tạo dịch vụ API.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setupRecyclerView();

        // --- Luồng mới: Kết nối Socket và sau đó emit 'createRoom' ---
        Log.d(TAG, "onCreate: Attempting to connect to socket to get/create room.");
        connectToSocket(); // Hàm này sẽ dẫn đến onConnect -> handleSocketConnectionSuccess -> emit 'createRoom'

        btnSendMessage.setOnClickListener(v -> attemptSendMessage());
        Log.d(TAG, "onCreate: Activity setup complete. Waiting for socket connection and room creation.");
    }

    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: Initializing.");
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList, currentUserIdForChat);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);
        Log.d(TAG, "setupRecyclerView: Initialized. Adapter: " + messageAdapter.hashCode() + ", List: " + messageList.hashCode());
    }

    // Bỏ phương thức fetchOrCreateRoomAndLoadData dùng API HTTP
    /*
    private void fetchOrCreateRoomAndLoadData(String userId) { ... }
    */

    private void loadMessageHistory(String roomId) {
        if (TextUtils.isEmpty(roomId)) {
            Log.e(TAG, "loadMessageHistory: Room ID is missing. Cannot load.");
            // Toast.makeText(this, "Không có ID phòng để tải lịch sử.", Toast.LENGTH_SHORT).show(); // Có thể thông báo
            return;
        }
        if (apiService == null) {
            Log.e(TAG, "loadMessageHistory: ApiService is null.");
            return;
        }

        Log.d(TAG, "loadMessageHistory: Loading history for room: " + roomId);
        apiService.getMessagesByRoomId(roomId).enqueue(new Callback<MessagesListApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<MessagesListApiResponse> call, @NonNull Response<MessagesListApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Message> history = response.body().getMessages();
                    if (history != null) {
                        Log.d(TAG, "loadMessageHistory: Success. Loaded " + history.size() + " messages for room " + roomId);
                        messageList.clear();
                        messageList.addAll(history);
                        messageAdapter.notifyDataSetChanged();
                        if (!messageList.isEmpty()) {
                            rvMessages.scrollToPosition(messageList.size() - 1);
                        }
                    } else {
                        Log.d(TAG, "loadMessageHistory: Success but message history list is null for room " + roomId);
                    }
                } else {
                    handleApiError("load message history (room: " + roomId + ")", response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MessagesListApiResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "loadMessageHistory: API call FAILED for room " + roomId + ": " + t.getMessage(), t);
                Toast.makeText(ChatSupportActivity.this, "Lỗi mạng khi tải lịch sử tin nhắn.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleApiError(String context, Response<?> response) {
        // ... (Giữ nguyên handleApiError của bạn)
        String defaultMessage = "Lỗi không xác định từ server";
        String logMessage = "handleApiError: Failed API call for " + context + ". Code: " + response.code() + ", Message: " + response.message();
        if (response.errorBody() != null) {
            try {
                String errorBodyString = response.errorBody().string();
                logMessage += ", Error Body: " + errorBodyString;
                try {
                    JSONObject errorJson = new JSONObject(errorBodyString);
                    if (errorJson.has("message")) {
                        defaultMessage = errorJson.getString("message");
                    } else if (errorJson.has("error")) {
                        defaultMessage = errorJson.getString("error");
                    }
                } catch (JSONException e) {
                    Log.w(TAG, "handleApiError: Error body for " + context + " is not valid JSON or lacks 'message' field: " + errorBodyString);
                }
            } catch (Exception e) {
                Log.e(TAG, "handleApiError: Error parsing error body for " + context, e);
            }
        }
        Log.e(TAG, logMessage);
        Toast.makeText(ChatSupportActivity.this, "Lỗi " + context + ": " + defaultMessage + " (" + response.code() + ")", Toast.LENGTH_LONG).show();
    }


    // <<<================ LUỒNG KẾT NỐI SOCKET VÀ LẤY/TẠO PHÒNG MỚI ================>>>
    private void connectToSocket() {
        // if (TextUtils.isEmpty(currentRoomIdForChat)) { // Không cần kiểm tra roomId ở đây nữa vì sẽ lấy qua socket
        //     Log.e(TAG, "connectToSocket: Room ID is missing."); // Thông tin này sẽ đến sau
        // }

        if (mLocalSocket != null && mLocalSocket.connected()) {
            Log.d(TAG, "connectToSocket: Socket is already connected (ID: " + mLocalSocket.id() + ").");
            // Nếu đã kết nối và chưa có currentRoomIdForChat (ví dụ, activity resume),
            // thì cần gọi handleSocketConnectionSuccess để emit 'createRoom'
            if (TextUtils.isEmpty(currentRoomIdForChat) && !TextUtils.isEmpty(currentUserIdForChat)) {
                Log.d(TAG, "connectToSocket: Socket connected, but no currentRoomIdForChat. Triggering create/get room flow.");
                handleSocketConnectionSuccess();
            } else if (!TextUtils.isEmpty(currentRoomIdForChat)){
                Log.d(TAG, "connectToSocket: Socket connected and currentRoomIdForChat ("+currentRoomIdForChat+") exists. Listeners should be set.");
                setupSocketListeners(); // Đảm bảo listeners được cập nhật
            }
            return;
        }

        Log.i(TAG, "connectToSocket: Attempting to connect socket via SocketManager...");
        SocketManager.connectSocket(userAuthToken); // SocketManager sẽ gọi mSocket.connect()
        mLocalSocket = SocketManager.getSocket();

        if (mLocalSocket == null) {
            Log.e(TAG, "connectToSocket: mLocalSocket is NULL after SocketManager.getSocket(). This should not happen if connectSocket was successful.");
            Toast.makeText(this, "Lỗi nghiêm trọng: Không thể khởi tạo socket.", Toast.LENGTH_LONG).show();
            finish(); // Có thể cần đóng activity nếu socket là thiết yếu
            return;
        }

        Log.d(TAG, "connectToSocket: Socket instance obtained (hash: " + mLocalSocket.hashCode() + "). Is connected: " + mLocalSocket.connected() + ". Setting up core listeners.");
        setupSocketListeners(); // Thiết lập các listener như EVENT_CONNECT, DISCONNECT, NEW_MESSAGE

        // Nếu SocketManager.connectSocket không tự động gọi mSocket.connect() (nhưng phiên bản SocketManager mới đã gọi)
        // thì bạn cần gọi ở đây. Với phiên bản SocketManager đã sửa, dòng này có thể không cần.
        // if (!mLocalSocket.connected()) {
        //     Log.d(TAG, "connectToSocket: SocketManager provided instance but it's not connected. Calling mLocalSocket.connect().");
        //     mLocalSocket.connect();
        // }
        // Logic lấy/tạo phòng sẽ được xử lý trong onConnect listener
    }

    private void setupSocketListeners() {
        if (mLocalSocket == null) {
            Log.e(TAG, "setupSocketListeners: Cannot setup, mLocalSocket is null.");
            return;
        }
        Log.d(TAG, "setupSocketListeners: Setting up for socket (hash: " + mLocalSocket.hashCode() + "). Current ID: " + (mLocalSocket.id() != null ? mLocalSocket.id() : "N/A"));

        mLocalSocket.off(Socket.EVENT_CONNECT, onConnect);
        mLocalSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mLocalSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mLocalSocket.off(SOCKET_EVENT_NEW_MESSAGE, onNewMessage);
        // Thêm các listener khác nếu có, ví dụ 'room_closed'
        mLocalSocket.off("room_closed", onRoomClosed);


        mLocalSocket.on(Socket.EVENT_CONNECT, onConnect);
        mLocalSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mLocalSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mLocalSocket.on(SOCKET_EVENT_NEW_MESSAGE, onNewMessage);
        mLocalSocket.on("room_closed", onRoomClosed); // Lắng nghe sự kiện phòng bị đóng

        Log.d(TAG, "setupSocketListeners: Listeners registered.");
    }

    // Listener cho sự kiện kết nối socket thành công
    private final Emitter.Listener onConnect = args -> runOnUiThread(() -> {
        if (mLocalSocket == null) {
            Log.e(TAG, "onConnect: mLocalSocket is null at time of event!");
            return;
        }
        Log.i(TAG, "==> Socket EVENT_CONNECT: Connected! Session ID: " + mLocalSocket.id() + ", Hash: " + mLocalSocket.hashCode());
        Toast.makeText(getApplicationContext(), "Đã kết nối chat server.", Toast.LENGTH_SHORT).show();

        // Sau khi kết nối, nếu chưa có roomId, thì emit 'createRoom' để lấy/tạo và join phòng
        if (TextUtils.isEmpty(currentRoomIdForChat) && !TextUtils.isEmpty(currentUserIdForChat)) {
            Log.d(TAG, "onConnect: No currentRoomIdForChat. Triggering create/get room flow.");
            handleSocketConnectionSuccess();
        } else if (!TextUtils.isEmpty(currentRoomIdForChat)) {
            Log.d(TAG, "onConnect: Socket reconnected and currentRoomIdForChat ("+currentRoomIdForChat+") already exists. Re-validating room or ensuring joined.");
            // Có thể cần emit lại createRoom để server join lại nếu session socket mới,
            // server mới của bạn đã join khi createRoom.
            // Để đảm bảo, gọi lại handleSocketConnectionSuccess, server sẽ trả về phòng hiện có.
            handleSocketConnectionSuccess();
        } else {
            Log.w(TAG, "onConnect: currentUserIdForChat is missing, cannot proceed with room creation.");
        }
    });

    // Hàm được gọi sau khi socket kết nối thành công (hoặc nếu đã kết nối mà chưa có room)
    private void handleSocketConnectionSuccess() {
        Log.d(TAG, "handleSocketConnectionSuccess: Socket connected/ready. UserID: " + currentUserIdForChat);
        if (TextUtils.isEmpty(currentUserIdForChat)) {
            Log.e(TAG, "handleSocketConnectionSuccess: currentUserIdForChat is missing! Cannot create/get room.");
            Toast.makeText(this, "Lỗi thông tin người dùng để vào phòng chat.", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "handleSocketConnectionSuccess: Calling SocketManager.createOrGetRoomOnSocket for user: " + currentUserIdForChat);
        SocketManager.createOrGetRoomOnSocket(currentUserIdForChat, new SocketManager.CreateRoomCallback() {
            @Override
            public void onSuccess(String receivedRoomId, String roomStatus) {
                runOnUiThread(() -> {
                    Log.i(TAG, "SocketManager.createOrGetRoomOnSocket SUCCESS! RoomID: " + receivedRoomId + ", Status: " + roomStatus + ". Old RoomID: " + currentRoomIdForChat);
                    if (TextUtils.isEmpty(receivedRoomId)) {
                        Log.e(TAG, "createOrGetRoomOnSocket success callback but receivedRoomId is empty!");
                        Toast.makeText(ChatSupportActivity.this, "Lỗi: Server trả về ID phòng trống.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Chỉ load lịch sử nếu roomId thay đổi hoặc chưa có
                    boolean loadHistoryNeeded = TextUtils.isEmpty(currentRoomIdForChat) || !currentRoomIdForChat.equals(receivedRoomId);

                    currentRoomIdForChat = receivedRoomId; // << CẬP NHẬT ROOM ID CHÍNH THỨC
                    Log.i(TAG, "Updated currentRoomIdForChat to: " + currentRoomIdForChat);

                    // Cập nhật tiêu đề toolbar nếu muốn
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle("Hỗ trợ (Phòng " + roomStatus + ")");
                    }

                    if (loadHistoryNeeded) {
                        Log.d(TAG, "Room ID obtained/changed, loading message history for: " + currentRoomIdForChat);
                        loadMessageHistory(currentRoomIdForChat);
                    } else {
                        Log.d(TAG, "Room ID ("+currentRoomIdForChat+") confirmed, history should be current or reloading if needed by other logic.");
                        // Nếu không load history, vẫn cần đảm bảo RecyclerView được cập nhật nếu có tin nhắn mới chờ
                        messageAdapter.notifyDataSetChanged();
                        if(!messageList.isEmpty()) rvMessages.scrollToPosition(messageList.size() - 1);
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Log.e(TAG, "SocketManager.createOrGetRoomOnSocket ERROR: " + message);
                    Toast.makeText(ChatSupportActivity.this, "Lỗi vào phòng chat: " + message, Toast.LENGTH_LONG).show();
                    // Xử lý lỗi, ví dụ: đóng activity hoặc cho phép thử lại
                    // finish();
                });
            }
        });
    }


    private final Emitter.Listener onDisconnect = args -> runOnUiThread(() -> {
        // ... (Giữ nguyên onDisconnect của bạn)
        String reason = (args.length > 0 && args[0] != null) ? args[0].toString() : "N/A";
        Log.w(TAG, "==> Socket EVENT_DISCONNECT: Disconnected! Reason: " + reason + (mLocalSocket != null ? ", Socket hash: " + mLocalSocket.hashCode() : ""));
        Toast.makeText(getApplicationContext(), "Đã ngắt kết nối chat server.", Toast.LENGTH_SHORT).show();
    });

    private final Emitter.Listener onConnectError = args -> runOnUiThread(() -> {
        // ... (Giữ nguyên onConnectError của bạn)
        String errorMsg = "Lỗi kết nối Socket: ";
        if (args.length > 0 && args[0] != null) {
            Object error = args[0];
            Log.e(TAG, "==> Socket EVENT_CONNECT_ERROR: Raw error data: " + error.toString() + (mLocalSocket != null ? ", Socket hash: " + mLocalSocket.hashCode() : ""));
            if (error instanceof Exception) {
                errorMsg += ((Exception) error).getMessage();
                Log.e(TAG, "onConnectError: Socket Connection Exception: ", (Exception) error);
            } else if (error instanceof JSONObject) {
                try {
                    JSONObject errJson = (JSONObject) error;
                    errorMsg += errJson.optString("message", error.toString());
                    Log.e(TAG, "onConnectError: Socket Connection Error (JSONObject): " + errJson.toString());
                } catch (Exception e) {
                    Log.e(TAG, "onConnectError: Error parsing JSONObject from connect_error", e);
                    errorMsg += error.toString();
                }
            } else {
                errorMsg += error.toString();
            }
        } else {
            Log.e(TAG, "==> Socket EVENT_CONNECT_ERROR: Error with no specific data." + (mLocalSocket != null ? ", Socket hash: " + mLocalSocket.hashCode() : ""));
            errorMsg += "Không có thông tin chi tiết.";
        }
        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
    });

    private final Emitter.Listener onRoomClosed = args -> runOnUiThread(() -> {
        Log.i(TAG, "==> Socket EVENT 'room_closed' RECEIVED!");
        if (args.length > 0 && args[0] instanceof JSONObject) {
            JSONObject data = (JSONObject) args[0];
            String closedBy = data.optString("closedBy", "N/A");
            String timestamp = data.optString("timestamp", "N/A");
            Log.d(TAG, "Room closed by: " + closedBy + " at " + timestamp + ". Current room: " + currentRoomIdForChat + ", Received room (if any in payload): " + data.optString("roomId"));

            Toast.makeText(this, "Phòng chat đã bị đóng bởi quản trị viên.", Toast.LENGTH_LONG).show();
            // Xử lý UI, ví dụ: vô hiệu hóa input, hiển thị thông báo, điều hướng đi
            etMessageInput.setEnabled(false);
            btnSendMessage.setEnabled(false);
            // Có thể finish() activity sau một khoảng delay hoặc khi người dùng xác nhận
            // finish();
        } else {
            Log.w(TAG, "'room_closed' event data is not a JSONObject or is empty.");
        }
    });

    private final Emitter.Listener onNewMessage = args -> runOnUiThread(() -> {
        Log.i(TAG, "==> Socket EVENT '" + SOCKET_EVENT_NEW_MESSAGE + "' RECEIVED!" + (mLocalSocket != null ? " Socket hash: " + mLocalSocket.hashCode() : ""));
        if (TextUtils.isEmpty(currentRoomIdForChat)) {
            Log.w(TAG, "onNewMessage: Received a message but currentRoomIdForChat is not set yet. Ignoring. Data: " + (args.length > 0 ? args[0] : "null"));
            return; // Chưa có phòng thì không xử lý tin nhắn
        }
        // ... (Phần còn lại của onNewMessage giữ nguyên như bản hoàn chỉnh trước, sử dụng notifyDataSetChanged)
        if (args.length > 0 && args[0] != null) {
            Log.d(TAG, "onNewMessage: Raw data from socket: " + args[0].toString());

            if (!(args[0] instanceof JSONObject)) {
                Log.w(TAG, "onNewMessage: Data is not a JSONObject. Data: " + args[0].toString());
                return;
            }

            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "onNewMessage: Parsed JSONObject: " + data.toString());

            try {
                String msgId = data.optString("_id"); // Ưu tiên _id
                if (TextUtils.isEmpty(msgId)) {
                    msgId = "socket_msg_fallback_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
                    Log.w(TAG, "onNewMessage: Received message from socket without '_id'. Generated temp ID: " + msgId);
                }

                String senderId = data.optString("senderId");
                String text = data.optString("message");
                String timestampString = data.optString("timestamp", String.valueOf(System.currentTimeMillis()));
                String msgType = data.optString("messageType", "text");
                String receivedRoomId = data.optString("roomId");

                Log.d(TAG, String.format("onNewMessage: Parsed: msgId=%s, senderId=%s, text=%s, timestamp=%s, msgType=%s, receivedRoomId=%s",
                        msgId, senderId, text, timestampString, msgType, receivedRoomId));

                if (TextUtils.isEmpty(senderId) || TextUtils.isEmpty(text) || TextUtils.isEmpty(receivedRoomId)) {
                    Log.e(TAG, "onNewMessage: Essential message data missing. Payload: " + data.toString());
                    return;
                }

                Log.d(TAG, "onNewMessage: Checking room match: current='" + currentRoomIdForChat + "', received='" + receivedRoomId + "'");
                if (currentRoomIdForChat.equals(receivedRoomId)) { // Phải dùng equals cho String

                    boolean messageExists = false;
                    if (!TextUtils.isEmpty(msgId) && !msgId.startsWith("socket_msg_fallback_")) {
                        for (Message existingMsg : messageList) {
                            if (existingMsg.getMessageId().equals(msgId)) {
                                messageExists = true;
                                Log.d(TAG, "onNewMessage: Message with ID '" + msgId + "' already exists.");
                                break;
                            }
                        }
                    }

                    if (!messageExists) {
                        Message newMessage = new Message(
                                msgId,
                                senderId,
                                receivedRoomId,
                                text,
                                timestampString,
                                msgType,
                                senderId.equals(currentUserIdForChat)
                        );

                        Log.d(TAG, "onNewMessage: Adding new message. List size BEFORE: " + messageList.size());
                        messageList.add(newMessage);
                        Log.d(TAG, "onNewMessage: List size AFTER add: " + messageList.size());
                        Log.d(TAG, "onNewMessage: Calling notifyDataSetChanged(). Adapter: " + messageAdapter.hashCode() + ", List: " + messageList.hashCode());
                        messageAdapter.notifyDataSetChanged();

                        if (!messageList.isEmpty()) {
                            rvMessages.post(() -> {
                                if (!messageList.isEmpty() && rvMessages != null && messageAdapter != null && messageAdapter.getItemCount() > 0) {
                                    Log.d(TAG, "onNewMessage: Scrolling to position " + (messageList.size() - 1));
                                    rvMessages.scrollToPosition(messageList.size() - 1);
                                }
                            });
                        }
                        Log.i(TAG, "onNewMessage: Processed and UI updated for: '" + text + "' from: " + senderId);
                    }
                } else {
                    Log.w(TAG, "onNewMessage: Received message for different room. Current: '" + currentRoomIdForChat + "', Received for: '" + receivedRoomId + "'. Ignoring.");
                }

            } catch (Exception e) {
                Log.e(TAG, "onNewMessage: Error processing new message JSON or adding to adapter", e);
                Toast.makeText(this, "Lỗi xử lý tin nhắn mới.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.w(TAG, "onNewMessage: Event '" + SOCKET_EVENT_NEW_MESSAGE + "' received, but data is null or args empty.");
        }
    });

    private void attemptSendMessage() {
        String messageText = etMessageInput.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Vui lòng nhập tin nhắn.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mLocalSocket == null || !mLocalSocket.connected() || TextUtils.isEmpty(currentRoomIdForChat) || TextUtils.isEmpty(currentUserIdForChat)) {
            Log.w(TAG, "attemptSendMessage: Cannot send. Socket connected: " + (mLocalSocket != null && mLocalSocket.connected()) +
                    ", RoomID set: " + !TextUtils.isEmpty(currentRoomIdForChat) +
                    ", UserID set: " + !TextUtils.isEmpty(currentUserIdForChat));
            Toast.makeText(this, "Chưa thể gửi tin nhắn. Kiểm tra kết nối hoặc thông tin phòng.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gửi qua sự kiện socket 'send_message' mà server mới hỗ trợ
        Log.d(TAG, "attemptSendMessage: Sending message via SocketManager.sendMessageViaSocket (event 'send_message')");
        SocketManager.sendMessageViaSocket(currentRoomIdForChat, currentUserIdForChat, "user", messageText, "text");

        // Xóa input sau khi gửi
        etMessageInput.setText("");

        // Không cần thêm vào UI lạc quan ở đây nữa nếu server emit lại 'receive_message' nhanh chóng
        // và onNewMessage sẽ xử lý việc hiển thị. Điều này tránh trùng lặp.
        // Nếu muốn hiển thị lạc quan, bạn cần logic phức tạp hơn để khớp tin nhắn tạm thời với tin nhắn từ server.
    }


    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "onSupportNavigateUp: Navigating back.");
        // Cân nhắc ngắt kết nối socket ở đây nếu ChatSupportActivity không được giữ trong backstack
        // và bạn muốn giải phóng tài nguyên ngay khi người dùng bấm back.
        // disconnectBeforeFinish(); // Hàm tùy chỉnh nếu cần
        onBackPressed();
        return true;
    }

    private void disconnectBeforeFinish() {
        Log.d(TAG, "disconnectBeforeFinish: Activity is likely finishing, disconnecting socket.");
        if (mLocalSocket != null) {
            // Không cần gỡ listener ở đây nếu onDestroy sẽ làm
            if (mLocalSocket.connected()) {
                Log.d(TAG, "disconnectBeforeFinish: Calling mLocalSocket.disconnect().");
                // mLocalSocket.disconnect(); // Có thể không cần nếu onDestroy sẽ gọi SocketManager.disconnectSocket()
            }
        }
        SocketManager.disconnectSocket(); // Gọi hàm của manager để quản lý việc ngắt kết nối
        mLocalSocket = null;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: Activity is being destroyed.");
        // Gỡ bỏ listener và yêu cầu ngắt kết nối qua SocketManager
        if (mLocalSocket != null) {
            Log.d(TAG, "onDestroy: Removing listeners from mLocalSocket (hash: " + mLocalSocket.hashCode() + ")");
            mLocalSocket.off(Socket.EVENT_CONNECT, onConnect);
            mLocalSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
            mLocalSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mLocalSocket.off(SOCKET_EVENT_NEW_MESSAGE, onNewMessage);
            mLocalSocket.off("room_closed", onRoomClosed);
        }
        // Yêu cầu SocketManager ngắt kết nối. SocketManager sẽ kiểm tra và ngắt nếu cần.
        Log.d(TAG, "onDestroy: Calling SocketManager.disconnectSocket().");
        SocketManager.disconnectSocket();
        mLocalSocket = null; // Quan trọng: giải phóng tham chiếu ở Activity
        Log.d(TAG, "onDestroy: ChatSupportActivity fully destroyed.");
    }
}


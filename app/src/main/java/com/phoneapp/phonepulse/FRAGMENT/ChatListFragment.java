package com.phoneapp.phonepulse.FRAGMENT;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
// Xóa import Button vì không còn dùng btnStartNewChat
// import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phoneapp.phonepulse.Adapter.ChatOverviewAdapter;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.RoomApiResponse;
import com.phoneapp.phonepulse.VIEW.ChatSupportActivity;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.ChatOverviewItem;
import com.phoneapp.phonepulse.models.ChatRoom;
import com.phoneapp.phonepulse.models.User;

import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.request.UserIdRequest;
import com.phoneapp.phonepulse.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatListFragment extends Fragment implements ChatOverviewAdapter.OnChatItemClickListener {

    private static final String TAG = "ChatListFragment";
    private ApiService apiService;
    private RecyclerView rvChatList;
    private ChatOverviewAdapter chatAdapter;
    private List<ChatOverviewItem> chatOverviewItemsList;

    public ChatListFragment() {
        // Constructor rỗng là bắt buộc cho Fragment
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvChatList = view.findViewById(R.id.rv_chat_list); // Đảm bảo ID này đúng trong XML
        setupRecyclerView();

        String token = Constants.getToken(requireContext().getApplicationContext());
        if (token != null && !token.isEmpty()) {
            apiService = RetrofitClient.getApiService(token);
            loadChatOverview(token);
        } else {
            Log.w(TAG, "Token is null or empty. Cannot load chat overview.");
            Toast.makeText(getContext(), "Vui lòng đăng nhập để xem tin nhắn.", Toast.LENGTH_LONG).show();
            // Có thể chuyển người dùng đến màn hình đăng nhập ở đây
        }
    }

    private void setupRecyclerView() {
        chatOverviewItemsList = new ArrayList<>();
        chatAdapter = new ChatOverviewAdapter(chatOverviewItemsList, this);
        rvChatList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChatList.setAdapter(chatAdapter);
    }

    private void loadChatOverview(String token) {
        if (getContext() == null || getActivity() == null) {
            Log.e(TAG, "Context or Activity is null, cannot load chat overview.");
            return;
        }
        if (apiService == null) {
            Log.e(TAG, "ApiService is null. Cannot load chat overview.");
            Toast.makeText(getContext(), "Lỗi dịch vụ. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = requireActivity().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        // Sử dụng key "user_id" như hiện tại, nếu bạn có hằng số trong Constants.java cho key này, hãy dùng nó.
        String userIdFromPrefs = prefs.getString("user_id", null);

        if (userIdFromPrefs != null && !userIdFromPrefs.isEmpty()) {
            Log.d(TAG, "User ID found in SharedPreferences: " + userIdFromPrefs);
            fetchOrCreateChatRoom(token, userIdFromPrefs);
        } else {
            Log.d(TAG, "User ID not found in SharedPreferences. Fetching from API...");
            fetchUserProfileThenRoom(token);
        }
    }

    private void fetchUserProfileThenRoom(String token) {
        apiService.getProfile().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    User user = response.body().getData();
                    if (user != null && user.getId() != null && !user.getId().isEmpty()) {
                        String fetchedUserId = user.getId();
                        Log.d(TAG, "User ID fetched from API: " + fetchedUserId);

                        SharedPreferences prefs = requireActivity().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
                        // Sử dụng key "user_id" để lưu.
                        prefs.edit().putString("user_id", fetchedUserId).apply();
                        Log.d(TAG, "User ID saved to SharedPreferences.");

                        fetchOrCreateChatRoom(token, fetchedUserId);
                    } else {
                        Log.e(TAG, "User data or User ID is null/empty in API profile response.");
                        Toast.makeText(getContext(), "Không thể lấy ID người dùng từ hồ sơ.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to fetch profile: " + response.code() + " - " + response.message());
                    Toast.makeText(getContext(), "Lỗi khi lấy thông tin người dùng: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                Log.e(TAG, "API call failed for getProfile: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Lỗi mạng khi lấy thông tin người dùng.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchOrCreateChatRoom(String token, String userId) {
        if (apiService == null) {
            Log.e(TAG, "ApiService is null when trying to fetch/create chat room.");
            Toast.makeText(getContext(), "Lỗi dịch vụ, không thể tải phòng chat.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Fetching or creating chat room for User ID: " + userId);
        apiService.createOrGetRoom(new UserIdRequest(userId)).enqueue(new Callback<RoomApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<RoomApiResponse> call, @NonNull Response<RoomApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RoomApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getRoom() != null) {
                        ChatRoom room = apiResponse.getRoom();
                        if (room != null && room.getRoomId() != null && !room.getRoomId().isEmpty()) {
                            Log.d(TAG, "Chat room obtained: " + room.getRoomId() + ", Status: " + room.getStatus());
                            // Xử lý chat room, ví dụ: cập nhật UI
                            ChatOverviewItem item = convertChatRoomToOverviewItem(room);
                            // Kiểm tra xem item đã tồn tại trong list chưa dựa trên roomId
                            boolean found = false;
                            for (int i = 0; i < chatOverviewItemsList.size(); i++) {
                                if (chatOverviewItemsList.get(i).getRoomId().equals(item.getRoomId())) {
                                    chatOverviewItemsList.set(i, item); // Cập nhật item đã tồn tại
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                chatOverviewItemsList.add(item); // Thêm item mới
                            }
                            // Sắp xếp lại danh sách (ví dụ: theo updatedAt giảm dần)
                            // Collections.sort(chatOverviewItemsList, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                            chatAdapter.notifyDataSetChanged(); // Hoặc sử dụng DiffUtil để tối ưu hơn

                        } else {
                            String errorDetails = "ChatRoom is valid, but RoomId is null or empty.";
                            if (room == null) {
                                errorDetails = "ChatRoom object (room) itself is null after apiResponse.getRoom().";
                            } else if (room.getRoomId() == null) {
                                errorDetails = "ChatRoom.getRoomId() is null.";
                            }
                            Log.e(TAG, "Error processing chat room: " + errorDetails);
                            // Toast.makeText(getContext(), "Lỗi xử lý thông tin phòng chat.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String logMsg = "API call successful but response indicates failure or room data is missing in RoomApiResponse.";
                        if (apiResponse != null && !apiResponse.isSuccess()) logMsg += " 'success' field is false.";
                        if (apiResponse != null && apiResponse.getRoom() == null) logMsg += " 'room' object (from getRoom()) is null.";
                        if (apiResponse == null) logMsg = "API call successful but response.body() or RoomApiResponse itself is null.";
                        Log.e(TAG, logMsg);
                        Toast.makeText(getContext(), "Không thể lấy thông tin phòng chat từ phản hồi.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorBodyString = "";
                     if (response.errorBody() != null) {
                        try {
                            errorBodyString = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading errorBody", e);
                        }
                    }
                    Log.e(TAG, "Failed to fetch/create chat room: " + response.code() + " - " + response.message() + " - " + errorBodyString);
                    Toast.makeText(getContext(), "Lỗi khi tải phòng chat: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RoomApiResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "API call failed for createOrGetRoom: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Lỗi mạng khi tải phòng chat.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private ChatOverviewItem convertChatRoomToOverviewItem(ChatRoom room) {
        String displayName = "Hỗ trợ khách hàng"; // Tên mặc định
        if ("waiting".equalsIgnoreCase(room.getStatus()) && room.getAdminId() == null) {
            displayName = "Đang chờ hỗ trợ";
        } else if (room.getAdminId() != null) {
            displayName = "Hỗ trợ khách hàng";
        }

        String lastMessagePreview = "Nhấn để xem tin nhắn"; // Placeholder

        String updatedAtString = room.getUpdatedAt();
        long updatedAtTimestamp = 0L; // Giá trị mặc định

        if (updatedAtString != null && !updatedAtString.isEmpty()) {
            try {
                // Định dạng này phải khớp chính xác với chuỗi ngày tháng từ API
                // Ví dụ: "2025-08-25T15:03:54.928Z"
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Quan trọng: 'Z' là UTC
                Date date = sdf.parse(updatedAtString);
                if (date != null) {
                    updatedAtTimestamp = date.getTime();
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing updatedAt string: " + updatedAtString, e);
                // Giữ giá trị mặc định hoặc có thể đặt là System.currentTimeMillis()
                // Hoặc xử lý lỗi theo cách khác nếu cần
            }
        }

        return new ChatOverviewItem(
                room.getRoomId(),
                displayName,
                lastMessagePreview,
                updatedAtTimestamp, // Sử dụng timestamp đã parse (long)
                room.getStatus(),
                room.getUserId(),
                room.getAdminId()
        );
    }

    @Override
    public void onChatItemClick(ChatOverviewItem item) {
        Log.d(TAG, "Chat item clicked: " + item.getRoomId());
        if (getContext() == null) {
             Log.e(TAG, "Context is null in onChatItemClick.");
             return;
        }
        String token = Constants.getToken(requireContext().getApplicationContext());
        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(), "Lỗi xác thực. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (item.getUserId() == null || item.getUserId().isEmpty()) {
            Toast.makeText(getContext(), "Lỗi thông tin người dùng.", Toast.LENGTH_SHORT).show();
            return;
        }

        launchChatSupportActivity(token, item.getUserId(), item.getRoomId());
    }

    private void launchChatSupportActivity(String token, String userId, String roomId) {
        if (getActivity() == null) {
            Log.e(TAG, "Activity is null when trying to launch ChatSupportActivity.");
            return;
        }
        if (roomId == null || roomId.isEmpty()) {
            Log.e(TAG, "Room ID is null or empty, cannot launch chat.");
            Toast.makeText(getContext(), "Không thể mở phòng chat.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Launching ChatSupportActivity with Token, UserID: " + userId + ", RoomID: " + roomId);
        Intent intent = new Intent(getActivity(), ChatSupportActivity.class);
        intent.putExtra("AUTH_TOKEN", token);
        intent.putExtra("USER_ID_FOR_CHAT", userId);
        intent.putExtra("ROOM_ID_FOR_CHAT", roomId);
        startActivity(intent);
    }
}

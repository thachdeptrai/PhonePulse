package com.phoneapp.phonepulse.VIEW;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Order;
import com.phoneapp.phonepulse.models.Cart;
import com.phoneapp.phonepulse.models.Variant;
import com.phoneapp.phonepulse.utils.Constants;
import com.phoneapp.phonepulse.request.OrderItem;
import com.phoneapp.phonepulse.request.CartRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MomoPaymentWebViewActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_QR = "extra_qr";
    private static final String TAG = "MomoPaymentWebView";
    private ApiService apiService;
    private WebView webView;
    private ImageView qrImage;
    private boolean hasHandledResult = false;
    private Handler pollHandler = new Handler();
    private int pollAttempts = 0;
    private static final int MAX_POLL_ATTEMPTS = 30; // ~30s

    private static final int REDIRECT_DELAY_MS = 3000; // 3 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_momo_payment_web_view);

        webView = findViewById(R.id.webView);
        qrImage = findViewById(R.id.qrImage);

        String url = getIntent().getStringExtra(EXTRA_URL);
        String qrUrl = getIntent().getStringExtra(EXTRA_QR);

        apiService = RetrofitClient.getApiService(Constants.getToken(this));

        if (url != null) {
            setupWebView(url);
        }

        if (qrUrl != null && !qrUrl.isEmpty()) {
            qrImage.setVisibility(View.VISIBLE);
            Bitmap qrBitmap = generateQRCode(qrUrl);
            if (qrBitmap != null) {
                qrImage.setImageBitmap(qrBitmap);
            }
        } else {
            qrImage.setVisibility(View.GONE);
        }
    }

    // Tạo QR code từ chuỗi
    private Bitmap generateQRCode(String text) {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder encoder = new BarcodeEncoder();
            return encoder.createBitmap(bitMatrix);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Cấu hình WebView và xử lý redirect MoMo
    private void setupWebView(String url) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                android.util.Log.d(TAG, "shouldOverrideUrlLoading() url=" + url);
                if (hasHandledResult) {
                    android.util.Log.d(TAG, "Guard active: hasHandledResult=true, ignoring further loads");
                    return true;
                }

                if (url.startsWith("momo_return://") || url.contains("payment-success") || url.contains("/success") || url.contains("/return")) {
                    Uri uri = Uri.parse(url);

                    String orderId = uri.getQueryParameter("orderId");
                    String resultCodeStr = uri.getQueryParameter("resultCode");
                    String message = uri.getQueryParameter("message");
                    String extraData = uri.getQueryParameter("extraData");
                    int resultCode = resultCodeStr != null ? Integer.parseInt(resultCodeStr) : -1;

                    hasHandledResult = true;
                    android.util.Log.d(TAG, "Detected success-like URL. Params => orderId=" + orderId + ", resultCode=" + resultCode + ", message=" + message + ", extraData=" + extraData);

                    // ✅ Nếu có đủ tham số thì xác nhận với server; nếu không, vẫn điều hướng để tránh treo
                    if (orderId != null && resultCode != -1) {
                        android.util.Log.d(TAG, "Calling handleMomoReturn API...");
                        apiService.handleMomoReturn(resultCode, orderId, message, extraData)
                            .enqueue(new Callback<ApiResponse<Order>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                                    android.util.Log.d(TAG, "handleMomoReturn onResponse: success=" + response.isSuccessful() + ", code=" + response.code());
                                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                        // Lấy dữ liệu order từ ApiResponse
                                        Order order = response.body().getData();

                                        Toast.makeText(MomoPaymentWebViewActivity.this,
                                                "Thanh toán thành công!", Toast.LENGTH_SHORT).show();

                                        // Cleanup tồn kho và giỏ hàng trước khi điều hướng
                                        if (order != null && order.getItems() != null && !order.getItems().isEmpty()) {
                                            performPostPaymentCleanup(order.getItems());
                                        } else {
                                            android.util.Log.w(TAG, "Order null hoặc không có items. Điều hướng thẳng.");
                                            navigateToHistory();
                                        }

                                    } else {
                                        String apiMsg = (response.body() != null ? response.body().getMessage() : "null body");
                                        android.util.Log.w(TAG, "handleMomoReturn API NOT success. msg=" + apiMsg);
                                        Toast.makeText(MomoPaymentWebViewActivity.this,
                                                "Thanh toán thành công nhưng tạo đơn thất bại!", Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                                    android.util.Log.e(TAG, "handleMomoReturn onFailure: " + t.getMessage(), t);
                                    Toast.makeText(MomoPaymentWebViewActivity.this,
                                            "Lỗi kết nối server: " + t.getMessage(), Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            });
                    } else {
                        android.util.Log.w(TAG, "Missing return params. Fallback navigate to history.");
                        // Không có tham số return — fallback: điều hướng thẳng để tránh treo
                        Toast.makeText(MomoPaymentWebViewActivity.this,
                                "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                        navigateToHistory();
                    }

                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                android.util.Log.d(TAG, "onPageFinished() url=" + url + ", title=" + view.getTitle());
                if (hasHandledResult) {
                    android.util.Log.d(TAG, "Guard active onPageFinished: hasHandledResult=true");
                    return;
                }
                String title = view.getTitle() != null ? view.getTitle().toLowerCase() : "";
                if (title.contains("thành công") || title.contains("success")) {
                    hasHandledResult = true;
                    android.util.Log.d(TAG, "Detected success by page title. Navigating to history.");
                    Toast.makeText(MomoPaymentWebViewActivity.this,
                            "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                    navigateToHistory();
                } else {
                    // Fallback: poll DOM text to detect success keyword on MoMo static success page
                    startSuccessPolling();
                }
            }
        });

        webView.loadUrl(url);
    }

    private void startSuccessPolling() {
        pollAttempts = 0;
        android.util.Log.d(TAG, "Start DOM polling for success text...");
        pollHandler.post(pollRunnable);
    }

    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (hasHandledResult) {
                android.util.Log.d(TAG, "Stop polling: already handled.");
                return;
            }
            if (pollAttempts >= MAX_POLL_ATTEMPTS) {
                android.util.Log.w(TAG, "Stop polling: max attempts reached without success.");
                return;
            }
            pollAttempts++;
            if (webView == null) return;
            webView.evaluateJavascript("(function(){try{return document.body && document.body.innerText || ''; }catch(e){return ''; }})()", value -> {
                String text = value != null ? value.replace("\\n", " ").replace("\"", "").toLowerCase() : "";
                android.util.Log.d(TAG, "Poll#" + pollAttempts + " bodyText.len=" + (text != null ? text.length() : 0));
                if (!hasHandledResult && text != null && (text.contains("thành công") || text.contains("payment successful") || text.contains("success"))) {
                    hasHandledResult = true;
                    android.util.Log.d(TAG, "Detected success by DOM text. Navigating to history.");
                    Toast.makeText(MomoPaymentWebViewActivity.this,
                            "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                    navigateToHistory();
                } else {
                    pollHandler.postDelayed(this, 1000);
                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            pollHandler.removeCallbacksAndMessages(null);
        } catch (Exception ignored) {}
    }

    // ===== Post-payment cleanup and navigation =====
    private void performPostPaymentCleanup(java.util.List<OrderItem> items) {
        android.util.Log.d(TAG, "Start post-payment cleanup for items: " + (items != null ? items.size() : 0));
        if (items == null || items.isEmpty()) {
            navigateToHistory();
            return;
        }

        final java.util.concurrent.CountDownLatch stockLatch = new java.util.concurrent.CountDownLatch(items.size());
        for (OrderItem item : items) {
            final String productId = item.getProductId();
            final String variantId = item.getVariantId();
            final int qty = item.getQuantity();
            if (productId == null || variantId == null || productId.trim().isEmpty() || variantId.trim().isEmpty()) {
                android.util.Log.w(TAG, "Skip stock update: missing productId/variantId for " + item.getName());
                stockLatch.countDown();
                continue;
            }
            apiService.getVariantForProductById(productId, variantId)
                    .enqueue(new retrofit2.Callback<com.phoneapp.phonepulse.models.Variant>() {
                        @Override
                        public void onResponse(retrofit2.Call<com.phoneapp.phonepulse.models.Variant> call, retrofit2.Response<com.phoneapp.phonepulse.models.Variant> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                com.phoneapp.phonepulse.models.Variant current = response.body();
                                int newQty = Math.max(0, current.getQuantity() - qty);
                                com.phoneapp.phonepulse.models.Variant payload = new com.phoneapp.phonepulse.models.Variant();
                                payload.setQuantity(newQty);
                                apiService.updateVariantForProductById(productId, variantId, payload)
                                        .enqueue(new retrofit2.Callback<ApiResponse<com.phoneapp.phonepulse.models.Variant>>() {
                                            @Override
                                            public void onResponse(retrofit2.Call<ApiResponse<com.phoneapp.phonepulse.models.Variant>> call2, retrofit2.Response<ApiResponse<com.phoneapp.phonepulse.models.Variant>> resp2) {
                                                if (resp2.isSuccessful() && resp2.body() != null && resp2.body().isSuccess()) {
                                                    android.util.Log.d(TAG, "Stock updated variant=" + variantId + " -> " + newQty);
                                                } else {
                                                    android.util.Log.w(TAG, "Stock update failed for variant=" + variantId + ", code=" + resp2.code());
                                                }
                                                stockLatch.countDown();
                                            }

                                            @Override
                                            public void onFailure(retrofit2.Call<ApiResponse<com.phoneapp.phonepulse.models.Variant>> call2, Throwable t) {
                                                android.util.Log.e(TAG, "Stock update API error: " + t.getMessage());
                                                stockLatch.countDown();
                                            }
                                        });
                            } else {
                                android.util.Log.w(TAG, "Fetch variant failed for " + variantId + ", code=" + response.code());
                                stockLatch.countDown();
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<com.phoneapp.phonepulse.models.Variant> call, Throwable t) {
                            android.util.Log.e(TAG, "Fetch variant API error: " + t.getMessage());
                            stockLatch.countDown();
                        }
                    });
        }

        new Thread(() -> {
            try {
                stockLatch.await();
                runOnUiThread(() -> clearCartForItems(items));
            } catch (InterruptedException e) {
                runOnUiThread(() -> clearCartForItems(items));
            }
        }).start();
    }

    private void clearCartForItems(java.util.List<OrderItem> items) {
        android.util.Log.d(TAG, "Start clearCart for items: " + (items != null ? items.size() : 0));
        if (items == null || items.isEmpty()) {
            navigateToHistory();
            return;
        }
        final java.util.concurrent.CountDownLatch cartLatch = new java.util.concurrent.CountDownLatch(items.size());
        for (OrderItem item : items) {
            String productId = item.getProductId();
            String variantId = item.getVariantId();
            if (productId == null || variantId == null || productId.trim().isEmpty() || variantId.trim().isEmpty()) {
                android.util.Log.w(TAG, "Skip removeFromCart: missing productId/variantId for " + item.getName());
                cartLatch.countDown();
                continue;
            }
            CartRequest.RemoveCartItem req = new CartRequest.RemoveCartItem(productId, variantId);
            apiService.removeFromCart(req).enqueue(new retrofit2.Callback<ApiResponse<Cart>>() {
                @Override
                public void onResponse(retrofit2.Call<ApiResponse<Cart>> call, retrofit2.Response<ApiResponse<Cart>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        android.util.Log.d(TAG, "Removed from cart p=" + productId + " v=" + variantId);
                    } else {
                        android.util.Log.w(TAG, "RemoveFromCart failed p=" + productId + " v=" + variantId + ", code=" + response.code());
                    }
                    cartLatch.countDown();
                }

                @Override
                public void onFailure(retrofit2.Call<ApiResponse<Cart>> call, Throwable t) {
                    android.util.Log.e(TAG, "RemoveFromCart API error: " + t.getMessage());
                    cartLatch.countDown();
                }
            });
        }

        new Thread(() -> {
            try {
                cartLatch.await();
            } catch (InterruptedException ignored) {}
            runOnUiThread(this::navigateToHistory);
        }).start();
    }

    private void navigateToHistory() {
        Intent intent = new Intent(MomoPaymentWebViewActivity.this, DashBoar_Activity.class);
        intent.putExtra("navigate_to_history", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

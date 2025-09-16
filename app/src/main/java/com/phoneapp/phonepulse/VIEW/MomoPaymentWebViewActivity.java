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
    private boolean daXuLyKetQua = false;
    private Handler pollHandler = new Handler();
    private int soLanKiemTra = 0;
    private static final int MAX_SO_LAN_KIEM_TRA = 30; // ~30s

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
            cauHinhWebView(url);
        }

        if (qrUrl != null && !qrUrl.isEmpty()) {
            qrImage.setVisibility(View.VISIBLE);
            Bitmap qrBitmap = taoQRCode(qrUrl);
            if (qrBitmap != null) {
                qrImage.setImageBitmap(qrBitmap);
            }
        } else {
            qrImage.setVisibility(View.GONE);
        }
    }

    // Tạo mã QR từ chuỗi
    private Bitmap taoQRCode(String text) {
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
    private void cauHinhWebView(String url) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                android.util.Log.d(TAG, "URL đang tải = " + url);
                if (daXuLyKetQua) {
                    android.util.Log.d(TAG, "Đã xử lý kết quả trước đó -> bỏ qua");
                    return true;
                }

                if (url.startsWith("momo_return://") || url.contains("payment-success") || url.contains("/success") || url.contains("/return")) {
                    Uri uri = Uri.parse(url);

                    String orderId = uri.getQueryParameter("orderId");
                    String resultCodeStr = uri.getQueryParameter("resultCode");
                    String message = uri.getQueryParameter("message");
                    String extraData = uri.getQueryParameter("extraData");
                    int resultCode = resultCodeStr != null ? Integer.parseInt(resultCodeStr) : -1;

                    daXuLyKetQua = true;
                    android.util.Log.d(TAG, "Phát hiện URL trả về MoMo. orderId=" + orderId + ", resultCode=" + resultCode);

                    if (orderId != null && resultCode != -1) {
                        apiService.handleMomoReturn(resultCode, orderId, message, extraData)
                                .enqueue(new Callback<ApiResponse<Order>>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                            Order order = response.body().getData();

                                            Toast.makeText(MomoPaymentWebViewActivity.this,
                                                    "Thanh toán thành công!", Toast.LENGTH_SHORT).show();

                                            if (order != null && order.getItems() != null && !order.getItems().isEmpty()) {
                                                xuLySauThanhToan(order.getItems());
                                            } else {
                                                navigateToHistory();
                                            }

                                        } else {
                                            Toast.makeText(MomoPaymentWebViewActivity.this,
                                                    "Thanh toán thành công nhưng tạo đơn thất bại!", Toast.LENGTH_LONG).show();
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                                        Toast.makeText(MomoPaymentWebViewActivity.this,
                                                "Lỗi kết nối server: " + t.getMessage(), Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                });
                    } else {
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
                if (daXuLyKetQua) {
                    return;
                }
                String title = view.getTitle() != null ? view.getTitle().toLowerCase() : "";
                if (title.contains("thành công") || title.contains("success")) {
                    daXuLyKetQua = true;
                    Toast.makeText(MomoPaymentWebViewActivity.this,
                            "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                    navigateToHistory();
                } else {
                    batDauKiemTraThanhCong();
                }
            }
        });

        webView.loadUrl(url);
    }

    private void batDauKiemTraThanhCong() {
        soLanKiemTra = 0;
        pollHandler.post(kiemTraThanhCongRunnable);
    }

    private final Runnable kiemTraThanhCongRunnable = new Runnable() {
        @Override
        public void run() {
            if (daXuLyKetQua) return;
            if (soLanKiemTra >= MAX_SO_LAN_KIEM_TRA) return;

            soLanKiemTra++;
            if (webView == null) return;

            webView.evaluateJavascript("(function(){try{return document.body && document.body.innerText || ''; }catch(e){return ''; }})()", value -> {
                String text = value != null ? value.replace("\\n", " ").replace("\"", "").toLowerCase() : "";
                if (!daXuLyKetQua && text.contains("thành công")) {
                    daXuLyKetQua = true;
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

    // ===== Xử lý sau khi thanh toán =====
    private void xuLySauThanhToan(java.util.List<OrderItem> items) {
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
                                                stockLatch.countDown();
                                            }

                                            @Override
                                            public void onFailure(retrofit2.Call<ApiResponse<com.phoneapp.phonepulse.models.Variant>> call2, Throwable t) {
                                                stockLatch.countDown();
                                            }
                                        });
                            } else {
                                stockLatch.countDown();
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<com.phoneapp.phonepulse.models.Variant> call, Throwable t) {
                            stockLatch.countDown();
                        }
                    });
        }

        new Thread(() -> {
            try {
                stockLatch.await();
                runOnUiThread(() -> xoaKhoiGioHang(items));
            } catch (InterruptedException e) {
                runOnUiThread(() -> xoaKhoiGioHang(items));
            }
        }).start();
    }

    private void xoaKhoiGioHang(java.util.List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            navigateToHistory();
            return;
        }
        final java.util.concurrent.CountDownLatch cartLatch = new java.util.concurrent.CountDownLatch(items.size());
        for (OrderItem item : items) {
            String productId = item.getProductId();
            String variantId = item.getVariantId();
            if (productId == null || variantId == null || productId.trim().isEmpty() || variantId.trim().isEmpty()) {
                cartLatch.countDown();
                continue;
            }
            CartRequest.RemoveCartItem req = new CartRequest.RemoveCartItem(productId, variantId);
            apiService.removeFromCart(req).enqueue(new retrofit2.Callback<ApiResponse<Cart>>() {
                @Override
                public void onResponse(retrofit2.Call<ApiResponse<Cart>> call, retrofit2.Response<ApiResponse<Cart>> response) {
                    cartLatch.countDown();
                }

                @Override
                public void onFailure(retrofit2.Call<ApiResponse<Cart>> call, Throwable t) {
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

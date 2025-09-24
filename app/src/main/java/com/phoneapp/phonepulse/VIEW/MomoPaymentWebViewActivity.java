package com.phoneapp.phonepulse.VIEW;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.models.Cart;
import com.phoneapp.phonepulse.models.Variant;
import com.phoneapp.phonepulse.request.CartRequest;
import com.phoneapp.phonepulse.request.OrderItem;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MomoPaymentWebViewActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_QR = "extra_qr";

    private WebView webView;
    private ImageView qrImage;
    private ProgressBar progressBar;
    private Bitmap qrBitmap;
    private boolean isProcessing = false;

    private static final String TAG = "MomoPaymentWebView";

    // TODO: truyền các list & apiService từ trước khi gọi activity
    private ArrayList<OrderItem> orderItemList;  // danh sách sp đã đặt
    private ArrayList<Variant> variantsInCart;   // biến thể trong giỏ
    private ApiService apiService;               // service retrofit

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_momo_payment_web_view);

        webView = findViewById(R.id.webView);
        qrImage = findViewById(R.id.qrImage);
        progressBar = findViewById(R.id.progressBar);

        String url = getIntent().getStringExtra(EXTRA_URL);
        String qrUrl = getIntent().getStringExtra(EXTRA_QR);

        if (qrUrl != null && !qrUrl.isEmpty()) {
            qrImage.setVisibility(View.VISIBLE);
            qrBitmap = generateQRCode(qrUrl);
            if (qrBitmap != null) qrImage.setImageBitmap(qrBitmap);
        } else {
            qrImage.setVisibility(View.GONE);
        }

        if (url != null) {
            configureWebView(url);
        }
    }

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

    private void configureWebView(String url) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (isProcessing) return true;

                if (url.startsWith("momo_return://") || url.contains("payment-success")
                        || url.contains("/success") || url.contains("/return")) {
                    handlePaymentSuccess();
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (isProcessing) return;

                view.evaluateJavascript("(function(){try{return document.body.innerText;}catch(e){return '';}})();", value -> {
                    if (value != null) {
                        String text = value.replace("\\n", " ").replace("\"", "").toLowerCase();
                        if (text.contains("thành công") || text.contains("success")) {
                            handlePaymentSuccess();
                        }
                    }
                });
            }
        });

        webView.loadUrl(url);
    }

    private void handlePaymentSuccess() {
        if (isProcessing) return;
        isProcessing = true;

        Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();

        // ✅ Sau khi thanh toán: cập nhật tồn kho → clear giỏ → điều hướng
        updateVariantStockOnServer(orderItemList);
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, DashBoar_Activity.class);
        intent.putExtra("openFragment", "TatCaDonHang");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.clearHistory();
            webView.clearCache(true);
            CookieManager.getInstance().removeAllCookies(null);
            webView.destroy();
        }

        finish();
    }

    // ================== HÀM CẬP NHẬT TỒN KHO ==================
    public void updateVariantStockOnServer(ArrayList<OrderItem> orderedItems) {
        if (orderedItems == null || orderedItems.isEmpty()) {
            Log.w(TAG, "Không có OrderedItems để cập nhật tồn kho. Chuyển hướng.");
            navigateToDashboard();
            return;
        }

        final CountDownLatch stockUpdateLatch = new CountDownLatch(orderedItems.size());
        Log.d(TAG, "Bắt đầu cập nhật tồn kho cho " + orderedItems.size() + " sản phẩm.");

        for (OrderItem item : orderedItems) {
            final OrderItem finalOrderItem = item;
            String variantId = finalOrderItem.getVariantId();
            String productId = finalOrderItem.getProductId();

            if (variantId == null || variantId.trim().isEmpty() || productId == null || productId.trim().isEmpty()) {
                Log.w(TAG, "❌ Bỏ qua cập nhật tồn kho: Thiếu variantId hoặc productId cho sản phẩm: " + finalOrderItem.getName());
                stockUpdateLatch.countDown();
                continue;
            }

            Variant foundVariant = findVariantById(variantId, variantsInCart);
            if (foundVariant != null) {
                int currentQuantity = foundVariant.getQuantity();
                int quantityOrdered = finalOrderItem.getQuantity();
                int newQuantity = Math.max(currentQuantity - quantityOrdered, 0);

                Log.d(TAG, String.format(Locale.getDefault(),
                        "📦 Cập nhật tồn kho Variant ID: %s | Cũ: %d | Đặt: %d | Mới: %d",
                        variantId, currentQuantity, quantityOrdered, newQuantity));

                Variant updatedVariant = new Variant();
                updatedVariant.setId(variantId);
                updatedVariant.setQuantity(newQuantity);

                apiService.updateVariantForProductById(productId, variantId, updatedVariant)
                        .enqueue(new Callback<ApiResponse<Variant>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<Variant>> call, Response<ApiResponse<Variant>> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                    Log.i(TAG, "✅ Tồn kho đã cập nhật: " + variantId);
                                }
                                stockUpdateLatch.countDown();
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<Variant>> call, Throwable t) {
                                Log.e(TAG, "🌐 Lỗi cập nhật tồn kho: " + t.getMessage());
                                stockUpdateLatch.countDown();
                            }
                        });
            } else {
                Log.e(TAG, "❌ Không tìm thấy variant ID: " + variantId);
                stockUpdateLatch.countDown();
            }
        }

        new Thread(() -> {
            try {
                stockUpdateLatch.await();
                runOnUiThread(this::clearCartOnServer);
            } catch (InterruptedException e) {
                Log.e(TAG, "⚠️ Lỗi khi chờ cập nhật tồn kho.", e);
                runOnUiThread(this::navigateToDashboard);
            }
        }).start();
    }

    // ================== HÀM CLEAR GIỎ ==================
    public void clearCartOnServer() {
        if (orderItemList == null || orderItemList.isEmpty()) {
            Log.d(TAG, "Giỏ hàng rỗng. Chuyển hướng.");
            navigateToDashboard();
            return;
        }

        final CountDownLatch cartRemovalLatch = new CountDownLatch(orderItemList.size());
        Log.d(TAG, "Bắt đầu xóa " + orderItemList.size() + " sản phẩm khỏi giỏ.");

        for (OrderItem item : orderItemList) {
            String productId = item.getProductId();
            String variantId = item.getVariantId();

            if (productId == null || productId.trim().isEmpty() ||
                    variantId == null || variantId.trim().isEmpty()) {
                cartRemovalLatch.countDown();
                continue;
            }

            CartRequest.RemoveCartItem request = new CartRequest.RemoveCartItem(productId, variantId);

            apiService.removeFromCart(request).enqueue(new Callback<ApiResponse<Cart>>() {
                @Override
                public void onResponse(Call<ApiResponse<Cart>> call, Response<ApiResponse<Cart>> response) {
                    cartRemovalLatch.countDown();
                }

                @Override
                public void onFailure(Call<ApiResponse<Cart>> call, Throwable t) {
                    cartRemovalLatch.countDown();
                }
            });
        }

        new Thread(() -> {
            try {
                cartRemovalLatch.await();
                runOnUiThread(this::navigateToDashboard);
            } catch (InterruptedException e) {
                runOnUiThread(this::navigateToDashboard);
            }
        }).start();
    }

    private Variant findVariantById(String variantId, ArrayList<Variant> variants) {
        if (variants == null) return null;
        for (Variant v : variants) {
            if (variantId.equals(v.getId())) {
                return v;
            }
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (qrBitmap != null && !qrBitmap.isRecycled()) {
            qrBitmap.recycle();
            qrBitmap = null;
        }
    }
}

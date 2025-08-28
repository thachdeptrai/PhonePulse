package com.phoneapp.phonepulse.VIEW;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
import com.phoneapp.phonepulse.utils.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MomoPaymentWebViewActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_QR = "extra_qr";
    private ApiService apiService;
    private WebView webView;
    private ImageView qrImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_momo_payment_web_view);

        webView = findViewById(R.id.webView);
        qrImage = findViewById(R.id.qrImage);

        String url = getIntent().getStringExtra(EXTRA_URL);
        String qrUrl = getIntent().getStringExtra(EXTRA_QR);

        if (url != null) {
            setupWebView(url);
        }
        apiService = RetrofitClient.getApiService(Constants.getToken(this));
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
                if (url.startsWith("momo_return://") || url.contains("payment-success")) {
                    Uri uri = Uri.parse(url);

                    String orderId = uri.getQueryParameter("orderId");
                    String resultCodeStr = uri.getQueryParameter("resultCode");
                    String message = uri.getQueryParameter("message");
                    String extraData = uri.getQueryParameter("extraData");
                    int resultCode = resultCodeStr != null ? Integer.parseInt(resultCodeStr) : -1;
                    // ✅ POST về backend để tạo đơn hàng thực sự
                    apiService.handleMomoReturn(resultCode, orderId, message, extraData)
                            .enqueue(new Callback<ApiResponse<Order>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                        // Lấy dữ liệu order từ ApiResponse
                                        Order order = response.body().getData();

                                        Intent intent = new Intent(MomoPaymentWebViewActivity.this, Oder_Activity.class);
                                        intent.putExtra("orderData", order);
                                        startActivity(intent);
                                        finish();
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
                    return true;
                }
                return false;
            }
        });

        webView.loadUrl(url);
    }
}

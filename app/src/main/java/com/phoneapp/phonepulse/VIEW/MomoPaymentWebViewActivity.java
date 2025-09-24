package com.phoneapp.phonepulse.VIEW;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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

public class MomoPaymentWebViewActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_QR = "extra_qr";

    private WebView webView;
    private ImageView qrImage;
    private ProgressBar progressBar;
    private Bitmap qrBitmap;
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_momo_payment_web_view);

        webView = findViewById(R.id.webView);
        qrImage = findViewById(R.id.qrImage);
        progressBar = findViewById(R.id.progressBar);

        String url = getIntent().getStringExtra(EXTRA_URL);
        String qrUrl = getIntent().getStringExtra(EXTRA_QR);

        // Hiển thị QR nếu có
        if (qrUrl != null && !qrUrl.isEmpty()) {
            qrImage.setVisibility(View.VISIBLE);
            qrBitmap = generateQRCode(qrUrl);
            if (qrBitmap != null) qrImage.setImageBitmap(qrBitmap);
        } else {
            qrImage.setVisibility(View.GONE);
        }

        // Cấu hình WebView
        if (url != null) {
            configureWebView(url);
        }
    }

    // Tạo QR Code
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

    // Cấu hình WebView
    private void configureWebView(String url) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (isProcessing) return true;

                // Phát hiện redirect thanh toán thành công
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

                // Kiểm tra nội dung trang
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
        navigateToDashboard();
    }

    private void navigateToDashboard() {
        // Điều hướng về DashBoar + mở fragment TatCaDonHang
        Intent intent = new Intent(this, DashBoar_Activity.class);
        intent.putExtra("openFragment", "TatCaDonHang");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        // Clear WebView & cookies
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.clearHistory();
            webView.clearCache(true);
            CookieManager.getInstance().removeAllCookies(null);
            webView.destroy();
        }

        finish();
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

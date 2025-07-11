package com.phoneapp.phonepulse.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog; // Import AlertDialog
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.request.CartItem; // Lớp CartItem từ phản hồi API
import com.phoneapp.phonepulse.models.Product;   // Lớp Product model
import com.phoneapp.phonepulse.models.ProductImage; // Lớp ProductImage model

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private List<CartItem> cartItems;
    private final CartItemActionCallback callback; // Callback để giao tiếp với ViewModel/Activity
    private final NumberFormat numberFormat;

    // --- Interface để giao tiếp ngược lại với ViewModel/Activity ---
    public interface CartItemActionCallback {
        /**
         * Được gọi khi số lượng sản phẩm trong giỏ hàng thay đổi.
         * @param item Sản phẩm giỏ hàng bị thay đổi.
         * @param newQuantity Số lượng mới của sản phẩm.
         */
        void onQuantityChanged(CartItem item, int newQuantity);

        /**
         * Được gọi khi người dùng muốn xóa một sản phẩm khỏi giỏ hàng.
         * @param item Sản phẩm giỏ hàng cần xóa.
         */
        void onRemoveItem(CartItem item);

        /**
         * Được gọi để thông báo cần tính toán lại tổng tiền của giỏ hàng.
         */
        void onCartTotalChanged();
    }

    public CartAdapter(Context context, List<CartItem> cartItems, CartItemActionCallback callback) {
        this.context = context;
        this.cartItems = cartItems;
        this.callback = callback;
        // Định dạng tiền tệ theo Locale Việt Nam (vd: 1.000.000₫)
        this.numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Ánh xạ layout item_cart.xml cho mỗi mục trong RecyclerView
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        // Gán dữ liệu cho từng mục dựa trên vị trí
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        // Trả về tổng số lượng sản phẩm trong giỏ hàng
        return cartItems.size();
    }

    /**
     * Cập nhật danh sách sản phẩm trong giỏ hàng và làm mới RecyclerView.
     * Phương thức này thường được gọi từ ViewModel khi dữ liệu giỏ hàng thay đổi.
     * @param newItems Danh sách CartItem mới.
     */
    public void setCartItems(List<CartItem> newItems) {
        this.cartItems = newItems;
        notifyDataSetChanged(); // Thông báo cho RecyclerView làm mới toàn bộ danh sách
        callback.onCartTotalChanged(); // Thông báo cho Activity/ViewModel tính toán lại tổng tiền
    }

    // --- ViewHolder cho mỗi mục trong giỏ hàng ---
    class CartViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivProductImage, btnDecrease, btnIncrease;
        private final TextView tvProductName, tvProductPrice, tvQuantity;


        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các View từ layout item_cart.xml
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            // Ánh xạ nút xóa nếu có
            // ivDelete = itemView.findViewById(R.id.iv_delete);
        }

        /**
         * Gán dữ liệu của một CartItem vào các View trong ViewHolder.
         * @param item CartItem chứa thông tin sản phẩm và số lượng.
         */
        public void bind(CartItem item) {
            Product product = item.getProduct();
            if (product != null) {
                tvProductName.setText(product.getName());

                // Lấy giá của từng đơn vị sản phẩm (từ variant nếu có, nếu không thì từ product)
                double singleItemPrice = (item.getVariant() != null) ? item.getVariant().getPrice() : product.getPrice();
                // Hiển thị TỔNG giá của sản phẩm này (đơn giá * số lượng hiện tại)
                tvProductPrice.setText("₫" + numberFormat.format(singleItemPrice * item.getQuantity()));

                // Lấy URL hình ảnh từ ProductImage của Product
                String imageUrl = "";
                ProductImage productImage = product.getProductImage();
                if (productImage != null && productImage.getImageUrl() != null) {
                    imageUrl = productImage.getImageUrl();
                }

                // Tải hình ảnh bằng Glide
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_product) // Ảnh placeholder khi đang tải
                        .error(R.drawable.placeholder_product)       // Ảnh hiển thị nếu lỗi tải
                        .into(ivProductImage);
            }

            // Hiển thị số lượng hiện tại của sản phẩm
            tvQuantity.setText(String.valueOf(item.getQuantity()));

            // --- Xử lý sự kiện khi nhấn nút GIẢM số lượng ---
            btnDecrease.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    // Nếu số lượng > 1, giảm số lượng và thông báo cho ViewModel
                    callback.onQuantityChanged(item, item.getQuantity() - 1);
                } else {
                    // Nếu số lượng đã là 1 và người dùng nhấn giảm, hiển thị hộp thoại xác nhận xóa
                    showRemoveConfirmationDialog(item);
                }
            });

            // --- Xử lý sự kiện khi nhấn nút TĂNG số lượng ---
            btnIncrease.setOnClickListener(v -> {
                // Có thể thêm kiểm tra số lượng tối đa có sẵn của sản phẩm ở đây nếu cần
                callback.onQuantityChanged(item, item.getQuantity() + 1);
            });

            // --- Xử lý sự kiện khi nhấn nút XÓA (nếu có riêng cho từng item) ---
            // if (ivDelete != null) {
            //     ivDelete.setOnClickListener(v -> showRemoveConfirmationDialog(item));
            // }
        }

        /**
         * Hiển thị hộp thoại xác nhận trước khi xóa một sản phẩm khỏi giỏ hàng.
         * @param item CartItem cần xác nhận xóa.
         */
        private void showRemoveConfirmationDialog(final CartItem item) {
            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận xóa sản phẩm")
                    .setMessage("Bạn có muốn xóa sản phẩm \"" + item.getProduct().getName() + "\" khỏi giỏ hàng không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        // Nếu người dùng chọn "Xóa", thông báo cho ViewModel để thực hiện xóa
                        callback.onRemoveItem(item);
                        Toast.makeText(context, "Đang xóa sản phẩm...", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> {
                        // Nếu người dùng chọn "Hủy", đóng hộp thoại
                        dialog.dismiss();
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert) // Biểu tượng cảnh báo tiêu chuẩn
                    .show();
        }
    }
}
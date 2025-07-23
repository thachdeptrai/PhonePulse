package com.phoneapp.phonepulse.request; // Đặt vào package 'utils' hoặc package phù hợp

import com.phoneapp.phonepulse.models.Product;
import com.phoneapp.phonepulse.models.Variant;
import com.phoneapp.phonepulse.models.ProductImage; // Đã bỏ qua import Color, Size vì chúng được truy cập qua Variant
// Đảm bảo import đúng lớp ProductGirdItem đã được cập nhật
import com.phoneapp.phonepulse.request.ProductGirdItem;

import java.util.ArrayList;
import java.util.List;

public class DataConverter {

    /**
     * Chuyển đổi danh sách Product (được populate từ API) thành danh sách ProductGirdItem.
     * Mỗi biến thể của sản phẩm sẽ trở thành một ProductGirdItem riêng biệt.
     *
     * @param products Danh sách các đối tượng Product nhận được từ API backend.
     * @return Danh sách các đối tượng ProductGirdItem đã được làm phẳng.
     */
    public static List<ProductGirdItem> convertProductsToGridItems(List<Product> products) {
        List<ProductGirdItem> gridItems = new ArrayList<>();

        // Kiểm tra nếu danh sách sản phẩm rỗng hoặc null
        if (products == null || products.isEmpty()) {
            return gridItems;
        }

        for (Product product : products) {
            // 1. Lấy URL ảnh chính của sản phẩm
            // Sử dụng phương thức an toàn getImageUrlSafe() từ lớp Product đã cập nhật
            String imageUrl = product.getImageUrlSafe();

            // 2. Lặp qua từng biến thể (variant) của sản phẩm
            List<Variant> variants = product.getVariants();
            if (variants != null && !variants.isEmpty()) {
                for (Variant variant : variants) {
                    // 3. Lấy tên màu sắc an toàn
                    String colorName = null;
                    if (variant.getColor() != null) { // Đảm bảo đối tượng Color không null
                        colorName = variant.getColor().getColorName();
                    }

                    // 4. Lấy tên kích thước/dung lượng an toàn
                    String sizeName = null;
                    if (variant.getSize() != null) { // Đảm bảo đối tượng Size không null
                        sizeName = variant.getSize().getSizeName();
                    }

                    // 5. Lấy giá và tính toán các giá trị liên quan đến giá/giảm giá
                    double discountedPrice = variant.getPrice();
                    // Lấy phần trăm giảm giá từ đối tượng Product (giả định đây là phần trăm giảm giá chung cho sản phẩm)
                    int discountPercent = product.getDiscount();

                    double originalPrice = discountedPrice;
                    // Tính giá gốc nếu có giảm giá
                    if (discountPercent > 0 && discountPercent <= 100) {
                        originalPrice = discountedPrice / (1 - (double) discountPercent / 100);
                    }

                    // 6. Số lượng đã bán (placeholder)
                    // **QUAN TRỌNG:** sold_count không có trong schema Mongoose bạn cung cấp.
                    // Để có dữ liệu thực, bạn cần thêm trường này vào model Product hoặc Variant ở backend.
                    // Hiện tại, tôi sẽ dùng giá trị ngẫu nhiên để minh họa.
                    int soldCount = (int) (Math.random() * 500) + 1; // Giá trị ngẫu nhiên từ 1 đến 500

                    // 7. Tạo đối tượng ProductGirdItem với constructor đã cập nhật
                    ProductGirdItem item = new ProductGirdItem(
                            product.getId(),         // _id của Product
                            variant.getId(),         // _id của Variant
                            product.getName(),       // product_name của Product
                            imageUrl,                // image_url của ProductImage
                            discountedPrice,         // price (giá đã giảm)
                            originalPrice,           // original_price (giá gốc đã tính)
                            discountPercent,         // discount_percent (phần trăm giảm giá)
                            soldCount,               // sold_count (số lượng đã bán)
                            sizeName,                // size_name của Size
                            colorName                // color_name của Color
                    );
                    gridItems.add(item);
                }
            }
            // Nếu sản phẩm không có biến thể nào, nó sẽ không được thêm vào gridItems.
            // Nếu bạn muốn sản phẩm vẫn xuất hiện dù không có biến thể (chỉ với thông tin cơ bản),
            // bạn sẽ cần thêm logic xử lý riêng ở đây.
        }
        return gridItems;
    }
}
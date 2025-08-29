package com.phoneapp.phonepulse.request;

import com.phoneapp.phonepulse.models.Product;
import com.phoneapp.phonepulse.models.Variant;
import com.phoneapp.phonepulse.models.ProductImage;
import com.phoneapp.phonepulse.request.ProductGirdItem;

import java.util.ArrayList;
import java.util.List;

public class DataConverter {

    private static List<ProductImage> images;

    public static List<ProductGirdItem> convertProductsToGridItems(List<Product> products) {
        List<ProductGirdItem> gridItems = new ArrayList<>();

        if (products == null || products.isEmpty()) {
            return gridItems;
        }

        for (Product product : products) {
            String imageUrl = product.getImageUrlSafe();

            // Lấy category_id từ object Category
            String categoryId = null;
            if (product.getCategory() != null) {
                categoryId = product.getCategory().getId();
            }

            // ✅ chỉ lấy 1 variant đại diện
            Variant variant = product.getVariant();
            if (variant != null) {
                String colorName = (variant.getColor() != null) ? variant.getColor().getColorName() : null;
                String sizeName = (variant.getSize() != null) ? variant.getSize().getSizeName() : null;

                double discountedPrice = variant.getPrice();
                int discountPercent = product.getDiscount();
                double originalPrice = discountedPrice;
                if (discountPercent > 0 && discountPercent <= 100) {
                    originalPrice = discountedPrice / (1 - (double) discountPercent / 100);
                }

                int soldCount = (int) (Math.random() * 500) + 1;

                ProductGirdItem item = new ProductGirdItem(
                        product.getId(),
                        variant.getId(),
                        product.getName(),
                        imageUrl,
                        discountedPrice,
                        originalPrice,
                        discountPercent,
                        soldCount,
                        sizeName,
                        colorName,
                        images
                );

                item.setCategory_id(categoryId);

                gridItems.add(item);
            }
        }
        return gridItems;
    }


}

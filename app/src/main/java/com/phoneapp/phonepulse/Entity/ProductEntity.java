package com.phoneapp.phonepulse.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class ProductEntity {
    @PrimaryKey
    public int id;

    public String name;
    public int brand_id;
    public int price;
    public int discount;
    public String image_url;
    public String specs_json;
    public int stock;
    public String created_at;
}

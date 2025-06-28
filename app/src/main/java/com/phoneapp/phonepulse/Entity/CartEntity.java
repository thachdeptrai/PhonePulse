package com.phoneapp.phonepulse.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cart")
public class CartEntity {
    @PrimaryKey
    public int id;

    public int user_id;
    public int product_id;
    public int quantity;
}


package com.phoneapp.phonepulse;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.phoneapp.phonepulse.DAO.CartDao;
import com.phoneapp.phonepulse.DAO.ProductDao;
import com.phoneapp.phonepulse.Entity.CartEntity;
import com.phoneapp.phonepulse.Entity.ProductEntity;

@Database(
        entities = {
                ProductEntity.class,
                CartEntity.class
        },
        version = 1
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ProductDao productDao();
    public abstract CartDao cartDao();
}

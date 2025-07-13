//package com.phoneapp.phonepulse.DAO;
//
//import androidx.room.Dao;
//import androidx.room.Insert;
//import androidx.room.OnConflictStrategy;
//import androidx.room.Query;
//
//import java.util.List;
//
//@Dao
//public interface CartDao {
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    void insertAll(List<CartEntity> cartList);
//
//    @Query("SELECT * FROM cart WHERE user_id = :userId")
//    List<CartEntity> getCartByUser(int userId);
//
//    @Query("DELETE FROM cart")
//    void clearCart();
//}

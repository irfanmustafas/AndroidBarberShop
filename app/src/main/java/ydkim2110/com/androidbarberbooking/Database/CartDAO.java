package ydkim2110.com.androidbarberbooking.Database;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface CartDAO {

    @Query("SELECT SUM(productPrice*productQuantity) FROM Cart where userPhone=:userPhone")
    long sumPrice(String userPhone);

    @Query("SELECT * FROM Cart WHERE userPhone=:userPhone")
    List<CartItem> getAllItemFromCart(String userPhone);

    @Query("SELECT COUNT(*) FROM Cart WHERE userPhone=:userPhone")
    int countItemInCart(String userPhone);

    @Query("SELECT * FROM Cart WHERE productId=:productId AND userPhone=:userPhone")
    CartItem getProductInCart(String productId, String userPhone);

    @Insert(onConflict = OnConflictStrategy.FAIL)
    void insert(CartItem... carts);

    @Update(onConflict = OnConflictStrategy.FAIL)
    void update(CartItem cart);

    @Delete
    void delete(CartItem cartItem);

    @Query("DELETE FROM Cart WHERE userPhone=:userPhone")
    void clearCart(String userPhone);

}

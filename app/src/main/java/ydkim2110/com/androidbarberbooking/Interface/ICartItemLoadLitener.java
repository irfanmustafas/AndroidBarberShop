package ydkim2110.com.androidbarberbooking.Interface;

import java.util.List;

import ydkim2110.com.androidbarberbooking.Database.CartItem;

public interface ICartItemLoadLitener {
    void onGetAllItemFromCartSuccess(List<CartItem> cartItemList);
}

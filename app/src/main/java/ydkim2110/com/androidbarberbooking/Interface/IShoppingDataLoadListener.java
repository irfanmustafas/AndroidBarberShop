package ydkim2110.com.androidbarberbooking.Interface;

import java.util.List;

import ydkim2110.com.androidbarberbooking.Model.ShoppingItem;

public interface IShoppingDataLoadListener {
    void onShoppingDataLoadSuccess(List<ShoppingItem> shoppingItemList);
    void onShoppingDataLoadFailed(String message);
}

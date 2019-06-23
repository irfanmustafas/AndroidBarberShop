package ydkim2110.com.androidbarberbooking.Database;

import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import ydkim2110.com.androidbarberbooking.Common.Common;
import ydkim2110.com.androidbarberbooking.Interface.ICartItemLoadLitener;
import ydkim2110.com.androidbarberbooking.Interface.ICountItemInCartListener;
import ydkim2110.com.androidbarberbooking.Interface.ISumCartListener;

public class DatabaseUtils {

    // Because all room handle need work on other thread
    public static void sumCart(CartDatabase db, ISumCartListener iSumCartListener) {
        SumCartAsync task = new SumCartAsync(db, iSumCartListener);
        task.execute();
    }

    public static void getAllCart(CartDatabase db, ICartItemLoadLitener cartItemLoadListener) {
        GetAllCartAsync task = new GetAllCartAsync(db, cartItemLoadListener);
        task.execute();
    }

    public static void updateCart(CartDatabase db, CartItem cartItem) {
        UpdateCartAsync task = new UpdateCartAsync(db);
        task.execute(cartItem);
    }

    public static void insertCart(CartDatabase db, CartItem... cartItems) {
        InsertToCartAsync task = new InsertToCartAsync(db);
        task.execute(cartItems);
    }

    public static void countItemInCart(CartDatabase db, ICountItemInCartListener iCountItemInCartListener) {
        CountItemInCartAsync task = new CountItemInCartAsync(db, iCountItemInCartListener);
        task.execute();
    }

    /*
    ============================================================================
    ASYNC TASK DEFINE
    ============================================================================
     */

    private static class SumCartAsync extends AsyncTask<Void, Void, Long> {

        private final CartDatabase db;
        private final ISumCartListener mListener;

        public SumCartAsync(CartDatabase db, ISumCartListener listener) {
            this.db = db;
            mListener = listener;
        }

        @Override
        protected Long doInBackground(Void... voids) {
            return db.cartDao().sumPrice(Common.currentUser.getPhoneNumber());
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            mListener.onSumCartSuccess(aLong);
        }
    }

    private static class UpdateCartAsync extends AsyncTask<CartItem, Void, Void> {

        private final CartDatabase db;

        public UpdateCartAsync(CartDatabase db) {
            this.db = db;
        }

        @Override
        protected Void doInBackground(CartItem... cartItems) {
            db.cartDao().update(cartItems[0]);
            return null;
        }
    }

    private static class GetAllCartAsync extends AsyncTask<String, Void, List<CartItem>> {

        CartDatabase db;
        ICartItemLoadLitener mLitener;

        public GetAllCartAsync(CartDatabase cartDatabase, ICartItemLoadLitener iCartItemLoadLitener) {
            db = cartDatabase;
            mLitener = iCartItemLoadLitener;
        }

        @Override
        protected List<CartItem> doInBackground(String... strings) {
            return db.cartDao().getAllItemFromCart(Common.currentUser.getPhoneNumber());
        }

        @Override
        protected void onPostExecute(List<CartItem> cartItemList) {
            super.onPostExecute(cartItemList);
            mLitener.onGetAllItemFromCartSuccess(cartItemList);
        }
    }

    private static class InsertToCartAsync extends AsyncTask<CartItem, Void, Void> {

        CartDatabase db;

        public InsertToCartAsync(CartDatabase cartDatabase) {
            db = cartDatabase;
        }

        @Override
        protected Void doInBackground(CartItem... cartItems) {
            insertToCart(db, cartItems[0]);
            return null;
        }

        private void insertToCart(CartDatabase db, CartItem cartItem) {
            // If item already available in cart, just increase quantity
            try {
                db.cartDao().insert(cartItem);
            } catch (SQLiteConstraintException exception) {
                CartItem updateCartItem = db.cartDao().getProductInCart(cartItem.getProductId(),
                        Common.currentUser.getPhoneNumber());
                updateCartItem.setProductQuantity(updateCartItem.getProductQuantity() + 1);
                db.cartDao().update(updateCartItem);
            }
        }
    }

    private static class CountItemInCartAsync extends AsyncTask<Void, Void, Integer> {

        CartDatabase db;
        ICountItemInCartListener listener;

        public CountItemInCartAsync(CartDatabase cartDatabase, ICountItemInCartListener iCountItemInCartListener) {
            db = cartDatabase;
            listener = iCountItemInCartListener;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return Integer.parseInt(String.valueOf(countItemInCartRun(db)));
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            listener.onCartItemCountSuccess(integer.intValue());
        }

        private int countItemInCartRun(CartDatabase db) {
            return db.cartDao().countItemInCart(Common.currentUser.getPhoneNumber());
        }
    }


}
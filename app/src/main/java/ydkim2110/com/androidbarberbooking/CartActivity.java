package ydkim2110.com.androidbarberbooking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import ydkim2110.com.androidbarberbooking.Adapter.MyCartAdapter;
import ydkim2110.com.androidbarberbooking.Database.CartDatabase;
import ydkim2110.com.androidbarberbooking.Database.CartItem;
import ydkim2110.com.androidbarberbooking.Database.DatabaseUtils;
import ydkim2110.com.androidbarberbooking.Interface.ICartItemLoadLitener;
import ydkim2110.com.androidbarberbooking.Interface.ICartItemUpdateListener;
import ydkim2110.com.androidbarberbooking.Interface.ISumCartListener;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class CartActivity extends AppCompatActivity implements ICartItemLoadLitener, ICartItemUpdateListener, ISumCartListener {

    private static final String TAG = "CartActivity";

    @BindView(R.id.recycler_cart)
    RecyclerView recycler_cart;
    @BindView(R.id.txt_total_price)
    TextView txt_total_price;
    @BindView(R.id.btn_submit_cart)
    Button btn_submit_cart;

    private CartDatabase mCartDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        Log.d(TAG, "onCreate: started!!");

        ButterKnife.bind(CartActivity.this);

        mCartDatabase = CartDatabase.getInstance(this);

        DatabaseUtils.getAllCart(mCartDatabase, this);

        // View
        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recycler_cart.setLayoutManager(linearLayoutManager);
        recycler_cart.addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));
    }

    @Override
    public void onGetAllItemFromCartSuccess(List<CartItem> cartItemList) {
        Log.d(TAG, "onGetAllItemFromCartSuccess: called!!");
        // Here, after we get all cart item from DB
        // We will display by Recycler View
        MyCartAdapter adapter = new MyCartAdapter(this, cartItemList, this);
        recycler_cart.setAdapter(adapter);
    }

    @Override
    public void onCartItemUpdateSuccess() {
        Log.d(TAG, "onCartItemUpdateSuccess: called!!");
        DatabaseUtils.sumCart(mCartDatabase, this);
    }

    @Override
    public void onSumCartSuccess(long value) {
        Log.d(TAG, "onSumCartSuccess: called!!");
        txt_total_price.setText(new StringBuilder("$").append(value));
    }
}

package ydkim2110.com.androidbarberbooking.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ydkim2110.com.androidbarberbooking.Common.Common;
import ydkim2110.com.androidbarberbooking.Database.CartDatabase;
import ydkim2110.com.androidbarberbooking.Database.CartItem;
import ydkim2110.com.androidbarberbooking.Database.DatabaseUtils;
import ydkim2110.com.androidbarberbooking.Interface.IRecyclerItemSelectedListener;
import ydkim2110.com.androidbarberbooking.Model.ShoppingItem;
import ydkim2110.com.androidbarberbooking.R;

public class MyShoppingItemAdapter extends RecyclerView.Adapter<MyShoppingItemAdapter.MyViewHolder> {

    Context mContext;
    List<ShoppingItem> mShoppingItemList;

    CartDatabase mCartDatabase;

    public MyShoppingItemAdapter(Context context, List<ShoppingItem> shoppingItemList) {
        mContext = context;
        mShoppingItemList = shoppingItemList;
        mCartDatabase = CartDatabase.getInstance(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_shopping_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get().load(mShoppingItemList.get(position).getImage()).into(holder.img_shopping_item);

        holder.txt_shopping_item_name.setText(
                Common.formatShoppingItemName(mShoppingItemList.get(position).getName()));
        holder.txt_shopping_item_price.setText(
                new StringBuilder("$").append(mShoppingItemList.get(position).getPrice().toString()));

        // Add to cart
        holder.setIRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelected(View view, int position) {
                // Create cart Item
                CartItem cartItem = new CartItem();
                cartItem.setProductId(mShoppingItemList.get(position).getId());
                cartItem.setProductName(mShoppingItemList.get(position).getName());
                cartItem.setProductImage(mShoppingItemList.get(position).getImage());
                cartItem.setProductQuantity(1);
                cartItem.setProductPrice(mShoppingItemList.get(position).getPrice());
                cartItem.setUserPhone(Common.currentUser.getPhoneNumber());

                // Insert to DB
                DatabaseUtils.insertCart(mCartDatabase, cartItem);
                Toast.makeText(mContext, "Added to cart", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mShoppingItemList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txt_shopping_item_name, txt_shopping_item_price, txt_add_to_cart;
        ImageView img_shopping_item;

        IRecyclerItemSelectedListener mIRecyclerItemSelectedListener;

        public void setIRecyclerItemSelectedListener(IRecyclerItemSelectedListener IRecyclerItemSelectedListener) {
            mIRecyclerItemSelectedListener = IRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            img_shopping_item = itemView.findViewById(R.id.img_shopping_item);
            txt_shopping_item_name = itemView.findViewById(R.id.txt_name_shopping_item);
            txt_shopping_item_price = itemView.findViewById(R.id.txt_price_shopping_item);
            txt_add_to_cart = itemView.findViewById(R.id.txt_add_to_cart);

            txt_add_to_cart.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mIRecyclerItemSelectedListener.onItemSelected(v, getAdapterPosition());
        }
    }
}

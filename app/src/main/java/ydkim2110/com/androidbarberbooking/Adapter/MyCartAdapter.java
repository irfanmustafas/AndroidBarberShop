package ydkim2110.com.androidbarberbooking.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ydkim2110.com.androidbarberbooking.Database.CartDatabase;
import ydkim2110.com.androidbarberbooking.Database.CartItem;
import ydkim2110.com.androidbarberbooking.Database.DatabaseUtils;
import ydkim2110.com.androidbarberbooking.Interface.ICartItemUpdateListener;
import ydkim2110.com.androidbarberbooking.R;

public class MyCartAdapter extends RecyclerView.Adapter<MyCartAdapter.MyViewHolder> {

    Context mContext;
    List<CartItem> mCartItemList;
    CartDatabase mCartDatabase;
    ICartItemUpdateListener mICartItemUpdateListener;

    public MyCartAdapter(Context context, List<CartItem> cartItemList, ICartItemUpdateListener ICartItemUpdateListener) {
        mContext = context;
        mCartItemList = cartItemList;
        mICartItemUpdateListener = ICartItemUpdateListener;

        this.mCartDatabase = CartDatabase.getInstance(context);

        mICartItemUpdateListener.onCartItemUpdateSuccess();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_cart_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get()
                .load(mCartItemList.get(position).getProductImage())
                .into(holder.img_product);

        holder.txt_cart_name.setText(
                new StringBuilder(mCartItemList.get(position).getProductName())
        );
        holder.txt_cart_price.setText(
                new StringBuilder("$").append(mCartItemList.get(position).getProductPrice())
        );
        holder.txt_quantity.setText(
                new StringBuilder(String.valueOf(mCartItemList.get(position).getProductQuantity()))
        );

        // Event
        holder.setListener(new IImageButtonListener() {
            @Override
            public void onImageButtonListener(View view, int position, boolean isDecrease) {
                if (isDecrease) {
                    if (mCartItemList.get(position).getProductQuantity() > 0) {
                        mCartItemList.get(position)
                                .setProductQuantity(mCartItemList
                                        .get(position)
                                        .getProductQuantity()-1);

                        DatabaseUtils.updateCart(mCartDatabase, mCartItemList.get(position));
                    }
                } else {
                    if (mCartItemList.get(position).getProductQuantity() < 99) {
                        mCartItemList.get(position)
                                .setProductQuantity(mCartItemList
                                        .get(position)
                                        .getProductQuantity()+1);
                        DatabaseUtils.updateCart(mCartDatabase, mCartItemList.get(position));
                    }
                }
                holder.txt_quantity.setText(
                        new StringBuilder(String.valueOf(mCartItemList.get(position).getProductQuantity()))
                );
                mICartItemUpdateListener.onCartItemUpdateSuccess();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCartItemList.size();
    }

    interface  IImageButtonListener {
        void onImageButtonListener(View view, int position, boolean isDecrease);
    }

    public class MyViewHolder  extends RecyclerView.ViewHolder{

        TextView txt_cart_name, txt_cart_price, txt_quantity;
        ImageView img_decrease, img_increase, img_product;

        IImageButtonListener mListener;

        public void setListener(IImageButtonListener listener) {
            mListener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txt_cart_name = itemView.findViewById(R.id.txt_cart_name);
            txt_cart_price = itemView.findViewById(R.id.txt_cart_price);
            txt_quantity = itemView.findViewById(R.id.txt_cart_quantity);

            img_decrease = itemView.findViewById(R.id.img_decrease);
            img_increase = itemView.findViewById(R.id.img_increase);
            img_product = itemView.findViewById(R.id.cart_img);

            // Event
            img_decrease.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onImageButtonListener(v, getAdapterPosition(), true);
                }
            });

            img_increase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onImageButtonListener(v, getAdapterPosition(), false);
                }
            });
        }
    }
}

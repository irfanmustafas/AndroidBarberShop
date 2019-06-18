package ydkim2110.com.androidbarberbooking.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ydkim2110.com.androidbarberbooking.Model.Banner;
import ydkim2110.com.androidbarberbooking.R;

public class LookbookAdapter extends RecyclerView.Adapter<LookbookAdapter.MyViewHolder> {

    private static final String TAG = LookbookAdapter.class.getSimpleName();

    private Context mContext;
    private List<Banner> mLookbook;

    public LookbookAdapter(Context context, List<Banner> lookbook) {
        mContext = context;
        this.mLookbook = lookbook;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_look_book, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get().load(mLookbook.get(position).getImage()).into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mLookbook.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView mImageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.image_look_book);
        }
    }
}

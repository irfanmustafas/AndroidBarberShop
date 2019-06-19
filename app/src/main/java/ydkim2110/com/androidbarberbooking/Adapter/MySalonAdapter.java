package ydkim2110.com.androidbarberbooking.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import ydkim2110.com.androidbarberbooking.Common.Common;
import ydkim2110.com.androidbarberbooking.Interface.IRecyclerItemSelectedListener;
import ydkim2110.com.androidbarberbooking.Model.Salon;
import ydkim2110.com.androidbarberbooking.R;

public class MySalonAdapter extends RecyclerView.Adapter<MySalonAdapter.MyViewHolder> {

    private Context mContext;
    private List<Salon> salonList;
    private List<CardView> cardViewList;
    private LocalBroadcastManager mLocalBroadcastManager;

    public MySalonAdapter(Context context, List<Salon> salonList) {
        mContext = context;
        this.salonList = salonList;
        cardViewList = new ArrayList<>();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_salon, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_salon_name.setText(salonList.get(position).getName());
        holder.txt_salon_address.setText(salonList.get(position).getAddress());

        if(!cardViewList.contains(holder.card_salon)) {
            cardViewList.add(holder.card_salon);
        }

        holder.setIRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelected(View view, int position) {
                // Set white background for all card not be selected
                for (CardView cardView : cardViewList) {
                    cardView.setCardBackgroundColor(mContext.getResources()
                            .getColor(android.R.color.white));
                }

                // Set selected BG for only selected item
                holder.card_salon.setCardBackgroundColor(mContext.getResources()
                        .getColor(android.R.color.holo_orange_dark));

                // Send Broadcast to tell Booking Activity enable Button next
                Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);
                // We need send Salon Object to intent, so we must implement Parcelable for Salon Object
                // And we need add more property "salonId" to select all barber of this salon
                // Because we just add 'salonId', so we need set it when user load all salon
                // salonId is just our property, so it can't be parse from Firestore, need set it by manually
                intent.putExtra(Common.KEY_STEP, 1);
                intent.putExtra(Common.KEY_SALON_STORE, salonList.get(position));
                mLocalBroadcastManager.sendBroadcast(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return salonList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView txt_salon_name;
        private TextView txt_salon_address;
        private CardView card_salon;

        private IRecyclerItemSelectedListener mIRecyclerItemSelectedListener;

        public void setIRecyclerItemSelectedListener(IRecyclerItemSelectedListener IRecyclerItemSelectedListener) {
            this.mIRecyclerItemSelectedListener = IRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            card_salon = itemView.findViewById(R.id.card_salon);
            txt_salon_name = itemView.findViewById(R.id.txt_salon_name);
            txt_salon_address = itemView.findViewById(R.id.txt_salon_address);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mIRecyclerItemSelectedListener.onItemSelected(v, getAdapterPosition());
        }
    }
}

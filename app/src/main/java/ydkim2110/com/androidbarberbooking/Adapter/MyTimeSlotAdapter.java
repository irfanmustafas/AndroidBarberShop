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

import org.greenrobot.eventbus.EventBus;

import ydkim2110.com.androidbarberbooking.Common.Common;
import ydkim2110.com.androidbarberbooking.Interface.IRecyclerItemSelectedListener;
import ydkim2110.com.androidbarberbooking.Model.EventBus.EnableNextButton;
import ydkim2110.com.androidbarberbooking.Model.TimeSlot;
import ydkim2110.com.androidbarberbooking.R;

public class MyTimeSlotAdapter extends RecyclerView.Adapter<MyTimeSlotAdapter.MyViewHolder> {

    private static final String TAG = MyTimeSlotAdapter.class.getSimpleName();

    private Context mContext;
    private List<TimeSlot> mTimeSlotList;
    private List<CardView> mCardViewList;
    //private LocalBroadcastManager mLocalBroadcastManager;

    public MyTimeSlotAdapter(Context context) {
        this.mContext = context;
        this.mTimeSlotList = new ArrayList<>();
        this.mCardViewList = new ArrayList<>();
        //this.mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    public MyTimeSlotAdapter(Context context, List<TimeSlot> timeSlotList) {
        mContext = context;
        mTimeSlotList = timeSlotList;
        this.mCardViewList = new ArrayList<>();
        //this.mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_time_slot, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_time_slot.setText(new StringBuilder(Common.convertTimeSlotToString(position)).toString());
        // If all position is available, just show list
        if (mTimeSlotList.size() == 0) {
            holder.card_time_slot.setEnabled(true);
            holder.card_time_slot.setCardBackgroundColor(mContext.getResources().getColor(android.R.color.white));
            holder.txt_time_slot_description.setText("Available!");
            holder.txt_time_slot_description.setTextColor(mContext.getResources().getColor(android.R.color.black));
            holder.txt_time_slot.setTextColor(mContext.getResources().getColor(android.R.color.black));
        }
        // If have position is full (booked)
        else {
            for (TimeSlot slotValue : mTimeSlotList) {
                // Loop all time slot from server and set different color
                int slot = Integer.parseInt(slotValue.getSlot().toString());
                // IF slot == position
                if (slot == position) {
                    // we will set tag for all time slot is full
                    // so base on tag, we can set all remain card background without change full time slot
                    holder.card_time_slot.setEnabled(false);
                    holder.card_time_slot.setTag(Common.DISABLE_TAG);
                    holder.card_time_slot.setCardBackgroundColor(mContext.getResources().getColor(android.R.color.darker_gray));
                    holder.txt_time_slot_description.setText("Full");
                    holder.txt_time_slot_description.setTextColor(mContext.getResources().getColor(android.R.color.black));
                    holder.txt_time_slot.setTextColor(mContext.getResources().getColor(android.R.color.black));
                }
            }
        }

        // Add all cart to list (20 card because we have 20 time slot)
        if (!mCardViewList.contains(holder.card_time_slot)) {
            mCardViewList.add(holder.card_time_slot);
        }

        // Check if card time slot is available
        // No add card already in carViewList
        if (!mTimeSlotList.contains(position)) {
            holder.setIRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                @Override
                public void onItemSelected(View view, int position) {
                    // Loop all card in card list
                    for (CardView cardView : mCardViewList) {
                        // Only available card time slot be change
                        if (cardView.getTag() == null) {
                            cardView.setCardBackgroundColor(mContext.getResources()
                                    .getColor(android.R.color.white));
                        }
                    }
                    // Our selected card will be change color
                    holder.card_time_slot.setCardBackgroundColor(mContext.getResources()
                            .getColor(android.R.color.holo_orange_dark));

                    // After that, send broadcast to enable button next
//                    Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);
                    // Put index of time slot we have selected
//                    intent.putExtra(Common.KEY_TIME_SLOT, position);
                    // Go to step3
//                    intent.putExtra(Common.KEY_STEP, 3);
//                    mLocalBroadcastManager.sendBroadcast(intent);

                    // EventBus
                    EventBus.getDefault().postSticky(new EnableNextButton(3, position));


                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return Common.TIME_SLOT_TOTAL;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView txt_time_slot, txt_time_slot_description;
        private CardView card_time_slot;

        private IRecyclerItemSelectedListener mIRecyclerItemSelectedListener;

        private void setIRecyclerItemSelectedListener(IRecyclerItemSelectedListener IRecyclerItemSelectedListener) {
            mIRecyclerItemSelectedListener = IRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            card_time_slot = itemView.findViewById(R.id.card_time_slot);
            txt_time_slot = itemView.findViewById(R.id.txt_time_slot);
            txt_time_slot_description = itemView.findViewById(R.id.txt_time_description);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mIRecyclerItemSelectedListener.onItemSelected(v, getAdapterPosition());
        }
    }
}

package ydkim2110.com.androidbarberbooking.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ydkim2110.com.androidbarberbooking.Model.BookingInformation;
import ydkim2110.com.androidbarberbooking.R;

public class MyHistoryAdapter extends RecyclerView.Adapter<MyHistoryAdapter.MyViewHolder> {

    private Context mContext;
    private List<BookingInformation> mBookingInformationList;

    public MyHistoryAdapter(Context context, List<BookingInformation> bookingInformationList) {
        mContext = context;
        mBookingInformationList = bookingInformationList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_history, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_salon_name.setText(mBookingInformationList.get(position).getSalonName());
        holder.txt_salon_address.setText(mBookingInformationList.get(position).getSalonAddress());
        holder.txt_booking_barber.setText(mBookingInformationList.get(position).getBarberName());
        holder.txt_booking_time.setText(mBookingInformationList.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return mBookingInformationList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private Unbinder mUnbinder;

        @BindView(R.id.txt_salon_name)
        TextView txt_salon_name;
        @BindView(R.id.txt_salon_address)
        TextView txt_salon_address;
        @BindView(R.id.txt_booking_time)
        TextView txt_booking_time;
        @BindView(R.id.txt_booking_barber)
        TextView txt_booking_barber;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            mUnbinder = ButterKnife.bind(this, itemView);
        }
    }
}

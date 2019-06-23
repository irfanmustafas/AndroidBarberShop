package ydkim2110.com.androidbarberbooking.Fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import dmax.dialog.SpotsDialog;
import ydkim2110.com.androidbarberbooking.Adapter.MyTimeSlotAdapter;
import ydkim2110.com.androidbarberbooking.Common.Common;
import ydkim2110.com.androidbarberbooking.Common.SpaceItemDecoration;
import ydkim2110.com.androidbarberbooking.Interface.ITimeSlotLoadListener;
import ydkim2110.com.androidbarberbooking.Model.TimeSlot;
import ydkim2110.com.androidbarberbooking.R;

public class BookingStep3Fragment extends Fragment implements ITimeSlotLoadListener {

    private static final String TAG = BookingStep3Fragment.class.getSimpleName();

    private static BookingStep3Fragment instance;

    public static BookingStep3Fragment getInstance() {
        if (instance == null) {
            instance = new BookingStep3Fragment();
        }
        return instance;
    }

    DocumentReference barberDoc;
    ITimeSlotLoadListener mITimeSlotLoadListener;
    AlertDialog mDialog;

    Calendar selected_date;

    Unbinder mUnbinder;
    LocalBroadcastManager mLocalBroadcastManager;

    @BindView(R.id.recycler_time_slot)
    RecyclerView recycler_time_slot;
    @BindView(R.id.calendarView)
    HorizontalCalendarView calendarView;
    SimpleDateFormat mSimpleDateFormat;

    BroadcastReceiver displayTimeSlot = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Calendar date = Calendar.getInstance();
            date.add(Calendar.DATE, 0);
            loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                    mSimpleDateFormat.format(date.getTime()));
        }
    };

    private void loadAvailableTimeSlotOfBarber(String barberId, String bookDate) {
        Log.d(TAG, "loadAvailableTimeSlotOfBarber: called!!");

        mDialog.show();

        barberDoc = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.city)
                .collection("Branch")
                .document(Common.currentSalon.getSalonId())
                .collection("Barber")
                .document(Common.currentBarber.getBarberId());

        // Get information of this barber
        barberDoc.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            // If barber available
                            if (documentSnapshot.exists()) {
                                // Get information of booking
                                // If not created, return empty;
                                CollectionReference date = FirebaseFirestore.getInstance()
                                        .collection("AllSalon")
                                        .document(Common.city)
                                        .collection("Branch")
                                        .document(Common.currentSalon.getSalonId())
                                        .collection("Barber")
                                        .document(Common.currentBarber.getBarberId())
                                        // bookDate is date simpleformat with dd_MM_yyyy = 28_03_2019
                                        .collection(bookDate);

                                date.get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    QuerySnapshot querySnapshot = task.getResult();
                                                    // If don't have any appointment
                                                    if (querySnapshot.isEmpty()) {
                                                        mITimeSlotLoadListener.onTimeSlotLoadEmpty();
                                                    }
                                                    // If have appointment
                                                    else {
                                                        List<TimeSlot> timeSlots = new ArrayList<>();
                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                            timeSlots.add(document.toObject(TimeSlot.class));
                                                        }
                                                        mITimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlots);
                                                    }
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mITimeSlotLoadListener.onTimeSlotLoadFailed(e.getMessage());
                                    }
                                });
                            }
                        }
                    }
                });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mITimeSlotLoadListener = this;

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        mLocalBroadcastManager.registerReceiver(displayTimeSlot, new IntentFilter(Common.KEY_DISPLAY_TIME_SLOT));

        // 28_03)2019 (this is key)
        mSimpleDateFormat = new SimpleDateFormat("dd_MM_yyyy");

        mDialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();

        selected_date = Calendar.getInstance();
        // Init current date
        selected_date.add(Calendar.DATE, 0);
    }

    @Override
    public void onDestroy() {
        mLocalBroadcastManager.unregisterReceiver(displayTimeSlot);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_booking_step_three,
                container, false);

        mUnbinder = ButterKnife.bind(this, view);

        initView(view);

        return view;
    }

    private void initView(View view) {
        Log.d(TAG, "initView: called!!");

        recycler_time_slot.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        recycler_time_slot.setLayoutManager(gridLayoutManager);
        recycler_time_slot.addItemDecoration(new SpaceItemDecoration(8));

        // Calendar
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, 0);

        Calendar endDate = Calendar.getInstance();
        // 2 day left
        endDate.add(Calendar.DATE, 2);

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(view, R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(1)
                .mode(HorizontalCalendar.Mode.DAYS)
                .defaultSelectedDate(startDate)
                .build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                if (Common.bookingDate.getTimeInMillis() != date.getTimeInMillis()) {
                    // This code will not load again if you select new day same with day selected
                    Common.bookingDate = date;
                    loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                            mSimpleDateFormat.format(date.getTime()));
                }
            }
        });
    }

    @Override
    public void onTimeSlotLoadSuccess(List<TimeSlot> timeSlotList) {
        Log.d(TAG, "onTimeSlotLoadSuccess: called!!");
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(getContext(), timeSlotList);
        recycler_time_slot.setAdapter(adapter);

        mDialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadFailed(String message) {
        Log.d(TAG, "onTimeSlotLoadFailed: called!!");
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        mDialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadEmpty() {
        Log.d(TAG, "onTimeSlotLoadEmpty: called!!");
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(getContext());
        recycler_time_slot.setAdapter(adapter);

        mDialog.dismiss();
    }
}

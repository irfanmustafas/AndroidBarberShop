package ydkim2110.com.androidbarberbooking.Fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.AccountKit;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nex3z.notificationbadge.NotificationBadge;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import ss.com.bannerslider.Slider;
import ydkim2110.com.androidbarberbooking.Adapter.HomeSliderAdapter;
import ydkim2110.com.androidbarberbooking.Adapter.LookbookAdapter;
import ydkim2110.com.androidbarberbooking.BookingActivity;
import ydkim2110.com.androidbarberbooking.CartActivity;
import ydkim2110.com.androidbarberbooking.Common.Common;
import ydkim2110.com.androidbarberbooking.Database.CartDatabase;
import ydkim2110.com.androidbarberbooking.Database.DatabaseUtils;
import ydkim2110.com.androidbarberbooking.HistoryActivity;
import ydkim2110.com.androidbarberbooking.Interface.IBannerLoadListener;
import ydkim2110.com.androidbarberbooking.Interface.IBookingInfoLoadListener;
import ydkim2110.com.androidbarberbooking.Interface.IBookingInformationChangeListener;
import ydkim2110.com.androidbarberbooking.Interface.ICountItemInCartListener;
import ydkim2110.com.androidbarberbooking.Interface.ILookbookLoadListener;
import ydkim2110.com.androidbarberbooking.Model.Banner;
import ydkim2110.com.androidbarberbooking.Model.BookingInformation;
import ydkim2110.com.androidbarberbooking.R;
import ydkim2110.com.androidbarberbooking.Service.PicassoImageLoadingService;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment
        implements ILookbookLoadListener, IBannerLoadListener, IBookingInfoLoadListener,
        IBookingInformationChangeListener, ICountItemInCartListener {

    private static final String TAG = HomeFragment.class.getSimpleName();

    private Unbinder unbinder;

    private AlertDialog mDialog;

    private CartDatabase mCartDatabase;

    @BindView(R.id.layout_user_information)
    LinearLayout layout_user_information;
    @BindView(R.id.txt_user_name)
    TextView txt_user_name;
    @BindView(R.id.txt_member_type)
    TextView txt_member_type;
    @BindView(R.id.banner_slider)
    Slider banner_slider;
    @BindView(R.id.recycler_look_book)
    RecyclerView recycler_look_book;
    @BindView(R.id.card_view_booking)
    CardView card_view_booking;

    @BindView(R.id.card_booking_info)
    CardView card_booking_info;
    @BindView(R.id.txt_salon_address)
    TextView txt_salon_address;
    @BindView(R.id.txt_salon_barber)
    TextView txt_salon_barber;
    @BindView(R.id.txt_time)
    TextView txt_time;
    @BindView(R.id.txt_time_remain)
    TextView txt_time_remain;

    @BindView(R.id.notification_badge)
    NotificationBadge notification_badge;


    @OnClick(R.id.card_view_booking)
    void booking() {
        startActivity(new Intent(getActivity(), BookingActivity.class));
    }

    @OnClick(R.id.btn_delete_booking)
    void deleteBooking() {
        deleteBookingFromBarber(false);
    }

    @OnClick(R.id.btn_change_booking)
    void changeBooking() {
        changeBookingFromUser();
    }

    @OnClick(R.id.card_view_cart)
    void openCartActivity() {
        startActivity(new Intent(getActivity(), CartActivity.class));
    }

    @OnClick(R.id.card_view_history)
    void openHistoryActivity() {
        startActivity(new Intent(getActivity(), HistoryActivity.class));
    }

    private void changeBookingFromUser() {
        androidx.appcompat.app.AlertDialog.Builder confirmDialog =
                new androidx.appcompat.app.AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setTitle("Hey!")
                .setMessage("Do you really want to change booking information?\nBecause we will delete your old booking information\nJust confirm")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteBookingFromBarber(true);
                    }
                });
        confirmDialog.show();
    }

    private void deleteBookingFromBarber(boolean isChange) {
        Log.d(TAG, "deleteBookingFromUser: called!!");

        // To delete booking, first we need delete from Barber Collection
        // After that, we will delete from User booking collection
        // And final, delete event

        // We need Load Common.currentBooking because we need some data from BookingInformation
        if (Common.currentBooking != null) {
            mDialog.show();

            // Get booking information in barber object
            DocumentReference barberBookingInfo = FirebaseFirestore.getInstance()
                    .collection("AllSalon")
                    .document(Common.currentBooking.getCityBook())
                    .collection("Branch")
                    .document(Common.currentBooking.getSalonId())
                    .collection("Barber")
                    .document(Common.currentBooking.getBarberId())
                    .collection(Common.convertTimeSlotToStringKey(Common.currentBooking.getTimestamp()))
                    .document(Common.currentBooking.getSlot().toString());

            // When we have document, just delete it.
            barberBookingInfo
                    .delete()
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // After delete on Barber done
                            // We will start delete from User
                            deleteBookingFromUser(isChange);
                        }
                    });

        } else {
            Toast.makeText(getContext(), "Current Booking must not be null", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteBookingFromUser(boolean isChange) {
        Log.d(TAG, "deleteBookingFromUser: called!!");

        // First, we need get information from user object.
        if (!TextUtils.isEmpty(Common.currentBookingId)) {
            DocumentReference userBookingInfo = FirebaseFirestore.getInstance()
                    .collection("User")
                    .document(Common.currentUser.getPhoneNumber())
                    .collection("Booking")
                    .document(Common.currentBookingId);
            // Delete
            userBookingInfo
                    .delete()
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // After delete from User, just delete from calendar
                            // First, we need get save Uri of event we just add
                            Paper.init(getActivity());
                            if (Paper.book().read(Common.EVENT_URI_CACHE) != null) {
                                String eventString = Paper.book().read(Common.EVENT_URI_CACHE).toString();
                                Uri eventUri = null;
                                if (eventString != null && !TextUtils.isEmpty(eventString)) {
                                    eventUri = Uri.parse(eventString);
                                }
                                if (eventUri != null) {
                                    getActivity().getContentResolver().delete(eventUri, null, null);
                                }
                            }

                            Toast.makeText(getContext(), "Success delete booking!", Toast.LENGTH_SHORT).show();

                            // Refresh
                            loadUserBooking();

                            // Check if isChange -> Call from change button, we will fired interface
                            if (isChange) {
                                mIBookingInformationChangeListener.onBookingInformationChange();
                            }

                            mDialog.dismiss();
                        }
                    });

        } else {
            mDialog.dismiss();

            Toast.makeText(getContext(), "Booking Information ID  must not be empty",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Firestore
    CollectionReference bannerRef, lookbookRef;

    // Interface
    IBannerLoadListener iBannerLoadListener;
    ILookbookLoadListener iLookbookLoadListener;
    IBookingInfoLoadListener mIBookingInfoLoadListener;
    IBookingInformationChangeListener mIBookingInformationChangeListener;

    ListenerRegistration userBookingListener = null;
    EventListener<QuerySnapshot> userBookingEvent = null;

    public HomeFragment() {
        bannerRef = FirebaseFirestore.getInstance().collection("Banner");
        lookbookRef = FirebaseFirestore.getInstance().collection("LookBook");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserBooking();
        countCartItem();
    }

    private void loadUserBooking() {
        Log.d(TAG, "loadUserBooking: called");

        CollectionReference userBooking = FirebaseFirestore.getInstance()
                .collection("User")
                .document(Common.currentUser.getPhoneNumber())
                .collection("Booking");

        // Get current date
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        Timestamp todayTimestamp = new Timestamp(calendar.getTime());

        // Select booking information from firestore with done = false and timestamp greater today
        userBooking
                .whereGreaterThanOrEqualTo("timestamp", todayTimestamp)
                .whereEqualTo("done", false)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                    BookingInformation bookingInformation = queryDocumentSnapshot.toObject(BookingInformation.class);
                                    mIBookingInfoLoadListener.onBookingInfoLoadSuccess(bookingInformation, queryDocumentSnapshot.getId());
                                    break;
                                }
                            } else {
                                mIBookingInfoLoadListener.onBookingInfoLoadEmpty();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mIBookingInfoLoadListener.onBookingInfoLoadFailed(e.getMessage());
                    }
                });

        // Here, after userBooking has benn assign data (collections)
        // We will make realtime listen here

        // If userBookingEvent already init
        if (userBookingEvent != null) {
            // Only add if userBookingListener == null
            if (userBookingListener == null) {
                // That mean we just add 1 time
                userBookingListener = userBooking
                        .addSnapshotListener(userBookingEvent);
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);

        mCartDatabase = CartDatabase.getInstance(getContext());

        // Init
        Slider.init(new PicassoImageLoadingService());
        iBannerLoadListener = this;
        iLookbookLoadListener = this;
        mIBookingInfoLoadListener = this;
        mIBookingInformationChangeListener = this;

        // Check is logged?
        if (AccountKit.getCurrentAccessToken() != null) {
            setUserInformation();
            loadBanner();
            loadLookbook();
            // need declare above loadUserBooking();
            initRealtimeUserBooking();
            loadUserBooking();
            countCartItem();
        }

        return view;
    }

    private void initRealtimeUserBooking() {
        Log.d(TAG, "initRealtimeUserBooking: called!!");
        // Warning: Please follow this step is carefully
        // Because if this step you do wrong, this will make
        // infinity loop in your app, and you will retrieve QUOTA LIMIT fom FireStore
        // If you do wrong, FireStore will get reah limit and you need wait for next 24hours for reset

        // We only init event if event is null
        if (userBookingEvent == null) {
            userBookingEvent = new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                    // in this event, when it fired, we will call loadUserBooking again
                    // to reload all booking information
                   loadUserBooking();
                }
            };
        }
    }

    private void countCartItem() {
        Log.d(TAG, "countCartItem: called!!");

        DatabaseUtils.countItemInCart(mCartDatabase, this);
    }

    private void loadLookbook() {
        Log.d(TAG, "loadLookbook: called!!");
        lookbookRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<Banner> lookbooks = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot bannerSnapshot: task.getResult()) {
                                Banner lookbook = bannerSnapshot.toObject(Banner.class);
                                lookbooks.add(lookbook);
                            }
                            iLookbookLoadListener.onLookbookLoadSuccess(lookbooks);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        iLookbookLoadListener.onLookbookLoadFailed(e.getMessage());
                    }
                });
    }

    private void loadBanner() {
        Log.d(TAG, "loadBanner: called!!");
        bannerRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<Banner> banners = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot bannerSnapshot: task.getResult()) {
                                Banner banner = bannerSnapshot.toObject(Banner.class);
                                banners.add(banner);
                            }
                            iBannerLoadListener.onBannerLoadSuccess(banners);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        iBannerLoadListener.onBannerLoadFailed(e.getMessage());
                    }
                });
    }

    private void setUserInformation() {
        layout_user_information.setVisibility(View.VISIBLE);
        txt_user_name.setText(Common.currentUser.getName());
    }

    @Override
    public void onLookbookLoadSuccess(List<Banner> banners) {
        Log.d(TAG, "onLookbookLoadSuccess: called!!");
        recycler_look_book.setHasFixedSize(true);
        recycler_look_book.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler_look_book.setAdapter(new LookbookAdapter(getActivity(), banners));
    }

    @Override
    public void onLookbookLoadFailed(String message) {
        Log.d(TAG, "onLookbookLoadFailed: called!!");
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBannerLoadSuccess(List<Banner> banners) {
        Log.d(TAG, "onBannerLoadSuccess: called!!");
        banner_slider.setAdapter(new HomeSliderAdapter(banners));
    }

    @Override
    public void onBannerLoadFailed(String message) {
        Log.d(TAG, "onBannerLoadFailed: called!!");
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBookingInfoLoadEmpty() {
        card_booking_info.setVisibility(View.GONE);
    }

    @Override
    public void onBookingInfoLoadSuccess(BookingInformation bookingInformation, String bookingId) {
        Log.d(TAG, "onBookingInfoLoadSuccess: called!!");

        Common.currentBooking = bookingInformation;
        Common.currentBookingId = bookingId;

        txt_salon_address.setText(bookingInformation.getSalonAddress());
        txt_salon_barber.setText(bookingInformation.getBarberName());
        txt_time.setText(bookingInformation.getTime());
        String dateRemain = DateUtils.getRelativeTimeSpanString(
                Long.valueOf(bookingInformation.getTimestamp().toDate().getTime()),
                Calendar.getInstance().getTimeInMillis(), 0).toString();
        txt_time_remain.setText(dateRemain);

        card_booking_info.setVisibility(View.VISIBLE);

        mDialog.dismiss();
    }

    @Override
    public void onBookingInfoLoadFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBookingInformationChange() {
        Log.d(TAG, "onBookingInformationChange: called");
        // Here we will just start activity Booking
        startActivity(new Intent(getActivity(), BookingActivity.class));
    }

    @Override
    public void onCartItemCountSuccess(int count) {
        Log.d(TAG, "onCartItemCountSuccess: called!!");
        if (count == 0) {
            notification_badge.setVisibility(View.GONE);
        } else {
            notification_badge.setVisibility(View.VISIBLE);
            notification_badge.setText(String.valueOf(count));
        }
    }

    @Override
    public void onDestroy() {
        if (userBookingListener != null)
            userBookingListener.remove();
        super.onDestroy();
    }
}

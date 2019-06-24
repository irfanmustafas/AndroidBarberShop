package ydkim2110.com.androidbarberbooking.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import ydkim2110.com.androidbarberbooking.Common.Common;
import ydkim2110.com.androidbarberbooking.Model.BookingInformation;
import ydkim2110.com.androidbarberbooking.Model.FCMResponse;
import ydkim2110.com.androidbarberbooking.Model.FCMSendData;
import ydkim2110.com.androidbarberbooking.Model.MyNotification;
import ydkim2110.com.androidbarberbooking.Model.MyToken;
import ydkim2110.com.androidbarberbooking.R;
import ydkim2110.com.androidbarberbooking.Retrofit.IFCMApi;
import ydkim2110.com.androidbarberbooking.Retrofit.RetrofitClient;

public class BookingStep4Fragment extends Fragment {

    private static final String TAG = BookingStep4Fragment.class.getSimpleName();

    private static BookingStep4Fragment instance;

    public static BookingStep4Fragment getInstance() {
        if (instance == null) {
            instance = new BookingStep4Fragment();
        }
        return instance;
    }

    private SimpleDateFormat mSimpleDateFormat;
    private LocalBroadcastManager mLocalBroadcastManager;

    private AlertDialog mDialog;

    private IFCMApi mIFCMApi;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @BindView(R.id.txt_booking_barber_text)
    TextView txt_booking_barber_text;
    @BindView(R.id.txt_booking_time_text)
    TextView txt_booking_time_text;
    @BindView(R.id.txt_salon_salon_address)
    TextView txt_salon_salon_address;
    @BindView(R.id.txt_salon_name)
    TextView txt_salon_name;
    @BindView(R.id.txt_salon_open_hours)
    TextView txt_salon_open_hours;
    @BindView(R.id.txt_salon_phone)
    TextView txt_salon_phone;
    @BindView(R.id.txt_salon_website)
    TextView txt_salon_website;

    @OnClick(R.id.btn_confirm)
    void confirmBooking() {
        mDialog.show();

        // Process TimeStamp
        // We will use TimeStamp to filter all booking with date is greater today
        // For only display all future booking
        String startTime = Common.convertTimeSlotToString(Common.currentTimeSlot);
        String[] convertTime = startTime.split("~");

        String[] startTimeConvert = convertTime[0].split(":");
        int startHourInt = Integer.parseInt(startTimeConvert[0].trim());
        int startMinInt = Integer.parseInt(startTimeConvert[1].trim());

        Calendar bookingDateWithoutHours = Calendar.getInstance();
        bookingDateWithoutHours.setTimeInMillis(Common.bookingDate.getTimeInMillis());
        bookingDateWithoutHours.set(Calendar.HOUR_OF_DAY, startHourInt);
        bookingDateWithoutHours.set(Calendar.MINUTE, startMinInt);

        // Create timestamp object and apply to BookingInformation
        Timestamp timestamp = new Timestamp(bookingDateWithoutHours.getTime());

        // creating booking information
        BookingInformation bookingInformation = new BookingInformation();

        bookingInformation.setCityBook(Common.city);
        bookingInformation.setTimestamp(timestamp);
        // Always false, because we will use this field to filter for display
        bookingInformation.setDone(false);
        bookingInformation.setBarberId(Common.currentBarber.getBarberId());
        bookingInformation.setBarberName(Common.currentBarber.getName());
        bookingInformation.setCustomerName(Common.currentUser.getName());
        bookingInformation.setCustomerPhone(Common.currentUser.getPhoneNumber());
        bookingInformation.setSalonId(Common.currentSalon.getSalonId());
        bookingInformation.setSalonAddress(Common.currentSalon.getAddress());
        bookingInformation.setSalonName(Common.currentSalon.getName());
        bookingInformation.setTime(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot))
                .append(" at ")
                .append(mSimpleDateFormat.format(bookingDateWithoutHours.getTime())).toString());
        bookingInformation.setSlot(Long.valueOf(Common.currentTimeSlot));

        // submit barber document
        DocumentReference bookingDate = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.city)
                .collection("Branch")
                .document(Common.currentSalon.getSalonId())
                .collection("Barber")
                .document(Common.currentBarber.getBarberId())
                .collection(Common.simpleFormatDate.format(Common.bookingDate.getTime()))
                .document(String.valueOf(Common.currentTimeSlot));

        // write data
        bookingDate.set(bookingInformation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Here we can write an function to check
                        // If already exist an booking, we will prevent new booking
                        addToUserBooking(bookingInformation);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), ""+e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addToUserBooking(BookingInformation bookingInformation) {
        Log.d(TAG, "addToUserBooking: called");

        // First, create new collection
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

        // Check if exist document in this collection
        // if have any document with field done = false;
        userBooking
                .whereGreaterThanOrEqualTo("timestamp", todayTimestamp)
                .whereEqualTo("done", false)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().isEmpty()) {
                            // Set data
                            userBooking.document()
                                    .set(bookingInformation)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            // Create notification
                                            MyNotification myNotification = new MyNotification();
                                            myNotification.setUid(UUID.randomUUID().toString());
                                            myNotification.setTitle("New Booking");
                                            myNotification.setContent("You have a new appointment for customer hair care!");
                                            // We will only filter notification with 'read' is false on barber staff
                                            myNotification.setRead(false);
                                            myNotification.setServerTimestamp(new Timestamp(calendar.getTime()));

                                            // Submit Notification to 'Notifications' collection of Barber
                                            FirebaseFirestore.getInstance()
                                                    .collection("AllSalon")
                                                    .document(Common.city)
                                                    .collection("Branch")
                                                    .document(Common.currentSalon.getSalonId())
                                                    .collection("Barber")
                                                    .document(Common.currentBarber.getBarberId())
                                                    // If  it not available, it will be create automatically
                                                    .collection("Notifications")
                                                    // Create unique key
                                                    .document(myNotification.getUid())
                                                    .set(myNotification)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                           // First, get Token base on Barber id
                                                            FirebaseFirestore.getInstance()
                                                                    .collection("Tokens")
                                                                    .whereEqualTo("userPhone", Common.currentBarber.getUsername())
                                                                    .limit(1)
                                                                    .get()
                                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                        @SuppressLint("CheckResult")
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                            if (task.isSuccessful() && task.getResult().size() > 0) {
                                                                                MyToken myToken = new MyToken();
                                                                                for (DocumentSnapshot tokenSnapshot : task.getResult()) {
                                                                                    myToken = tokenSnapshot.toObject(MyToken.class);
                                                                                }

                                                                                // Create data to send
                                                                                FCMSendData sendRequest = new FCMSendData();
                                                                                Map<String, String> dataSend = new HashMap<>();
                                                                                dataSend.put(Common.TITLE_KEY, "New Booking");
                                                                                dataSend.put(Common.CONTENT_KEY, "You have new booking from user "+Common.currentUser.getName());

                                                                                sendRequest.setTo(myToken.getToken());
                                                                                sendRequest.setData(dataSend);

                                                                                mCompositeDisposable.add(mIFCMApi.sendNotification(sendRequest)
                                                                                        .subscribeOn(Schedulers.io())
                                                                                        .observeOn(AndroidSchedulers.mainThread())
                                                                                        .subscribe(new Consumer<FCMResponse>() {
                                                                                            @Override
                                                                                            public void accept(FCMResponse fcmResponse) throws Exception {

                                                                                                mDialog.dismiss();

                                                                                                addToCalendar(Common.bookingDate,
                                                                                                        Common.convertTimeSlotToString(Common.currentTimeSlot));
                                                                                                resetStaticData();
                                                                                                getActivity().finish();
                                                                                                Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        }, new Consumer<Throwable>() {
                                                                                            @Override
                                                                                            public void accept(Throwable throwable) throws Exception {
                                                                                                Log.d(TAG, "NOTIFICATION_ERROR: "+throwable.getMessage());
                                                                                                addToCalendar(Common.bookingDate,
                                                                                                        Common.convertTimeSlotToString(Common.currentTimeSlot));
                                                                                                resetStaticData();
                                                                                                getActivity().finish();
                                                                                                Toast.makeText(getContext(), "Success!", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        }));
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            mDialog.dismiss();
                                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            if (mDialog.isShowing())
                                mDialog.dismiss();

                            resetStaticData();
                            // close activity
                            getActivity().finish();
                            Toast.makeText(getContext(), "Success!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addToCalendar(Calendar bookingDate, String startDate) {
        Log.d(TAG, "addToCalendar: called!!");

        String startTime = Common.convertTimeSlotToString(Common.currentTimeSlot);
        String[] convertTime = startTime.split("~");

        String[] startTimeConvert = convertTime[0].split(":");
        int startHourInt = Integer.parseInt(startTimeConvert[0].trim());
        int startMinInt = Integer.parseInt(startTimeConvert[1].trim());

        String[] endTimeConvert = convertTime[1].split(":");
        int endHourInt = Integer.parseInt(endTimeConvert[0].trim());
        int endMinInt = Integer.parseInt(endTimeConvert[1].trim());

        Calendar startEvent = Calendar.getInstance();
        startEvent.setTimeInMillis(bookingDate.getTimeInMillis());
        startEvent.set(Calendar.HOUR_OF_DAY, startHourInt); // Set event start hour
        startEvent.set(Calendar.MINUTE, startMinInt); // Set event start min

        Calendar endEvent = Calendar.getInstance();
        endEvent.setTimeInMillis(bookingDate.getTimeInMillis());
        endEvent.set(Calendar.HOUR_OF_DAY, endHourInt); // Set event start hour
        endEvent.set(Calendar.MINUTE, endMinInt); // Set event start min

        // After we have startEvent and endEvent, convert, convert it to format String
        SimpleDateFormat calendarDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String startEventTime = calendarDateFormat.format(startEvent.getTime());
        String endEventTime = calendarDateFormat.format(endEvent.getTime());

        addToDeviceCalendar(startEventTime, endEventTime, "Haircut Booking",
                new StringBuilder("Hair cut from")
                        .append(startTime)
                        .append(" with ")
                        .append(Common.currentBarber.getName())
                        .append(" at ")
                        .append(Common.currentSalon.getName()).toString(),
                                new StringBuilder("Address: ").append(Common.currentSalon.getAddress()).toString());
    }

    private void addToDeviceCalendar(String startEventTime, String endEventTime, String title,
                                     String description, String location) {
        Log.d(TAG, "addToDeviceCalendar: called!!");

        SimpleDateFormat calendarDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        try {
            Date start = calendarDateFormat.parse(startEventTime);
            Date end = calendarDateFormat.parse(endEventTime);

            ContentValues event = new ContentValues();

            event.put(CalendarContract.Events.CALENDAR_ID, getCalendar(getContext()));
            event.put(CalendarContract.Events.TITLE, title);
            event.put(CalendarContract.Events.DESCRIPTION, description);
            event.put(CalendarContract.Events.EVENT_LOCATION, location);

            event.put(CalendarContract.Events.DTSTART, start.getTime());
            event.put(CalendarContract.Events.DTEND, end.getTime());
            event.put(CalendarContract.Events.ALL_DAY, 0);
            event.put(CalendarContract.Events.HAS_ALARM, 1);

            String timezone = TimeZone.getDefault().getID();
            event.put(CalendarContract.Events.EVENT_TIMEZONE, timezone);

            Uri calendars;
            if (Build.VERSION.SDK_INT >= 8)
                calendars = Uri.parse("content://com.android.calendar/events");
            else
                calendars = Uri.parse("content://calendar/events");

            Uri uri_save = getActivity().getContentResolver().insert(calendars, event);

            // Save to cache
            Paper.init(getActivity());
            Paper.book().write(Common.EVENT_URI_CACHE, uri_save.toString());


        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private String getCalendar(Context context) {
        Log.d(TAG, "getCalendar: called!!");

        // Get default calendar ID of Calendar of Gmail
        String gmailIdCalendar = "";
        String projection[] = {"_id", "calendar_displayName"};
        Uri calendars = Uri.parse("content://com.android.calendar/calendars");

        ContentResolver contentResolver = context.getContentResolver();
        // Select all calendar
        Cursor managedCursor = contentResolver.query(calendars, projection, null, null, null);
        if (managedCursor.moveToFirst()) {
            String calName;
            int nameCol = managedCursor.getColumnIndex(projection[1]);
            int idCol = managedCursor.getColumnIndex(projection[0]);
            do {
                calName = managedCursor.getString(nameCol);
                if (calName.contains("@gmail.com")) {
                    gmailIdCalendar = managedCursor.getString(idCol);
                    break;
                }
            } while (managedCursor.moveToNext());
            managedCursor.close();
        }
        return gmailIdCalendar;
    }

    private void resetStaticData() {
        Log.d(TAG, "resetStaticData: called!!");

        Common.step = 0;
        Common.currentTimeSlot = -1;
        Common.currentSalon = null;
        Common.currentBarber = null;
        Common.bookingDate.add(Calendar.DATE, 0);
    }

    Unbinder mUnbinder;

    BroadcastReceiver confirmBookingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: called!!");
            setData();
        }
    };

    private void setData() {
        Log.d(TAG, "setData: called!!");
        txt_booking_barber_text.setText(Common.currentBarber.getName());
        txt_booking_time_text.setText(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot))
        .append(" at ")
        .append(mSimpleDateFormat.format(Common.bookingDate.getTime())));

        txt_salon_salon_address.setText(Common.currentSalon.getAddress());
        txt_salon_website.setText(Common.currentSalon.getWebsite());
        txt_salon_name.setText(Common.currentSalon.getName());
        txt_salon_open_hours.setText(Common.currentSalon.getOpenHours());

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: started!!");

        mIFCMApi = RetrofitClient.getInstance().create(IFCMApi.class);

        // Ally format for date display on confirm
        mSimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        mLocalBroadcastManager.registerReceiver(confirmBookingReceiver, new IntentFilter(Common.KEY_CONFIRM_BOOKING));

        mDialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
    }

    @Override
    public void onDestroy() {
        mLocalBroadcastManager.unregisterReceiver(confirmBookingReceiver);
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, "onCreate: started!!");
        View view =  inflater.inflate(R.layout.fragment_booking_step_four, container,false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

}

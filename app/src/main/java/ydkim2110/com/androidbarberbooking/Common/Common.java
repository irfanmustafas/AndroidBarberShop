package ydkim2110.com.androidbarberbooking.Common;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.core.app.NotificationCompat;

import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.paperdb.Paper;
import ydkim2110.com.androidbarberbooking.HomeActivity;
import ydkim2110.com.androidbarberbooking.Model.Barber;
import ydkim2110.com.androidbarberbooking.Model.BookingInformation;
import ydkim2110.com.androidbarberbooking.Model.MyToken;
import ydkim2110.com.androidbarberbooking.Model.Salon;
import ydkim2110.com.androidbarberbooking.Model.User;
import ydkim2110.com.androidbarberbooking.R;
import ydkim2110.com.androidbarberbooking.Service.MyFCMService;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class Common {

    public static final String KEY_ENABLE_BUTTON_NEXT = "ENABLE_BUTTON_NEXT";
    public static final String KEY_UNABLE_BUTTON_NEXT = "UNABLE_BUTTON_NEXT";
    public static final String KEY_SALON_STORE = "SALON_STORE";
    public static final String KEY_BARBER_LOAD_DONE = "BARBER_LOAD_DONE";
    public static final String KEY_DISPLAY_TIME_SLOT = "DISPLAY_TIME_SLOT";
    public static final String KEY_STEP = "STEP";
    public static final String KEY_BARBER_SELECTED = "BARBER_SELECTED";
    public static final int TIME_SLOT_TOTAL = 20;
    public static final Object DISABLE_TAG = "DISABLE";
    public static final String KEY_TIME_SLOT = "TIME_SLOT";
    public static final String KEY_CONFIRM_BOOKING = "CONFIRM_BOOKING";
    public static final String EVENT_URI_CACHE = "URI_EVENT_SAVE";
    public static final String TITLE_KEY = "title";
    public static final String CONTENT_KEY = "content";
    public static final String LOGGED_KEY = "UserLogged";
    public static final String RATING_INFORMATION_KEY = "RATING_INFORMATION";

    public static final String RATING_STATE_KEY = "RATING_STATE";
    public static final String RATING_SALON_ID = "RATING_SALON_ID";
    public static final String RATING_SALON_NAME = "RATING_SALON_NAME";
    public static final String RATING_BARBER_ID = "RATING_BARBER_ID";

    public static User currentUser;
    public static Salon currentSalon;
    public static Barber currentBarber;
    public static BookingInformation currentBooking;
    public static int currentTimeSlot=-1;
    public static int step = 0;
    public static String IS_LOGIN = "IsLogin";
    public static String city = "";
    public static String currentBookingId = "";

    public static Calendar bookingDate = Calendar.getInstance();
    public static SimpleDateFormat simpleFormatDate = new SimpleDateFormat("dd_MM_yyyy");

    public static String convertTimeSlotToString(int position) {
        switch (position) {
            case 0:
                return "9:00 ~ 9:30";
            case 1:
                return "9:30 ~ 10:00";
            case 2:
                return "10:00 ~ 10:30";
            case 3:
                return "10:30 ~ 11:00";
            case 4:
                return "11:00 ~ 11:30";
            case 5:
                return "11:30 ~ 12:00";
            case 6:
                return "12:00 ~ 12:30";
            case 7:
                return "12:30 ~ 13:00";
            case 8:
                return "13:00 ~ 13:30";
            case 9:
                return "13:30 ~ 14:00";
            case 10:
                return "14:00 ~ 14:30";
            case 11:
                return "14:30 ~ 15:00";
            case 12:
                return "15:00 ~ 15:30";
            case 13:
                return "15:30 ~ 16:00";
            case 14:
                return "16:00 ~ 16:30";
            case 16:
                return "16:30 ~ 17:00";
            case 17:
                return "17:00 ~ 17:30";
            case 18:
                return "17:30 ~ 18:00";
            case 19:
                return "18:00 ~ 18:30";
            case 20:
                return "18:30 ~ 19:00";
                default:
                    return "Closed!";
        }
    }

    public static String convertTimeSlotToStringKey(Timestamp timestamp) {
        Date date = timestamp.toDate();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy");
        return simpleDateFormat.format(date);
    }

    public static String formatShoppingItemName(String name) {
        return name.length() > 13 ? new StringBuilder(name.substring(0, 10)).append("...").toString() : name;
    }

    public static void showNotification(Context context, int noti_id, String title, String content, Intent intent) {
        PendingIntent pendingIntent = null;
        if (intent != null) {
            pendingIntent = PendingIntent.getActivity(context,
                    noti_id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        String NOTIFICATION_CHANNEL_ID = "ydkim2110_barber_client_app";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "My Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }

        Notification notification = builder.build();

        notificationManager.notify(noti_id, notification);
    }

    public static void showRatingDialog(Context context, String stateName, String salonID,
                                        String salonName, String barberId) {
        Log.d(TAG, "showRatingDialog: called!!");
        // First, we need get DocumentReference of Barber
        DocumentReference barberNeedRateRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(stateName)
                .collection("Branch")
                .document(salonID)
                .collection("Barber")
                .document(barberId);

        barberNeedRateRef.get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: "+e.getMessage());
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        Log.d(TAG, "onComplete: called!!");
                        if (task.isSuccessful()) {
                            Barber barberRate = task.getResult().toObject(Barber.class);
                            barberRate.setBarberId(task.getResult().getId());

                            // Create View for dialog
                            View view = LayoutInflater.from(context)
                                    .inflate(R.layout.layout_rating_dialog, null);

                            // Widget
                            TextView txt_salon_name = view.findViewById(R.id.txt_salon_name);
                            TextView txt_barber_name = view.findViewById(R.id.txt_barber_name);
                            AppCompatRatingBar ratingBar = view.findViewById(R.id.rating);

                            // Set Info
                            txt_salon_name.setText(salonName);
                            txt_barber_name.setText(barberRate.getName());

                            // Create Dialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                    .setView(view)
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // IF select OK, we will update
                                            // rating information to FireStore

                                            Double original_rating = barberRate.getRating();
                                            Long ratingTimes = barberRate.getRatingTimes();
                                            float userRating = ratingBar.getRating();

                                            Double finalRating = (original_rating+userRating);

                                            // Update barber
                                            Map<String, Object> data_update = new HashMap<>();
                                            data_update.put("rating", finalRating);
                                            // ++ratingTimes, not (ratingTimes+1) or (ratingTimes++)
                                            // because we need INCREASE FIRST AND USE
                                            data_update.put("ratingTimes", ++ratingTimes);
                                            
                                            barberNeedRateRef.update(data_update)
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(context, "Thank you for rating!", Toast.LENGTH_SHORT).show();
                                                                // Remove Key
                                                                Paper.init(context);
                                                                Paper.book().delete(Common.RATING_INFORMATION_KEY);
                                                            }
                                                        }
                                                    });
                                        }
                                    })
                                    .setNegativeButton("SKIP", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // If select Skip, we just dismiss dialog
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNeutralButton("NEVER", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // If select Never,
                                            // That mean no rating, we will delete key
                                            Paper.init(context);
                                            Paper.book().delete(Common.RATING_INFORMATION_KEY);

                                        }
                                    });

                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                });


    }

    public static enum TOKEN_TYPE {
        CLIENT,
        BARBER,
        MANAGER
    }

    public static void updateToken(Context context, String token) {

        AccessToken accessToken = AccountKit.getCurrentAccessToken();
        if (accessToken != null) {
            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(Account account) {
                    MyToken myToken = new MyToken();
                    myToken.setToken(token);
                    // Because token come from client app
                    myToken.setTokenType(TOKEN_TYPE.CLIENT);
                    myToken.setUserPhone(account.getPhoneNumber().toString());

                    FirebaseFirestore.getInstance()
                            .collection("Tokens")
                            .document(account.getPhoneNumber().toString())
                            .set(myToken)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                }

                @Override
                public void onError(AccountKitError accountKitError) {

                }
            });
        }
        else {
            Paper.init(context);
            String user = Paper.book().read(Common.LOGGED_KEY);
            if (user != null) {
                if (!TextUtils.isEmpty(user)) {
                    MyToken myToken = new MyToken();
                    myToken.setToken(token);
                    // Because token come from client app
                    myToken.setTokenType(TOKEN_TYPE.CLIENT);
                    myToken.setUserPhone(user);

                    FirebaseFirestore.getInstance()
                            .collection("Tokens")
                            .document(user)
                            .set(myToken)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                }
            }
        }
    }
}

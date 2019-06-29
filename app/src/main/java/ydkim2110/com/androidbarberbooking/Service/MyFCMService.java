package ydkim2110.com.androidbarberbooking.Service;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.paperdb.Paper;
import ydkim2110.com.androidbarberbooking.Common.Common;

public class MyFCMService extends FirebaseMessagingService {

    private static final String TAG = MyFCMService.class.getSimpleName();
    
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Common.updateToken(this, token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // dataSend.put("update_done", "true");
        if (remoteMessage.getData() != null) {
            if (remoteMessage.getData().get("update_done") != null) {
                Log.d(TAG, "onMessageReceived: updateLastBooking() called!!");
                updateLastBooking();

                //  To Rating
                Map<String, String> dataReceived = remoteMessage.getData();
                Paper.init(this);
                Paper.book().write(Common.RATING_INFORMATION_KEY, new Gson().toJson(dataReceived));
            }

            if (remoteMessage.getData().get(Common.TITLE_KEY) != null &&
                    remoteMessage.getData().get(Common.CONTENT_KEY) != null) {
                Log.d(TAG, "onMessageReceived: TITLE_KEY && CONTENT_KEY not null called!!");
                Common.showNotification(this, new Random().nextInt(),
                        remoteMessage.getData().get(Common.TITLE_KEY),
                        remoteMessage.getData().get(Common.CONTENT_KEY),
                        null);
            }
        }

    }

    private void updateLastBooking() {
        Log.d(TAG, "updateLastBooking: called!!");
        // Here we need get current user login
        // Because app maybe run on background so we need get from paper

        CollectionReference userBooking;

        // If app running
        if (Common.currentUser != null) {
            Log.d(TAG, "updateLastBooking: Common.currentUser not null called!!");
            userBooking = FirebaseFirestore.getInstance()
                    .collection("User")
                    .document(Common.currentUser.getPhoneNumber())
                    .collection("Booking");
        }
        // If app not running
        else {
            Log.d(TAG, "updateLastBooking: app not running called!!");
            Paper.init(this);
            String user = Paper.book().read(Common.LOGGED_KEY);

            userBooking = FirebaseFirestore.getInstance()
                    .collection("User")
                    .document(user)
                    .collection("Booking");
        }

        // Check if exists by get current date
        // Why we only work for current date ? Because in my scenario, we only load
        // appointment for current date and next 3day
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.add(Calendar.HOUR_OF_DAY, 0);
        calendar.add(Calendar.MINUTE, 0);

        Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());

        userBooking
                // Get only booking info with time is today or next day
                .whereGreaterThanOrEqualTo("timestamp", timestamp)
                // Add done field is false (not done services)
                .whereEqualTo("done", false)
                .limit(1)
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: "+e.getMessage());
                        Toast.makeText(MyFCMService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() > 0) {
                                // Update
                                Log.d(TAG, "onComplete: find complete called!!");
                                DocumentReference userBookingCurrentDocument = null;
                                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                    userBookingCurrentDocument = userBooking.document(documentSnapshot.getId());
                                }
                                if (userBookingCurrentDocument != null) {
                                    Log.d(TAG, "onComplete: userBookingCurrentDocument not null called!!");
                                    Map<String, Object> dataUpdate = new HashMap<>();
                                    dataUpdate.put("done", true);
                                    userBookingCurrentDocument.update(dataUpdate)
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "onComplete: Failed called!!");
                                                    Toast.makeText(MyFCMService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Log.d(TAG, "onComplete: userBookingCurrentDocument update complete called!!");
                                                }
                                            });
                                }
                            }
                        }
                    }
                });

    }
}

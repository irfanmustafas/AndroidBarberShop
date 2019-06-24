package ydkim2110.com.androidbarberbooking.Service;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

import ydkim2110.com.androidbarberbooking.Common.Common;

public class MyFCMService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Common.updateToken(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Common.showNotification(this, new Random().nextInt(),
                remoteMessage.getData().get(Common.TITLE_KEY),
                remoteMessage.getData().get(Common.CONTENT_KEY),
                null);
    }
}

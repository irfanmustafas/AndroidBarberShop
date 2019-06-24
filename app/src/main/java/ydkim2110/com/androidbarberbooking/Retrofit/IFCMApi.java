package ydkim2110.com.androidbarberbooking.Retrofit;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import ydkim2110.com.androidbarberbooking.Model.FCMResponse;
import ydkim2110.com.androidbarberbooking.Model.FCMSendData;

public interface IFCMApi {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAApYtjxso:APA91bFS4bnZdPRg5BlYjynwf0_tvrQRf664gMIZU-2HCoV1T4VXu52T3eOBXAt2ImJJIBOemWrPO44-gJBnKnpRyT6dDRKN_pb9ZUsOc9ceEx6bUoigIMPZSAidnfZ9dOhaCW2py4vx"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}

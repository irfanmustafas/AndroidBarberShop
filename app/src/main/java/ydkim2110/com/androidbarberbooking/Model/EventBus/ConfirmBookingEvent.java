package ydkim2110.com.androidbarberbooking.Model.EventBus;


/**
* EventBus
* @author Kim Yong dae
* @version 1.0.0
* @since 2019-06-29 오전 8:34
**/
public class ConfirmBookingEvent {

    private boolean isConfirm;

    public ConfirmBookingEvent(boolean isConfirm) {
        this.isConfirm = isConfirm;
    }

    public boolean isConfirm() {
        return isConfirm;
    }

    public void setConfirm(boolean confirm) {
        isConfirm = confirm;
    }

}

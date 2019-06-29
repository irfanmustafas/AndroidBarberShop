package ydkim2110.com.androidbarberbooking.Model.EventBus;

/**
 * Event Bus
 * @author Kim Yong dae
 * @version 1.0.0
 * @since 2019-06-29 오전 8:39
**/
public class DisplayTimeSlotEvent {
    
    private boolean isDisplay;

    public DisplayTimeSlotEvent(boolean isDisplay) {
        this.isDisplay = isDisplay;
    }

    public boolean isDisplay() {
        return isDisplay;
    }

    public void setDisplay(boolean display) {
        isDisplay = display;
    }
}

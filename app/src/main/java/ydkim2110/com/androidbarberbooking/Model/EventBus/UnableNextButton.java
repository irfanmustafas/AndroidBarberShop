package ydkim2110.com.androidbarberbooking.Model.EventBus;

public class UnableNextButton {
    private boolean isUnable;

    public UnableNextButton(boolean isUnable) {
        this.isUnable = isUnable;
    }

    public boolean isUnable() {
        return isUnable;
    }

    public void setUnable(boolean unable) {
        isUnable = unable;
    }
}

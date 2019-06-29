package ydkim2110.com.androidbarberbooking.Model.EventBus;

import java.util.List;

import ydkim2110.com.androidbarberbooking.Model.BookingInformation;

public class UserBookingLoadEvent {
    private boolean success;
    private String message;
    private List<BookingInformation> bookingInformationList;

    public UserBookingLoadEvent(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public UserBookingLoadEvent(boolean success, List<BookingInformation> bookingInformationList) {
        this.success = success;
        this.bookingInformationList = bookingInformationList;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<BookingInformation> getBookingInformationList() {
        return bookingInformationList;
    }

    public void setBookingInformationList(List<BookingInformation> bookingInformationList) {
        this.bookingInformationList = bookingInformationList;
    }
}

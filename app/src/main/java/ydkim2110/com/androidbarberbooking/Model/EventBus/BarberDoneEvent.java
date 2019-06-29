package ydkim2110.com.androidbarberbooking.Model.EventBus;

import java.util.List;

import ydkim2110.com.androidbarberbooking.Model.Barber;

public class BarberDoneEvent {

    private List<Barber> mBarberList;

    public BarberDoneEvent(List<Barber> barberList) {
        mBarberList = barberList;
    }

    public List<Barber> getBarberList() {
        return mBarberList;
    }

    public void setBarberList(List<Barber> barberList) {
        mBarberList = barberList;
    }
}

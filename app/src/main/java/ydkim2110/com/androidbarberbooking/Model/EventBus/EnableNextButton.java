package ydkim2110.com.androidbarberbooking.Model.EventBus;

import ydkim2110.com.androidbarberbooking.Model.Barber;
import ydkim2110.com.androidbarberbooking.Model.Salon;

public class EnableNextButton {

    private int step;
    private Barber barber;
    private Salon salon;
    private int timeSlot;

    public EnableNextButton(int step, Barber barber) {
        this.step = step;
        this.barber = barber;
    }

    public EnableNextButton(int step, Salon salon) {
        this.step = step;
        this.salon = salon;
    }

    public EnableNextButton(int step, int timeSlot) {
        this.step = step;
        this.timeSlot = timeSlot;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public Barber getBarber() {
        return barber;
    }

    public void setBarber(Barber barber) {
        this.barber = barber;
    }

    public Salon getSalon() {
        return salon;
    }

    public void setSalon(Salon salon) {
        this.salon = salon;
    }

    public int getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(int timeSlot) {
        this.timeSlot = timeSlot;
    }
}

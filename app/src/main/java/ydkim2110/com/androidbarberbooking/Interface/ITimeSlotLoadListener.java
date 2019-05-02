package ydkim2110.com.androidbarberbooking.Interface;

import java.util.List;

import ydkim2110.com.androidbarberbooking.Model.TimeSlot;

public interface ITimeSlotLoadListener  {
    void onTimeSlotLoadSuccess(List<TimeSlot> timeSlotList);
    void onTimeSlotLoadFailed(String message);
    void onTimeSlotLoadEmpty();
}

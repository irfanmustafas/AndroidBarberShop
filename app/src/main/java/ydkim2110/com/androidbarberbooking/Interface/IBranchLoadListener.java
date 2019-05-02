package ydkim2110.com.androidbarberbooking.Interface;

import java.util.List;

import ydkim2110.com.androidbarberbooking.Model.Salon;

public interface IBranchLoadListener {
    void onAllBranchLoadSuccess(List<Salon> areaNameList);
    void onAllBranchLoadFailed(String message);
}

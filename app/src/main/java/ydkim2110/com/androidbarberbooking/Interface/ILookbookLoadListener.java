package ydkim2110.com.androidbarberbooking.Interface;

import java.util.List;

import ydkim2110.com.androidbarberbooking.Model.Banner;

public interface ILookbookLoadListener {
    void onLookbookLoadSuccess(List<Banner> banners);
    void onLookbookLoadFailed(String message);
}

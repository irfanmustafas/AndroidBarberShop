package ydkim2110.com.androidbarberbooking.Interface;

import java.util.List;

import ydkim2110.com.androidbarberbooking.Model.Banner;

public interface IBannerLoadListener {
    void onBannerLoadSuccess(List<Banner> banners);
    void onBannerLoadFailed(String message);
}

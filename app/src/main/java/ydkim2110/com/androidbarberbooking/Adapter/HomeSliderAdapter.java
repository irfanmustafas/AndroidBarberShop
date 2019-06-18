package ydkim2110.com.androidbarberbooking.Adapter;

import java.util.List;

import ss.com.bannerslider.adapters.SliderAdapter;
import ss.com.bannerslider.viewholder.ImageSlideViewHolder;
import ydkim2110.com.androidbarberbooking.Model.Banner;

public class HomeSliderAdapter extends SliderAdapter {

    private static final String TAG = HomeSliderAdapter.class.getSimpleName();

    List<Banner> mBannerList;

    public HomeSliderAdapter(List<Banner> bannerList) {
        this.mBannerList = bannerList;
    }

    @Override
    public int getItemCount() {
        return mBannerList.size();
    }

    @Override
    public void onBindImageSlide(int position, ImageSlideViewHolder imageSlideViewHolder) {
        imageSlideViewHolder.bindImageSlide(mBannerList.get(position).getImage());
    }
}

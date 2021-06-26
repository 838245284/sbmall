package cn.wu1588.beauty.views;

import android.graphics.Bitmap;

import cn.wu1588.beauty.ui.interfaces.DefaultBeautyEffectListener;


public interface MHProjectBeautyEffectListener extends DefaultBeautyEffectListener {
    public void onFilterChanged(Bitmap bitmap);
}

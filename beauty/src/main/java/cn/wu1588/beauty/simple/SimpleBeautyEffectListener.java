package cn.wu1588.beauty.simple;


import cn.wu1588.beauty.ui.interfaces.BeautyEffectListener;

/**
 * Created by cxf on 2018/10/8.
 * 基础美颜回调
 */

public interface SimpleBeautyEffectListener extends BeautyEffectListener {

    void onFilterChanged(SimpleFilterBean filterBean);

    void onMeiBaiChanged(int progress);

    void onMoPiChanged(int progress);

    void onHongRunChanged(int progress);

}

package cn.wu1588.beauty.ui.interfaces;

import cn.wu1588.beauty.ui.bean.FilterBean;

/**
 * Created by cxf on 2018/10/8.
 * 基础美颜回调
 */

public interface DefaultBeautyEffectListener extends BeautyEffectListener {

    void onFilterChanged(FilterBean filterEnumEnum);

    void onMeiBaiChanged(int progress);

    void onMoPiChanged(int progress);

    void onFengNenChanged(int progress);

    void onBeautyOrigin();

}

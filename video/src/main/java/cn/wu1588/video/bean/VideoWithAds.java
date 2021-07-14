package cn.wu1588.video.bean;

import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.qq.e.ads.nativ.NativeExpressADView;

public class VideoWithAds {
    public VideoBean videoBean;
    public TTNativeExpressAd ad;
    public NativeExpressADView qqAd;
    public int itemType;
    public static final int ITEM_TYPE_SHORT_VIDEO = 1;
    public static final int ITEM_TYPE_LONG_VIDEO = 2;
    public static final int ITEM_TYPE_Ads = 3;
    public static final int ITEM_TYPE_QQAD = 4;
}

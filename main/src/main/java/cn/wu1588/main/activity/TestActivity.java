package cn.wu1588.main.activity;

import android.view.View;
import android.widget.FrameLayout;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;

import java.util.List;

import cn.wu1588.common.activity.AbsActivity;
import cn.wu1588.common.utils.DensityUtils;
import cn.wu1588.main.R;

public class TestActivity extends AbsActivity {
    private static final String TAG = "TestActivity";
    private TTAdNative mTTAdNative;
    private FrameLayout root;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_test;
    }

    @Override
    protected void main() {
        root = findViewById(R.id.root);
        initAds();
        loadAds();
    }

    private void loadAds() {
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId("946272856") //广告位id
                .setSupportDeepLink(true)
                .setAdCount(1) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(DensityUtils.getScreenW(mContext),DensityUtils.getScreenH(mContext)) //期望模板广告view的size,单位dp
                .build();
        mTTAdNative.loadExpressDrawFeedAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            //广告加载失败
            @Override
            public void onError(int code, String message) {

            }
            //广告加载成功
            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                showAd(ads);
            }
        });
    }

    private void showAd(List<TTNativeExpressAd> ads) {
        final TTNativeExpressAd ad = ads.get(0);
        ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
            //广告点击的回调
            @Override
            public void onAdClicked(View view, int type) {

            }
            //广告展示回调
            @Override
            public void onAdShow(View view, int type) {

            }
            //广告渲染失败
            @Override
            public void onRenderFail(View view, String msg, int code) {

            }
            //广告渲染成功
            @Override
            public void onRenderSuccess(View view, float width, float height) {
                root.addView(ad.getExpressAdView());
            }
        });
        ad.render();
    }

    private void initAds() {

        mTTAdNative = TTAdSdk.getAdManager().createAdNative(mContext);
        //step3:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdSdk.getAdManager().requestPermissionIfNecessary(mContext);
    }


}

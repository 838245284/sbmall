package cn.wu1588.live.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.List;

import cn.wu1588.common.Constants;
import cn.wu1588.common.http.HttpCallback;
import cn.wu1588.common.utils.JsonUtil;
import cn.wu1588.live.R;
import cn.wu1588.live.bean.BackPackGiftBean;
import cn.wu1588.live.http.LiveHttpConsts;
import cn.wu1588.live.http.LiveHttpUtil;

/**
 * Created by cxf on 2019/8/28.
 * 礼物 背包
 */

public class LiveGiftPackageViewHolder extends AbsLiveGiftViewHolder {

    public LiveGiftPackageViewHolder(Context context, ViewGroup parentView, String liveUid, String stream) {
        super(context, parentView, liveUid, stream);
    }

    @Override
    protected void processArguments(Object... args) {
        mLiveUid = (String) args[0];
        mStream = (String) args[1];
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_gift_package;
    }

    @Override
    public void init() {
        super.init();
    }

    public void loadData() {
        if(mActionListener!=null){
            mActionListener.onCountChanged(mCount);
        }
        if (!isFirstLoadData()) {
            return;
        }
        LiveHttpUtil.getBackpack(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    List<BackPackGiftBean> list = JsonUtil.getJsonToList(Arrays.toString(info), BackPackGiftBean.class);
                    if (list != null) {
                        showGiftList(list);
                    }
                }
            }

            @Override
            public void onFinish() {
                if (mLoading != null) {
                    mLoading.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public int getGiftType() {
        return Constants.GIFT_TYPE_PACK;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LiveHttpUtil.cancel(LiveHttpConsts.GET_BACK_PACK);

    }


    public void reducePackageCount(int giftId, int count) {
        if (mLiveGiftPagerAdapter != null) {
            mLiveGiftPagerAdapter.reducePackageCount(giftId, count);
        }
    }
}

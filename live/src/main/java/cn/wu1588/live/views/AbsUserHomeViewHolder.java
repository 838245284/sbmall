package cn.wu1588.live.views;

import android.content.Context;
import android.view.ViewGroup;

import cn.wu1588.common.views.AbsViewHolder;

/**
 * Created by cxf on 2019/3/23.
 */

public abstract class AbsUserHomeViewHolder extends AbsViewHolder {

    protected boolean mFirstLoadData = true;

    public AbsUserHomeViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    public AbsUserHomeViewHolder(Context context, ViewGroup parentView, Object... args) {
        super(context, parentView, args);
    }


    public abstract void loadData();

    protected boolean isFirstLoadData() {
        if (mFirstLoadData) {
            mFirstLoadData = false;
            return true;
        }
        return false;
    }

}

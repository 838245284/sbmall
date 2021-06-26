package cn.wu1588.live.activity;

import android.text.TextUtils;
import android.view.ViewGroup;

import cn.wu1588.common.Constants;
import cn.wu1588.common.activity.AbsActivity;
import cn.wu1588.live.R;
import cn.wu1588.live.views.LiveContributeViewHolder;

/**
 * Created by cxf on 2018/10/19.
 */

public class LiveContributeActivity extends AbsActivity {

    private LiveContributeViewHolder mLiveContributeViewHolder;
    private String mLiveUid;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_empty;
    }

    @Override
    protected void main() {
        mLiveUid = getIntent().getStringExtra(Constants.TO_UID);
        if (TextUtils.isEmpty(mLiveUid)) {
            return;
        }
        mLiveContributeViewHolder = new LiveContributeViewHolder(mContext, (ViewGroup) findViewById(R.id.container));
        mLiveContributeViewHolder.addToParent();
        mLiveContributeViewHolder.loadData();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mLiveContributeViewHolder != null) {
            mLiveContributeViewHolder.release();
        }
    }

    public String getLiveUid() {
        return mLiveUid;
    }
}

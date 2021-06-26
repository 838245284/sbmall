package cn.wu1588.main.activity;

import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;

import cn.wu1588.common.Constants;
import cn.wu1588.common.activity.AbsActivity;
import cn.wu1588.main.R;
import cn.wu1588.main.views.MainListViewHolder;

/**
 * 排行榜
 */
public class RankActivity extends AbsActivity {

    public static void forward(Context context, int position) {
        Intent intent = new Intent(context, RankActivity.class);
        intent.putExtra(Constants.LIVE_POSITION, position);
        context.startActivity(intent);
    }

    @Override
    protected boolean isStatusBarWhite() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_rank;
    }

    @Override
    protected void main() {
        int position = getIntent().getIntExtra(Constants.LIVE_POSITION, 0);
        MainListViewHolder viewHolder = new MainListViewHolder(mContext, (ViewGroup) findViewById(R.id.container));
        viewHolder.addToParent();
        viewHolder.subscribeActivityLifeCycle();
        viewHolder.loadData(position);
    }
}

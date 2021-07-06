package cn.wu1588.video.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.wu1588.common.Constants;
import cn.wu1588.common.utils.L;
import cn.wu1588.common.utils.StatusBarUtil;
import cn.wu1588.video.R;
import cn.wu1588.video.bean.VideoBean;
import cn.wu1588.video.http.VideoHttpUtil;
import cn.wu1588.video.utils.VideoStorge;
import cn.wu1588.video.views.VideoScrollViewHolder;

/**
 * Created by cxf on 2018/11/26.
 */

public class VideoPlayActivity extends AbsVideoPlayActivity {

    public static void forward(Context context, int position, String videoKey, int page) {
        Intent intent = new Intent(context, VideoPlayActivity.class);
        intent.putExtra(Constants.VIDEO_POSITION, position);
        intent.putExtra(Constants.VIDEO_KEY, videoKey);
        intent.putExtra(Constants.VIDEO_PAGE, page);
        context.startActivity(intent);
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setStatusBarColor(this,R.color.black);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Color.parseColor("#000000"));
            //getWindow().setNavigationBarColor(getResources().getColor(R.color.black));
            //getWindow().setNavigationBarColor(Color.BLUE);
        }
    }

    public static void forwardSingle(Context context, VideoBean videoBean) {
        if (videoBean == null) {
            return;
        }
        List<VideoBean> list = new ArrayList<>();
        list.add(videoBean);
        VideoStorge.getInstance().put(Constants.VIDEO_SINGLE, list);
        Intent intent = new Intent(context, VideoPlayActivity.class);
        intent.putExtra(Constants.VIDEO_POSITION, 0);
        intent.putExtra(Constants.VIDEO_KEY, Constants.VIDEO_SINGLE);
        intent.putExtra(Constants.VIDEO_PAGE, 1);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_play;
    }

    @Override
    protected boolean isStatusBarWhite() {
        return true;
    }

    @Override
    protected void main() {
        super.main();
        Intent intent = getIntent();
        mVideoKey = intent.getStringExtra(Constants.VIDEO_KEY);
        if (TextUtils.isEmpty(mVideoKey)) {
            return;
        }
        int position = intent.getIntExtra(Constants.VIDEO_POSITION, 0);
        int page = intent.getIntExtra(Constants.VIDEO_PAGE, 1);
        mVideoScrollViewHolder = new VideoScrollViewHolder(mContext, (ViewGroup) findViewById(R.id.container), position, mVideoKey, page);
        mVideoScrollViewHolder.addToParent();
        mVideoScrollViewHolder.subscribeActivityLifeCycle();
        VideoHttpUtil.startWatchVideo();
    }

    @Override
    public void onBackPressed() {
        VideoHttpUtil.endWatchVideo();
        release();
        super.onBackPressed();
    }


    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
        L.e("VideoPlayActivity------->onDestroy");
    }


}

package cn.wu1588.video.views;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.VideoAllCallBack;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import cn.wu1588.common.CommonAppConfig;
import cn.wu1588.common.glide.ImgLoader;
import cn.wu1588.common.utils.L;
import cn.wu1588.common.utils.Logger;
import cn.wu1588.common.views.AbsViewHolder;

import cn.wu1588.video.bean.VideoBean;
import cn.wu1588.video.http.VideoHttpConsts;
import cn.wu1588.video.http.VideoHttpUtil;
import cn.wu1588.video.R;

/**
 * Created by cxf on 2018/11/30.
 * 视频播放器
 */

public class VideoPlayViewHolder extends AbsViewHolder implements View.OnClickListener {

    private SVideoPlayer mVideoView;

    ActionListener mActionListener;

    private View mPlayBtn;
    private ObjectAnimator mPlayBtnAnimator;//暂停按钮的动画
    private VideoBean mVideoBean;

    private boolean mPaused;//生命周期暂停

    private boolean mStartPlay;
    private boolean mClickPaused;
    private boolean mEndPlay;
    private ImageView mVideoCoverView;

    public VideoPlayViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_video_play;
    }

    @Override
    public void init() {
        mVideoView = findViewById(R.id.video_view);

        mVideoCoverView = new ImageView(mContext);
        mVideoCoverView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mVideoView.setThumbImageView(mVideoCoverView);
        //全屏裁减显示，为了显示正常 CoverImageView 建议使用FrameLayout作为父布局
        GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_FULL);
        // 重复播放
        mVideoView.setLooping(true);
//        mVideoView.setAutoFullWithSize(true);
        mVideoView.setVideoAllCallBack(new GSYSampleCallBack() {
            @Override
            public void onStartPrepared(String url, Object... objects) {
                Logger.d("开始加载");
                if (mActionListener != null) {
                    mActionListener.onPlayLoading();
                }
            }

            @Override
            public void onPrepared(String url, Object... objects) {
                Logger.d("加载成功");
                mStartPlay = true;
                if (mActionListener != null) {
                    mContentView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mActionListener.onFirstFrame();
                            mActionListener.onPlayBegin();
                        }
                    }, 100);
                }
            }

            @Override
            public void onAutoComplete(String url, Object... objects) {
                Logger.d("播放完了");
                Logger.d(objects);
                Log.d("GSYVIDEO", "onAutoComplete url=" + url);
                if (!mEndPlay) {
                    mEndPlay = true;
                    if (mVideoBean != null) {
                        VideoHttpUtil.videoWatchEnd(mVideoBean.getUid(), mVideoBean.getId());
                    }
                }
            }

            @Override
            public void onPlayError(String url, Object... objects) {
                Logger.d("播放失败 url=" + url);
            }
        });


        findViewById(R.id.root).setOnClickListener(this);
        mPlayBtn = findViewById(R.id.btn_play);
        //暂停按钮动画
        mPlayBtnAnimator = ObjectAnimator.ofPropertyValuesHolder(mPlayBtn,
                PropertyValuesHolder.ofFloat("scaleX", 4f, 0.8f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 4f, 0.8f, 1f),
                PropertyValuesHolder.ofFloat("alpha", 0f, 1f));
        mPlayBtnAnimator.setDuration(150);
        mPlayBtnAnimator.setInterpolator(new AccelerateInterpolator());
    }

    /**
     * 开始播放
     */
    public void startPlay(VideoBean videoBean) {
        mStartPlay = false;
        mClickPaused = false;
        mEndPlay = false;
        mVideoBean = videoBean;
//        ImgLoader.display(mContext, mVideoBean.getThumb(), videoCoverView);
        hidePlayBtn();
        if (videoBean == null) {
            return;
        }
        ImgLoader.display(mContext, mVideoBean.getThumb(), mVideoCoverView);

        L.e("播放视频--->" + videoBean);
        Logger.d(videoBean);
        String url = videoBean.getHref();
        if (TextUtils.isEmpty(url)) {
            return;
        }
        mVideoView.setUp(url, true, "");
        mVideoView.startPlayLogic();
        VideoHttpUtil.videoWatchStart(videoBean.getUid(), videoBean.getId());

    }

    /**
     * 停止播放
     */
    public void stopPlay() {
        Logger.d();
        if (mVideoView != null) {
            mVideoView.onVideoPause();
        }
    }

    /**
     * 循环播放
     */
    private void replay() {
        mVideoView.onVideoReset();
    }

    public void release() {
        Logger.d();
        VideoHttpUtil.cancel(VideoHttpConsts.VIDEO_WATCH_START);
        VideoHttpUtil.cancel(VideoHttpConsts.VIDEO_WATCH_END);
        if (mVideoView != null) {
            mVideoView.release();
        }

    }

    /**
     * 生命周期暂停
     */
    public void pausePlay() {
        if (mVideoView != null && !mClickPaused && !mPaused && mVideoView.isInPlayingState()
                && mStartPlay) {

            mVideoView.onVideoPause();
        }
        mPaused = true;
        Logger.d("生命周期暂停 mClickPaused=" + mClickPaused + ",mPaused=" + mPaused);
//        if (!mClickPaused && mVideoView != null)
//            mVideoView.onVideoPause();
    }

    /**
     * 生命周期恢复
     */
    public void resumePlay() {
        Logger.d("生命周期恢复 mClickPaused=" + mClickPaused + ",mPaused=" + mPaused);

        if (mPaused) {
            if (!mClickPaused && mVideoView != null
            && mStartPlay)
                mVideoView.onVideoResume(false);
        }
        mPaused = false;
    }

    /**
     * 显示开始播放按钮
     */
    private void showPlayBtn() {
        if (mPlayBtn != null && mPlayBtn.getVisibility() != View.VISIBLE) {
            mPlayBtn.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏开始播放按钮
     */
    private void hidePlayBtn() {
        if (mPlayBtn != null && mPlayBtn.getVisibility() == View.VISIBLE) {
            mPlayBtn.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.root) {
            clickTogglePlay();
        }
    }

    /**
     * 点击切换播放和暂停
     */
    private void clickTogglePlay() {
        if (!mStartPlay) {
            return;
        }
        if (mVideoView != null) {
            if (mClickPaused) {
                mVideoView.onVideoResume(false);
            } else {
                mVideoView.onVideoPause();
            }
        }
        mClickPaused = !mClickPaused;
        if (mClickPaused) {
            showPlayBtn();
            if (mPlayBtnAnimator != null) {
                mPlayBtnAnimator.start();
            }
        } else {
            hidePlayBtn();
        }
    }

    public interface ActionListener {
        void onPlayBegin();

        void onPlayLoading();

        void onFirstFrame();
    }


    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }


}

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
import android.widget.RelativeLayout;

import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.VideoAllCallBack;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import cn.wu1588.common.CommonAppConfig;
import cn.wu1588.common.utils.L;
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

    private View mVideoCover;
    private View mPlayBtn;
    private ObjectAnimator mPlayBtnAnimator;//暂停按钮的动画
    private VideoBean mVideoBean;

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

        // 重复播放
        mVideoView.setLooping(true);

        mVideoView.setVideoAllCallBack(new GSYSampleCallBack() {
            @Override
            public void onPrepared(String url, Object... objects) {
                Log.d("GSYVIDEO", "onPrepared url=" + url);
                if (mActionListener != null) {
                    mActionListener.onFirstFrame();
                    mActionListener.onPlayLoading();
                }
            }

            @Override
            public void onAutoComplete(String url, Object... objects) {
                Log.d("GSYVIDEO", "onAutoComplete url=" + url);
            }
        });

//        findViewById(R.id.root).setOnClickListener(this);
        mVideoCover = findViewById(R.id.video_cover);
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
        mVideoBean = videoBean;
        if (mVideoCover != null && mVideoCover.getVisibility() != View.VISIBLE) {
            mVideoCover.setVisibility(View.VISIBLE);
        }
        hidePlayBtn();
        if (videoBean == null) {
            return;
        }
        L.e("播放视频--->" + videoBean);
        String url = videoBean.getHref();
        if (TextUtils.isEmpty(url)) {
            return;
        }

        mVideoView.setUp(videoBean.getHref(), true, "");
        mVideoView.startPlayLogic();
        VideoHttpUtil.videoWatchStart(videoBean.getUid(), videoBean.getId());
    }

    /**
     * 停止播放
     */
    public void stopPlay() {
        if (mVideoView != null) {
            mVideoView.release();
        }
    }

    /**
     * 循环播放
     */
    private void replay() {
        mVideoView.startPlayLogic();
    }

    public void release() {
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
        if (mVideoView != null)
            mVideoView.onVideoPause();
    }

    /**
     * 生命周期恢复
     */
    public void resumePlay() {

        if (mVideoView != null)
            mVideoView.onVideoResume();
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

    }


    public interface ActionListener {
        void onPlayBegin();

        void onPlayLoading();

        void onFirstFrame();
    }


    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }


    VideoAllCallBack mVideoAllCallBack = new VideoAllCallBack() {
        @Override
        public void onStartPrepared(String url, Object... objects) {
            Log.d("GSYVIDEO", "onStartPrepared url=" + url);
        }

        @Override
        public void onPrepared(String url, Object... objects) {
            Log.d("GSYVIDEO", "onPrepared url=" + url);
            if (mActionListener != null) {
                mActionListener.onFirstFrame();
                mActionListener.onPlayLoading();
            }
        }

        @Override
        public void onClickStartIcon(String url, Object... objects) {

        }

        @Override
        public void onClickStartError(String url, Object... objects) {

        }

        @Override
        public void onClickStop(String url, Object... objects) {

        }

        @Override
        public void onClickStopFullscreen(String url, Object... objects) {

        }

        @Override
        public void onClickResume(String url, Object... objects) {

        }

        @Override
        public void onClickResumeFullscreen(String url, Object... objects) {

        }

        @Override
        public void onClickSeekbar(String url, Object... objects) {

        }

        @Override
        public void onClickSeekbarFullscreen(String url, Object... objects) {

        }

        @Override
        public void onAutoComplete(String url, Object... objects) {
            Log.d("GSYVIDEO", "onAutoComplete url=" + url);
            if (mActionListener != null) {
                mActionListener.onPlayBegin();
            }

        }

        @Override
        public void onEnterFullscreen(String url, Object... objects) {

        }

        @Override
        public void onQuitFullscreen(String url, Object... objects) {

        }

        @Override
        public void onQuitSmallWidget(String url, Object... objects) {

        }

        @Override
        public void onEnterSmallWidget(String url, Object... objects) {

        }

        @Override
        public void onTouchScreenSeekVolume(String url, Object... objects) {

        }

        @Override
        public void onTouchScreenSeekPosition(String url, Object... objects) {

        }

        @Override
        public void onTouchScreenSeekLight(String url, Object... objects) {

        }

        @Override
        public void onPlayError(String url, Object... objects) {

        }

        @Override
        public void onClickStartThumb(String url, Object... objects) {

        }

        @Override
        public void onClickBlank(String url, Object... objects) {

        }

        @Override
        public void onClickBlankFullscreen(String url, Object... objects) {

        }
    };

}

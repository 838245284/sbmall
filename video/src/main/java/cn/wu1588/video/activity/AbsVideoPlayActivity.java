package cn.wu1588.video.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.view.WindowManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shuyu.gsyvideoplayer.GSYVideoManager;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import cn.wu1588.common.CommonAppConfig;
import cn.wu1588.common.HtmlConfig;
import cn.wu1588.common.bean.ConfigBean;
import cn.wu1588.common.bean.UserBean;
import cn.wu1588.common.http.HttpCallback;
import cn.wu1588.common.interfaces.CommonCallback;
import cn.wu1588.common.mob.MobCallback;
import cn.wu1588.common.mob.MobShareUtil;
import cn.wu1588.common.mob.ShareData;
import cn.wu1588.common.utils.DateFormatUtil;
import cn.wu1588.common.utils.DialogUitl;
import cn.wu1588.common.utils.DownloadUtil;
import cn.wu1588.common.utils.StringUtil;
import cn.wu1588.common.utils.ToastUtil;
import cn.wu1588.video.R;
import cn.wu1588.video.bean.VideoBean;
import cn.wu1588.video.event.VideoDeleteEvent;
import cn.wu1588.video.event.VideoShareEvent;
import cn.wu1588.video.http.VideoHttpConsts;
import cn.wu1588.video.http.VideoHttpUtil;
import cn.wu1588.video.utils.VideoLocalUtil;
import cn.wu1588.video.utils.VideoStorge;
import cn.wu1588.video.views.VideoScrollViewHolder;

/**
 * Created by cxf on 2019/2/28.
 */

public abstract class AbsVideoPlayActivity extends AbsVideoCommentActivity {

    protected VideoScrollViewHolder mVideoScrollViewHolder;
    private Dialog mDownloadVideoDialog;
    private ClipboardManager mClipboardManager;
    private MobCallback mMobCallback;
    private MobShareUtil mMobShareUtil;
    private DownloadUtil mDownloadUtil;
    private ConfigBean mConfigBean;
    private VideoBean mShareVideoBean;
    protected String mVideoKey;
    private boolean mPaused;


    @Override
    protected void main() {
        super.main();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        CommonAppConfig.getInstance().getConfig(new CommonCallback<ConfigBean>() {
            @Override
            public void callback(ConfigBean bean) {
                mConfigBean = bean;
            }
        });
    }


    /**
     * 复制视频链接
     */
    public void copyLink(VideoBean videoBean) {
        if (videoBean == null) {
            return;
        }
        if (mClipboardManager == null) {
            mClipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        }
        ClipData clipData = ClipData.newPlainText("text", videoBean.getHrefW());
        mClipboardManager.setPrimaryClip(clipData);
        ToastUtil.show(R.string.copy_success);
    }

    /**
     * 分享页面链接
     */
    public void shareVideoPage(String type, VideoBean videoBean) {
        if (videoBean == null || mConfigBean == null) {
            return;
        }
        if (mMobCallback == null) {
            mMobCallback = new MobCallback() {

                @Override
                public void onSuccess(Object data) {
                    if (mShareVideoBean == null) {
                        return;
                    }
                    VideoHttpUtil.setVideoShare(mShareVideoBean.getId(), new HttpCallback() {
                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            if (code == 0 && info.length > 0 && mShareVideoBean != null) {
                                JSONObject obj = JSON.parseObject(info[0]);
                                EventBus.getDefault().post(new VideoShareEvent(mShareVideoBean.getId(), obj.getString("shares")));
                            } else {
                                ToastUtil.show(msg);
                            }
                        }
                    });
                }

                @Override
                public void onError() {

                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onFinish() {

                }
            };
        }
        mShareVideoBean = videoBean;
        ShareData data = new ShareData();
        String shareVideoTitle = mConfigBean.getVideoShareTitle();
        if (!TextUtils.isEmpty(shareVideoTitle) && shareVideoTitle.contains("{username}")) {
            UserBean userBean = videoBean.getUserBean();
            if (userBean != null) {
                shareVideoTitle = shareVideoTitle.replace("{username}", userBean.getUserNiceName());
            }
        }
        data.setTitle(shareVideoTitle);
        String videoTitle = videoBean.getTitle();
        if (TextUtils.isEmpty(videoTitle)) {
            data.setDes(mConfigBean.getVideoShareDes());
        } else {
            data.setDes(videoTitle);
        }
        data.setImgUrl(videoBean.getThumbs());
        String webUrl = StringUtil.contact(HtmlConfig.SHARE_VIDEO, videoBean.getId());
        data.setWebUrl(webUrl);
        if (mMobShareUtil == null) {
            mMobShareUtil = new MobShareUtil();
        }
        mMobShareUtil.execute(type, data, mMobCallback);
    }


    /**
     * 下载视频
     */
    public void downloadVideo(final VideoBean videoBean) {
        if (mProcessResultUtil == null || videoBean == null || TextUtils.isEmpty(videoBean.getHrefW())) {
            return;
        }
        mProcessResultUtil.requestPermissions(new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        }, new Runnable() {
            @Override
            public void run() {
                mDownloadVideoDialog = DialogUitl.loadingDialog(mContext);
                mDownloadVideoDialog.show();
                if (mDownloadUtil == null) {
                    mDownloadUtil = new DownloadUtil();
                }
                String fileName = "YB_VIDEO_" + videoBean.getTitle() + "_" + DateFormatUtil.getCurTimeString() + ".mp4";
                mDownloadUtil.download(videoBean.getTag(), CommonAppConfig.VIDEO_PATH, fileName, videoBean.getHrefW(), new DownloadUtil.Callback() {
                    @Override
                    public void onSuccess(File file) {
                        ToastUtil.show(R.string.video_download_success);
                        if (mDownloadVideoDialog != null && mDownloadVideoDialog.isShowing()) {
                            mDownloadVideoDialog.dismiss();
                        }
                        mDownloadVideoDialog = null;
                        String path = file.getAbsolutePath();
                        try {
                            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                            mmr.setDataSource(path);
                            String d = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            if (StringUtil.isInt(d)) {
                                long duration = Long.parseLong(d);
                                VideoLocalUtil.saveVideoInfo(mContext, path, duration);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onProgress(int progress) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtil.show(R.string.video_download_failed);
                        if (mDownloadVideoDialog != null && mDownloadVideoDialog.isShowing()) {
                            mDownloadVideoDialog.dismiss();
                        }
                        mDownloadVideoDialog = null;
                    }
                });
            }
        });
    }

    /**
     * 删除视频
     */
    public void deleteVideo(final VideoBean videoBean) {
        if (mVideoScrollViewHolder != null) {
            mVideoScrollViewHolder.deleteVideo(videoBean);
            EventBus.getDefault().post(new VideoDeleteEvent(videoBean.getId()));
        }
//        DialogUitl.showSimpleDialog(mContext, WordUtil.getString(R.string.confirm_delete), new DialogUitl.SimpleCallback() {
//            @Override
//            public void onConfirmClick(Dialog dialog, String content) {
//                VideoHttpUtil.videoDelete(videoBean.getId(), new HttpCallback() {
//                    @Override
//                    public void onSuccess(int code, String msg, String[] info) {
//                        if (code == 0) {
//                            if (mVideoScrollViewHolder != null) {
//                                mVideoScrollViewHolder.deleteVideo(videoBean);
//                                EventBus.getDefault().post(new VideoDeleteEvent(videoBean.getId()));
//                            }
//                        }
//                        ToastUtil.show(msg);
//                    }
//                });
//            }
//        });
    }


    public boolean isPaused() {
        return mPaused;
    }

    @Override
    protected void onPause() {
        mPaused = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPaused = false;
    }

    @Override
    public void release() {
        super.release();
        VideoHttpUtil.cancel(VideoHttpConsts.SET_VIDEO_SHARE);
        VideoHttpUtil.cancel(VideoHttpConsts.VIDEO_DELETE);
        if (mDownloadVideoDialog != null && mDownloadVideoDialog.isShowing()) {
            mDownloadVideoDialog.dismiss();
        }
        if (mVideoScrollViewHolder != null) {
            mVideoScrollViewHolder.release();
        }
        if (mMobShareUtil != null) {
            mMobShareUtil.release();
        }
        VideoStorge.getInstance().removeDataHelper(mVideoKey);
        mDownloadVideoDialog = null;
        mVideoScrollViewHolder = null;
        mMobShareUtil = null;

    }


    public void setVideoScrollViewHolder(VideoScrollViewHolder videoScrollViewHolder) {
        mVideoScrollViewHolder = videoScrollViewHolder;
    }
}

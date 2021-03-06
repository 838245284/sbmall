package cn.wu1588.video.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ugc.TXVideoEditer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import cn.wu1588.common.CommonAppConfig;
import cn.wu1588.common.Constants;
import cn.wu1588.common.HtmlConfig;
import cn.wu1588.common.activity.AbsActivity;
import cn.wu1588.common.bean.ConfigBean;
import cn.wu1588.common.http.CommonHttpConsts;
import cn.wu1588.common.http.CommonHttpUtil;
import cn.wu1588.common.http.HttpCallback;
import cn.wu1588.common.interfaces.CommonCallback;
import cn.wu1588.common.mob.CoverBean;
import cn.wu1588.common.mob.MobShareUtil;
import cn.wu1588.common.mob.ShareData;
import cn.wu1588.common.utils.DensityUtils;
import cn.wu1588.common.utils.DialogUitl;
import cn.wu1588.common.utils.RouteUtil;
import cn.wu1588.common.utils.ToastUtil;
import cn.wu1588.common.utils.WordUtil;
import cn.wu1588.video.R;
import cn.wu1588.video.adapter.VideoCoverAdapter;
import cn.wu1588.video.adapter.VideoPubShareAdapter;
import cn.wu1588.video.http.VideoHttpConsts;
import cn.wu1588.video.http.VideoHttpUtil;
import cn.wu1588.video.upload.VideoUploadBean;
import cn.wu1588.video.upload.VideoUploadCallback;
import cn.wu1588.video.upload.VideoUploadQnImpl;
import cn.wu1588.video.upload.VideoUploadStrategy;
import cn.wu1588.video.upload.VideoUploadTxImpl;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;
import top.zibin.luban.OnRenameListener;

/**
 * Created by cxf on 2018/12/10.
 * ????????????
 */

public class VideoPublishActivity extends AbsActivity implements ITXVodPlayListener, View.OnClickListener {
    private static final int MAX_DURATION = 15000;//??????????????????15s
    private static final int REQUEST_CODE_VIDEO = 123;//????????????

    private float originalVideoWidth;
    private float originalVideoHeight;
    private float coverItemWidth;
    private float coverItemHeight;
    private long originalVideoDuration;
    private TXVideoEditer mTxVideoEditer;
    private boolean originalVideoHorizontal;
    private VideoCoverAdapter mVideoCoverAdapter;
    private RecyclerView mRecyclerCover;

    public static void forward(Context context, String videoPath, String videoWaterPath, int saveType, int musicId) {
        Intent intent = new Intent(context, VideoPublishActivity.class);
        intent.putExtra(Constants.VIDEO_PATH, videoPath);
        intent.putExtra(Constants.VIDEO_PATH_WATER, videoWaterPath);
        intent.putExtra(Constants.VIDEO_SAVE_TYPE, saveType);
        intent.putExtra(Constants.VIDEO_MUSIC_ID, musicId);
        context.startActivity(intent);
    }

    private static final String TAG = "VideoPublishActivity";
    private static final String TAG_NEW = "????????????Test";
    private static final int REQ_CODE_GOODS = 100;
    private static final int REQ_CODE_CLASS = 101;
    private TextView mNum;
    private TextView mLocation;
    private TXCloudVideoView mTXCloudVideoView;
    private TXVodPlayer mPlayer;
    private String mVideoPath;
    private String mVideoPathWater;
    private boolean mPlayStarted;//?????????????????????
    private boolean mPaused;//??????????????????
    private RecyclerView mRecyclerView;
    private ConfigBean mConfigBean;
    private VideoPubShareAdapter mAdapter;
    private VideoUploadStrategy mUploadStrategy;
    private EditText mInput;
    private String mVideoTitle;//????????????
    private Dialog mLoading;
    private MobShareUtil mMobShareUtil;
    private int mSaveType;
    private int mMusicId;
    private View mBtnPub;
    private CheckBox mCheckBox;
    private CheckBox mCheckBoxOriginal;
    private CheckBox mCheckBoxTeachingl;
    private TextView mGoodsName;
    private View mBtnGoodsAdd;
    private TextView mVideoClassName;
    private int mVideoClassId;
    private int mGoodsType;//type  ????????????????????? 0 ????????? 1 ?????? 2 ????????????
    private String mGoodsId;//???????????????Id
    private double mVideoRatio;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_publish;
    }

    @Override
    protected void main() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTitle(WordUtil.getString(R.string.video_pub));
        Intent intent = getIntent();
        mVideoPath = intent.getStringExtra(Constants.VIDEO_PATH);
        mVideoPathWater = intent.getStringExtra(Constants.VIDEO_PATH_WATER);
        mSaveType = intent.getIntExtra(Constants.VIDEO_SAVE_TYPE, Constants.VIDEO_SAVE_SAVE_AND_PUB);
        if (TextUtils.isEmpty(mVideoPath)) {
            return;
        }
        mMusicId = intent.getIntExtra(Constants.VIDEO_MUSIC_ID, 0);
        Log.d(TAG_NEW, "mVideoPath=" + mVideoPath + ",mVideoPathWater=" + mVideoPathWater + ",mSaveType=" + mSaveType + ",mMusicId=" + mMusicId);
        mBtnPub = findViewById(R.id.btn_pub);
        mBtnPub.setOnClickListener(this);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        CommonAppConfig.getInstance().getConfig(new CommonCallback<ConfigBean>() {
            @Override
            public void callback(ConfigBean bean) {
                mConfigBean = bean;
                if (mRecyclerView != null) {
                    mAdapter = new VideoPubShareAdapter(mContext, bean);
                    mRecyclerView.setAdapter(mAdapter);
                }
            }
        });
        mNum = (TextView) findViewById(R.id.num);
        mInput = (EditText) findViewById(R.id.input);
        mInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mNum != null) {
                    mNum.setText(s.length() + "/50");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mLocation = findViewById(R.id.location);
        mLocation.setText(CommonAppConfig.getInstance().getCity());
        mCheckBox = findViewById(R.id.checkbox);
        mCheckBox.setOnClickListener(this);
        mCheckBoxOriginal = findViewById(R.id.checkbox_original);
        mCheckBoxOriginal.setOnClickListener(this);
        mCheckBoxTeachingl = findViewById(R.id.checkbox_teachingl);
        mCheckBoxTeachingl.setOnClickListener(this);

        mBtnGoodsAdd = findViewById(R.id.btn_goods_add);
        mVideoClassName = findViewById(R.id.video_class_name);
        mGoodsName = findViewById(R.id.goods_name);
        findViewById(R.id.btn_video_class).setOnClickListener(this);

        mTXCloudVideoView = findViewById(R.id.video_view);
        mPlayer = new TXVodPlayer(mContext);
        mPlayer.setConfig(new TXVodPlayConfig());
        mPlayer.setPlayerView(mTXCloudVideoView);
        mPlayer.enableHardwareDecode(false);
        mPlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
        mPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
        mPlayer.setVodListener(this);
        mPlayer.setLoop(true);
        int result = mPlayer.startPlay(mVideoPath);
        if (result == 0) {
            mPlayStarted = true;
        }

        mRecyclerCover = findViewById(R.id.recyclerViewCover);
        mRecyclerCover.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mVideoCoverAdapter = new VideoCoverAdapter(mContext);
        mRecyclerCover.setAdapter(mVideoCoverAdapter);

        VideoHttpUtil.getConcatGoods(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    int isShop = JSON.parseObject(info[0]).getIntValue("isshop");
                    if (isShop == 1) {
                        if (mBtnGoodsAdd != null) {
                            mBtnGoodsAdd.setVisibility(View.VISIBLE);
                            mBtnGoodsAdd.setOnClickListener(VideoPublishActivity.this);
                        }
                    }
                }
            }
        });

        findViewById(R.id.btn_reset_video).setOnClickListener(this);
        findViewById(R.id.btn_upload_cover).setOnClickListener(this);
        findViewById(R.id.btn_capture_cover).setOnClickListener(this);
        initVideoParameter();
        setVideoSize(originalVideoWidth, originalVideoHeight);
        getVideoThumbnailList();
    }


    private void initVideoParameter() {
        mTxVideoEditer = new TXVideoEditer(mContext);
        mTxVideoEditer.setVideoPath(mVideoPath);

        //?????????????????????
        MediaMetadataRetriever mmr = null;
        mmr = new MediaMetadataRetriever();
        mmr.setDataSource(mVideoPath);
        String width = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);//???
        String height = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);//???
        String rotation = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        //???????????????
        originalVideoDuration = Long.valueOf(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

        originalVideoWidth = Float.valueOf(width);
        originalVideoHeight = Float.valueOf(height);
        originalVideoHorizontal = originalVideoWidth > originalVideoHeight;
    }

    private void getVideoThumbnailList() {

        int count = originalVideoHorizontal ? 6 : 4;
//        ?????? @param fast ???????????????????????????
//        ???????????????????????????????????????????????????????????????????????????????????????????????? true???
//        ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????? false???
        /**
         * ?????????????????????
         * @param count    ???????????????
         * @param width    ???????????????
         * @param height   ???????????????
         * @param fast       ?????????????????????????????????
         * @param listener ????????????????????????

         */
        ArrayList<Long> list = new ArrayList<>();
        long time = 0L;
        long timeItem = originalVideoDuration / count;
        for (int i = 0; i < count; i++) {
            list.add(time);
            time = time + timeItem;
        }
//        list.add(originalVideoDuration);
        mTxVideoEditer.getThumbnail(list, (int) coverItemWidth, (int) coverItemHeight, false, mThumbnailListener);
//        mTxVideoEditer.getThumbnail(list, (int) originalVideoHeight, (int) originalVideoWidth, false, mThumbnailListener);

    }

    TXVideoEditer.TXThumbnailListener mThumbnailListener = new TXVideoEditer.TXThumbnailListener() {
        @Override
        public void onThumbnail(int index, long timeMs, final Bitmap bitmap) {
            //?????????????????????????????????
            addVideoCover(index, bitmap);
        }
    };

    private void addVideoCover(int index, Bitmap bitmap) {
        CoverBean bean = new CoverBean();
        bean.setWidth((int) coverItemWidth);
        bean.setWidth((int) coverItemHeight);
//        bean.setBitmap(Bitmap.createScaledBitmap(bitmap, bean.getWidth(), bean.getHight(), true));
//        bean.setBitmap(setBitmapSize(bitmap, bean.getWidth(), bean.getHight()));
        bean.setBitmap(bitmap);
        if (index == 0 && !mVideoCoverAdapter.getList().isEmpty()) {
            for (CoverBean coverBean : mVideoCoverAdapter.getList()) {
                coverBean.setChecked(false);
            }
            mVideoCoverAdapter.getList().add(0, bean);
        } else {
            mVideoCoverAdapter.getList().add(bean);
        }
        bean.setChecked(index == 0);
        mRecyclerCover.post(new Runnable() {
            @Override
            public void run() {
                mVideoCoverAdapter.notifyDataSetChanged();
            }
        });
    }


    private Bitmap setBitmapSize(Bitmap bitmap, int newWidth, int newHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        //?????????????????????
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        //?????????????????????matrix
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        //????????????bitmap
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return bitmap;
    }

    /**
     * ??????????????????
     *
     * @param videoWidth
     * @param videoHeight
     */
    private void setVideoSize(float videoWidth, float videoHeight) {
        if (mTXCloudVideoView == null) return;
        int maxWidth = DensityUtils.getScreenW(mContext) - DensityUtils.dip2px(mContext, 30);
        int maxHight = DensityUtils.dip2px(mContext, 200);
        float ratio = videoWidth / videoHeight;


        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mTXCloudVideoView.getLayoutParams();
        if (originalVideoHorizontal) {
            // ??????
            params.width = FrameLayout.LayoutParams.MATCH_PARENT;
            params.height = (int) (maxWidth / ratio);
            coverItemWidth = maxWidth / 4;
            coverItemHeight = params.height / 4;
        } else {
            // ??????
            params.width = (int) (maxHight * ratio);
            params.height = maxHight;

            coverItemWidth = params.width / 2;
            coverItemHeight = params.height / 2;
        }

        ViewGroup.LayoutParams layoutParams = mRecyclerCover.getLayoutParams();
        layoutParams.height = (int) coverItemHeight;
        mRecyclerCover.setLayoutParams(layoutParams);

        mTXCloudVideoView.requestLayout();
    }

    @Override
    public void onPlayEvent(TXVodPlayer txVodPlayer, int e, Bundle bundle) {
        switch (e) {
            case TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION:
//                onVideoSizeChanged(bundle.getInt("EVT_PARAM1", 0), bundle.getInt("EVT_PARAM2", 0));
                break;
        }
    }

    @Override
    public void onNetStatus(TXVodPlayer txVodPlayer, Bundle bundle) {

    }


    /**
     * ???????????????????????????
     */
    public void onVideoSizeChanged(float videoWidth, float videoHeight) {
        if (mTXCloudVideoView != null && videoWidth > 0 && videoHeight > 0) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mTXCloudVideoView.getLayoutParams();
            if (videoWidth > videoHeight) {
                // ??????

            } else {
                // ??????

            }
            if (videoWidth / videoHeight > 0.5625f) {//?????? 9:16=0.5625
                params.height = (int) (mTXCloudVideoView.getWidth() / videoWidth * videoHeight);
                params.gravity = Gravity.CENTER;
                mTXCloudVideoView.requestLayout();
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        mPaused = true;
        if (mPlayStarted && mPlayer != null) {
            mPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPaused && mPlayStarted && mPlayer != null) {
            mPlayer.resume();
        }
        mPaused = false;
    }

    public void release() {
        CommonHttpUtil.cancel(CommonHttpConsts.GET_CONFIG);
        VideoHttpUtil.cancel(VideoHttpConsts.GET_CONCAT_GOODS);
        VideoHttpUtil.cancel(VideoHttpConsts.SAVE_UPLOAD_VIDEO_INFO);
        mPlayStarted = false;
        if (mVideoCoverAdapter != null && mVideoCoverAdapter.getList() != null) {
            for (CoverBean coverBean : mVideoCoverAdapter.getList()) {
                /*if (coverBean.getBitmap() != null) {
                    coverBean.getBitmap().recycle();
                }*/
            }
        }
        if (mPlayer != null) {
            mPlayer.stopPlay(false);
            mPlayer.setPlayListener(null);
        }
        if (mUploadStrategy != null) {
            mUploadStrategy.cancel();
        }
        if (mMobShareUtil != null) {
            mMobShareUtil.release();
        }
        if (mTxVideoEditer != null) {
            mTxVideoEditer.release();
        }
        mPlayer = null;
        mUploadStrategy = null;
        mMobShareUtil = null;
    }

    @Override
    public void onBackPressed() {
        DialogUitl.showSimpleDialog(mContext, WordUtil.getString(R.string.video_give_up_pub), new DialogUitl.SimpleCallback() {
            @Override
            public void onConfirmClick(Dialog dialog, String content) {
                if (mSaveType == Constants.VIDEO_SAVE_PUB) {
                    if (!TextUtils.isEmpty(mVideoPath)) {
                        File file = new File(mVideoPath);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                    if (!TextUtils.isEmpty(mVideoPathWater)) {
                        File file = new File(mVideoPathWater);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }
                release();
                VideoPublishActivity.super.onBackPressed();
            }
        });
    }

    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_pub) {
            publishVideo();
        } else if (i == R.id.checkbox) {
            clickCheckBox();
        } else if (i == R.id.checkbox_original) {
        } else if (i == R.id.checkbox_teachingl) {
        } else if (i == R.id.btn_goods_add) {
            RouteUtil.searchMallGoods(this, REQ_CODE_GOODS);
        } else if (i == R.id.btn_video_class) {
            chooseVideoClass();
        } else if (i == R.id.btn_reset_video) {
            Intent intent = new Intent(mContext, VideoChooseActivity.class);
            intent.putExtra(Constants.VIDEO_DURATION, MAX_DURATION);
            startActivityForResult(intent, REQUEST_CODE_VIDEO);
        } else if (i == R.id.btn_upload_cover) {
            ToastUtil.show(R.string.video_upload_success);


        } else if (i == R.id.btn_capture_cover) {
            mPlayer.snapshot(new TXLivePlayer.ITXSnapshotListener() {
                @Override
                public void onSnapshot(Bitmap bitmap) {
                    addVideoCover(0, bitmap);
                }
            });
        }
    }


    private void chooseVideoClass() {
        Intent intent = new Intent(mContext, VideoChooseClassActivity.class);
        intent.putExtra(Constants.VIDEO_ID, mVideoClassId);
        startActivityForResult(intent, REQ_CODE_CLASS);
    }


    private void clickCheckBox() {
        if (mCheckBox == null || mLocation == null) {
            return;
        }
        if (mCheckBox.isChecked()) {
            mLocation.setEnabled(true);
            mLocation.setText(CommonAppConfig.getInstance().getCity());
        } else {
            mLocation.setEnabled(false);
            mLocation.setText(WordUtil.getString(R.string.mars));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQ_CODE_GOODS:
                    mGoodsId = intent.getStringExtra(Constants.MALL_GOODS_ID);
                    mGoodsType = intent.getIntExtra(Constants.LIVE_TYPE, 0);
                    String goodsName = intent.getStringExtra(Constants.MALL_GOODS_NAME);
                    mGoodsName.setText(goodsName);
                    break;
                case REQ_CODE_CLASS:
                    mVideoClassId = intent.getIntExtra(Constants.VIDEO_ID, 0);
                    if (mVideoClassName != null) {
                        mVideoClassName.setText(intent.getStringExtra(Constants.CLASS_NAME));
                    }
                    break;
                case REQUEST_CODE_VIDEO:
                    String mVideoPath = intent.getStringExtra(Constants.VIDEO_PATH);
                    Long mDuration = intent.getLongExtra(Constants.VIDEO_DURATION, 0);
                    VideoEditActivity.forward(this, mDuration, mVideoPath, false, false);
                    finish();
                    break;
            }
        }
    }


    /**
     * ????????????
     */
    private void publishVideo() {
        if (mConfigBean == null || TextUtils.isEmpty(mVideoPath)) {
            return;
        }
        if (mVideoClassId == 0) {
            ToastUtil.show(R.string.video_choose_class_2);
            return;
        }
        /*if (TextUtils.isEmpty(mVideoTitle)) {
            ToastUtil.show("?????????????????????");
            return;
        }*/
        mBtnPub.setEnabled(false);
        mVideoTitle = mInput.getText().toString().trim();
        mLoading = DialogUitl.loadingDialog(mContext, WordUtil.getString(R.string.video_pub_ing));
        mLoading.show();
        mVideoRatio = originalVideoHeight / originalVideoWidth;
        Bitmap bitmap = null;
        for (CoverBean coverBean : mVideoCoverAdapter.getList()) {
            if (coverBean.isChecked())
                bitmap = coverBean.getBitmap();
        }
//        Bitmap bitmap = null;
//        //?????????????????????
//        MediaMetadataRetriever mmr = null;
//        try {
//            mmr = new MediaMetadataRetriever();
//            mmr.setDataSource(mVideoPath);
//            bitmap = mmr.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST);
//            String width = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);//???
//            String height = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);//???
//            String rotation = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
//            double videoWidth = 0;
//            double videoHeight = 0;
//            if ("0".equals(rotation)) {
//                if (!TextUtils.isEmpty(width)) {
//                    videoWidth = Double.parseDouble(width);
//                }
//                if (!TextUtils.isEmpty(height)) {
//                    videoHeight = Double.parseDouble(height);
//                }
//            } else {
//                if (!TextUtils.isEmpty(height)) {
//                    videoWidth = Double.parseDouble(height);
//                }
//                if (!TextUtils.isEmpty(width)) {
//                    videoHeight = Double.parseDouble(width);
//                }
//            }
//            if (videoHeight != 0 && videoWidth != 0) {
//                mVideoRatio = videoHeight / videoWidth;
//            }
//        } catch (Exception e) {
//            bitmap = null;
//            e.printStackTrace();
//        } finally {
//            if (mmr != null) {
//                mmr.release();
//            }
//        }
        if (bitmap == null) {
            ToastUtil.show(R.string.video_cover_img_failed);
            onFailed();
            return;
        }
        final String coverImagePath = mVideoPath.replace(".mp4", ".jpg");
        File imageFile = new File(coverImagePath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            imageFile = null;
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        /*if (bitmap != null) {
            bitmap.recycle();
        }*/
        if (imageFile == null) {
            ToastUtil.show(R.string.video_cover_img_failed);
            onFailed();
            return;
        }
        final File finalImageFile = imageFile;
        //?????????????????????
        Luban.with(this)
                .load(finalImageFile)
                .setFocusAlpha(false)
                .ignoreBy(8)//8k???????????????
                .setTargetDir(CommonAppConfig.VIDEO_PATH)
                .setRenameListener(new OnRenameListener() {
                    @Override
                    public String rename(String filePath) {
                        filePath = filePath.substring(filePath.lastIndexOf("/") + 1);
                        return filePath.replace(".jpg", "_c.jpg");
                    }
                })
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(File file) {
                        if (!finalImageFile.getAbsolutePath().equals(file.getAbsolutePath()) && finalImageFile.exists()) {
                            finalImageFile.delete();
                        }
                        uploadVideoFile(file);
                    }

                    @Override
                    public void onError(Throwable e) {
                        uploadVideoFile(finalImageFile);
                    }
                }).launch();
    }

    private void onFailed() {
        if (mLoading != null) {
            mLoading.dismiss();
        }
        if (mBtnPub != null) {
            mBtnPub.setEnabled(true);
        }
    }

    /**
     * ??????????????????
     */
    private void uploadVideoFile(File imageFile) {
        if (mConfigBean.getVideoCloudType() == 1) {
            mUploadStrategy = new VideoUploadQnImpl(mConfigBean);
        } else {
            mUploadStrategy = new VideoUploadTxImpl(mConfigBean);
        }
        VideoUploadBean videoUploadBean = new VideoUploadBean(new File(mVideoPath), imageFile);
        if (!TextUtils.isEmpty(mVideoPathWater)) {
            File waterFile = new File(mVideoPathWater);
            if (waterFile.exists()) {
                videoUploadBean.setVideoWaterFile(waterFile);
            }
        }
        mUploadStrategy.upload(videoUploadBean, new VideoUploadCallback() {
            @Override
            public void onSuccess(VideoUploadBean bean) {
                if (mSaveType == Constants.VIDEO_SAVE_PUB) {
                    bean.deleteFile();
                }
                saveUploadVideoInfo(bean);
            }

            @Override
            public void onFailure() {
                ToastUtil.show(R.string.video_pub_failed);
                onFailed();
            }
        });
    }

    /**
     * ?????????????????????????????????????????????
     */
    private void saveUploadVideoInfo(VideoUploadBean bean) {
        VideoHttpUtil.saveUploadVideoInfo(
                mVideoTitle,
                bean.getResultImageUrl(),
                bean.getResultVideoUrl(),
                bean.getResultWaterVideoUrl(),
                mGoodsId,
                mMusicId,
                mCheckBox != null && mCheckBox.isChecked(),
                mVideoClassId,
                mGoodsType,
                mVideoRatio,
                mCheckBoxOriginal.isChecked(),
                mCheckBoxTeachingl.isChecked(),
                new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0) {
                            if (info.length > 0) {
                                if (mConfigBean != null && mConfigBean.getVideoAuditSwitch() == 1) {
                                    ToastUtil.show(R.string.video_pub_success_2);
                                } else {
                                    ToastUtil.show(R.string.video_pub_success);
                                }
                                if (mAdapter != null) {
                                    String shareType = mAdapter.getShareType();
                                    if (shareType != null) {
                                        JSONObject obj = JSON.parseObject(info[0]);
                                        shareVideoPage(shareType, obj.getString("id"), obj.getString("title"), obj.getString("thumb_s"));
                                    }
                                }
                                finish();
                            }
                        } else {
                            ToastUtil.show(msg);
                            if (mBtnPub != null) {
                                mBtnPub.setEnabled(true);
                            }
                        }
                    }

                    @Override
                    public void onFinish() {
                        if (mLoading != null) {
                            mLoading.dismiss();
                        }
                    }
                });
    }

    /**
     * ??????????????????
     */

    public void shareVideoPage(String shareType, String videoId, String videoTitle, String videoImageUrl) {
        ShareData data = new ShareData();
        String shareVideoTitle = mConfigBean.getVideoShareTitle();
        if (!TextUtils.isEmpty(shareVideoTitle) && shareVideoTitle.contains("{username}")) {
            shareVideoTitle = shareVideoTitle.replace("{username}", CommonAppConfig.getInstance().getUserBean().getUserNiceName());
        }
        data.setTitle(shareVideoTitle);
        if (TextUtils.isEmpty(videoTitle)) {
            data.setDes(mConfigBean.getVideoShareDes());
        } else {
            data.setDes(videoTitle);
        }
        data.setImgUrl(videoImageUrl);
        String webUrl = HtmlConfig.SHARE_VIDEO + videoId;
        data.setWebUrl(webUrl);
        if (mMobShareUtil == null) {
            mMobShareUtil = new MobShareUtil();
        }
        mMobShareUtil.execute(shareType, data, null);
    }


}

package cn.wu1588.video.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodPlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import cn.wu1588.common.CommonAppConfig;
import cn.wu1588.common.Constants;
import cn.wu1588.common.HtmlConfig;
import cn.wu1588.common.activity.AbsActivity;
import cn.wu1588.common.bean.ConfigBean;
import cn.wu1588.common.glide.ImgLoader;
import cn.wu1588.common.http.CommonHttpConsts;
import cn.wu1588.common.http.CommonHttpUtil;
import cn.wu1588.common.http.HttpCallback;
import cn.wu1588.common.interfaces.CommonCallback;
import cn.wu1588.common.mob.CoverBean;
import cn.wu1588.common.mob.MobShareUtil;
import cn.wu1588.common.mob.ShareData;
import cn.wu1588.common.utils.BitmapUtil;
import cn.wu1588.common.utils.DensityUtils;
import cn.wu1588.common.utils.RouteUtil;
import cn.wu1588.common.utils.ToastUtil;
import cn.wu1588.common.utils.WordUtil;
import cn.wu1588.video.R;
import cn.wu1588.video.adapter.VideoCoverAdapter;
import cn.wu1588.video.adapter.VideoPubShareAdapter;
import cn.wu1588.video.bean.VideoBean;
import cn.wu1588.video.http.VideoHttpConsts;
import cn.wu1588.video.http.VideoHttpUtil;
import cn.wu1588.video.upload.VideoUploadBean;
import cn.wu1588.video.upload.VideoUploadCallback;
import cn.wu1588.video.upload.VideoUploadQnImpl;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;
import top.zibin.luban.OnRenameListener;

/**
 * Created by cxf on 2018/12/10.
 * 视频发布
 */

public class VideoRePublishActivity extends AbsActivity implements ITXVodPlayListener, View.OnClickListener {
    private static final int MAX_DURATION = 15000;//最大录制时间15s
    private static final int REQUEST_CODE_VIDEO = 123;//选择视频
    private static final int REQUEST_LIST_CODE = 188;

    private VideoCoverAdapter mVideoCoverAdapter;
    private RecyclerView mRecyclerCover;
    private VideoBean mVideoBean;
    private ImageView ivCover;

    public static void forward(Context context, VideoBean videoBean) {
        Intent intent = new Intent(context, VideoRePublishActivity.class);
        intent.putExtra(Constants.VIDEO_BEAN, videoBean);
        context.startActivity(intent);
    }

    private static final int REQ_CODE_GOODS = 100;
    private static final int REQ_CODE_CLASS = 101;
    private TextView mNum;
    private TextView mLocation;
    private RecyclerView mRecyclerView;
    private ConfigBean mConfigBean;
    private VideoPubShareAdapter mAdapter;
    private VideoUploadQnImpl mUploadStrategy;
    private EditText mInput;
    private String mVideoTitle;//视频标题
    private Dialog mLoading;
    private MobShareUtil mMobShareUtil;
    private View mBtnPub;
    private CheckBox mCheckBox;
    private CheckBox mCheckBoxOriginal;
    private CheckBox mCheckBoxTeachingl;
    private TextView mGoodsName;
    private View mBtnGoodsAdd;
    private TextView mVideoClassName;
    private int mVideoClassId;
    private int mGoodsType;//type  绑定的内容类型 0 没绑定 1 商品 2 付费内容
    private String mGoodsId;//绑定的内容Id
    private String coverImagePath;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_publish;
    }

    @Override
    protected void main() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTitle(WordUtil.getString(R.string.video_pub));
        Intent intent = getIntent();
        mVideoBean = intent.getParcelableExtra(Constants.VIDEO_BEAN);
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
        mInput.setText(mVideoBean.getTitle());
        mVideoClassId = mVideoBean.getClassid();
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
        ivCover = findViewById(R.id.img);
        mCheckBox.setOnClickListener(this);
        mCheckBox.setOnClickListener(this);
        mCheckBoxOriginal = findViewById(R.id.checkbox_original);
        mCheckBoxOriginal.setOnClickListener(this);
        mCheckBoxTeachingl = findViewById(R.id.checkbox_teachingl);
        mCheckBoxTeachingl.setOnClickListener(this);

        mBtnGoodsAdd = findViewById(R.id.btn_goods_add);
        mVideoClassName = findViewById(R.id.video_class_name);
        mGoodsName = findViewById(R.id.goods_name);
        findViewById(R.id.btn_video_class).setOnClickListener(this);

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
                            mBtnGoodsAdd.setOnClickListener(VideoRePublishActivity.this);
                        }
                    }
                }
            }
        });

        findViewById(R.id.btn_reset_video).setVisibility(View.GONE);
        findViewById(R.id.btn_upload_cover).setOnClickListener(this);
        findViewById(R.id.btn_capture_cover).setVisibility(View.GONE);
        ViewGroup.LayoutParams layoutParams = ivCover.getLayoutParams();
        layoutParams.width = (int) (DensityUtils.getScreenW(mContext) * 0.4f);
        layoutParams.height = (int) (layoutParams.width * 1.2f);
        ivCover.setLayoutParams(layoutParams);
        ImgLoader.display(mContext, mVideoBean.getThumb(), ivCover);
    }


    private void addVideoCover(int coverItemWidth, int coverItemHeight, Bitmap bitmap) {
        CoverBean bean = new CoverBean();
        bean.setWidth((int) coverItemWidth);
        bean.setHight((int) coverItemHeight);
//        bean.setBitmap(Bitmap.createScaledBitmap(bitmap, bean.getWidth(), bean.getHight(), true));
//        bean.setBitmap(setBitmapSize(bitmap, bean.getWidth(), bean.getHight()));
        bean.setBitmap(bitmap);
        List<CoverBean> list = mVideoCoverAdapter.getList();
        list.clear();
        list.add(bean);
        bean.setChecked(true);
        mRecyclerCover.post(new Runnable() {
            @Override
            public void run() {
                mVideoCoverAdapter.notifyDataSetChanged();
            }
        });
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


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void release() {
        CommonHttpUtil.cancel(CommonHttpConsts.GET_CONFIG);
        VideoHttpUtil.cancel(VideoHttpConsts.GET_CONCAT_GOODS);
        VideoHttpUtil.cancel(VideoHttpConsts.SAVE_UPLOAD_VIDEO_INFO);
        if (mVideoCoverAdapter != null && mVideoCoverAdapter.getList() != null) {
            for (CoverBean coverBean : mVideoCoverAdapter.getList()) {
                /*if (coverBean.getBitmap() != null) {
                    coverBean.getBitmap().recycle();
                }*/
            }
        }
        if (mUploadStrategy != null) {
            mUploadStrategy.cancel();
        }
        if (mMobShareUtil != null) {
            mMobShareUtil.release();
        }
        mUploadStrategy = null;
        mMobShareUtil = null;
    }

    @Override
    public void onBackPressed() {
        release();
        VideoRePublishActivity.super.onBackPressed();
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
        } else if (i == R.id.btn_goods_add) {
            RouteUtil.searchMallGoods(this, REQ_CODE_GOODS);
        } else if (i == R.id.btn_video_class) {
            chooseVideoClass();
        } else if (i == R.id.btn_reset_video) {
            Intent intent = new Intent(mContext, VideoChooseActivity.class);
            intent.putExtra(Constants.VIDEO_DURATION, MAX_DURATION);
            startActivityForResult(intent, REQUEST_CODE_VIDEO);
        } else if (i == R.id.btn_upload_cover) {
//            ToastUtil.show(R.string.video_upload_success);
            //获取写的权限
            Intent intent = new Intent(Intent.ACTION_PICK, null);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    "image/*");
            startActivityForResult(intent, REQUEST_LIST_CODE);
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
                case REQUEST_LIST_CODE:
                    try {
                        Bitmap bitmapFormUri = BitmapUtil.getBitmapFormUri(this, intent.getData());
                        coverImagePath = getRealPathFromURI(intent.getData());
                        addVideoCover(400, 400, bitmapFormUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //selectList = PictureSelector.obtainMultipleResult(data);

                    // 例如 LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的
                    break;
            }
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(contentURI, null, null, null, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    /**
     * 发布视频
     */
    private void publishVideo() {
        if (mConfigBean == null) {
            return;
        }
        if (mVideoClassId == 0) {
            ToastUtil.show(R.string.video_choose_class_2);
            return;
        }
        mBtnPub.setEnabled(false);
        mVideoTitle = mInput.getText().toString().trim();
        /*mLoading = DialogUitl.loadingDialog(mContext, WordUtil.getString(R.string.video_pub_ing));
        mLoading.show();*/
//        mVideoRatio = originalVideoHeight / originalVideoWidth;
        Bitmap bitmap = null;
        for (CoverBean coverBean : mVideoCoverAdapter.getList()) {
            if (coverBean.isChecked())
                bitmap = coverBean.getBitmap();
        }
        if (bitmap == null) {
            saveUploadVideoInfo(mVideoBean.getThumb());
        } else {
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
            //用鲁班压缩图片
            Luban.with(this)
                    .load(finalImageFile)
                    .setFocusAlpha(false)
                    .ignoreBy(8)//8k以下不压缩
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
     * 上传封面图片
     */
    private void uploadVideoFile(File imageFile) {
        mUploadStrategy = new VideoUploadQnImpl(mConfigBean);
        mUploadStrategy.upload(imageFile, new VideoUploadCallback() {
            @Override
            public void onSuccess(VideoUploadBean bean) {
                String thumbUrl = bean.getResultImageUrl();
                saveUploadVideoInfo(thumbUrl);
            }

            @Override
            public void onFailure() {
                ToastUtil.show(R.string.video_pub_failed);
                onFailed();
            }
        });
    }

    /**
     * 把视频上传后的信息保存在服务器
     */
    private void saveUploadVideoInfo(String thumbUrl) {
        VideoHttpUtil.editUploadVideoInfo(
                mVideoTitle,
                thumbUrl,
                mVideoBean.getId(),
                mGoodsId,
                mVideoBean.getType(),
                mVideoBean.getMusicId(),
                mCheckBox != null && mCheckBox.isChecked(),
                mVideoClassId,
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
     * 分享页面链接
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

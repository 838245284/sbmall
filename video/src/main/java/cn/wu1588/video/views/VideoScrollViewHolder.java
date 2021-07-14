package cn.wu1588.video.views;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.comm.util.AdError;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.wu1588.common.Constants;
import cn.wu1588.common.event.FollowEvent;
import cn.wu1588.common.http.HttpCallback;
import cn.wu1588.common.utils.DensityUtils;
import cn.wu1588.common.utils.RandomUtil;
import cn.wu1588.common.utils.SpUtil;
import cn.wu1588.common.utils.ToastUtil;
import cn.wu1588.common.views.AbsViewHolder;
import cn.wu1588.video.R;
import cn.wu1588.video.activity.AbsVideoPlayActivity;
import cn.wu1588.video.adapter.VideoScrollAdapter;
import cn.wu1588.video.bean.VideoBean;
import cn.wu1588.video.bean.VideoWithAds;
import cn.wu1588.video.custom.VideoLoadingBar;
import cn.wu1588.video.event.VideoCommentEvent;
import cn.wu1588.video.event.VideoLikeEvent;
import cn.wu1588.video.event.VideoScrollPageEvent;
import cn.wu1588.video.event.VideoShareEvent;
import cn.wu1588.video.http.VideoHttpConsts;
import cn.wu1588.video.http.VideoHttpUtil;
import cn.wu1588.video.interfaces.VideoScrollDataHelper;
import cn.wu1588.video.utils.VideoStorge;

/**
 * Created by cxf on 2018/11/26.
 * 视频滑动
 */

public class VideoScrollViewHolder extends AbsViewHolder implements
        VideoScrollAdapter.ActionListener, SwipeRefreshLayout.OnRefreshListener,
        VideoPlayViewHolder.ActionListener, View.OnClickListener {

    private VideoPlayViewHolder mVideoPlayViewHolder;
    private View mPlayView;
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private VideoScrollAdapter mVideoScrollAdapter;
    private int mPosition;
    private String mVideoKey;
    private VideoPlayWrapViewHolder mVideoPlayWrapViewHolder;
    private VideoLoadingBar mVideoLoadingBar;
    private int mPage;
    private HttpCallback mRefreshCallback;//下拉刷新回调
    private HttpCallback mLoadMoreCallback;//上拉加载更多回调
    private VideoScrollDataHelper mVideoDataHelper;
    private VideoBean mVideoBean;
    private boolean mPaused;//生命周期暂停
    private TTAdNative mTTAdNative;
    Handler handler = new Handler(Looper.getMainLooper());
    List<TTNativeExpressAd> adList = new ArrayList<>();
    private List<VideoBean> list;
    SparseArray<View> adViewMap = new SparseArray<>();
    private View llybot;
    private String adValue;
    private NativeExpressADView qqad;
    private String qqadValue;

    public VideoScrollViewHolder(Context context, ViewGroup parentView, int position, String videoKey, int page) {
        super(context, parentView, position, videoKey, page);
    }

    @Override
    protected void processArguments(Object... args) {
        mPosition = (int) args[0];
        mVideoKey = (String) args[1];
        mPage = (int) args[2];
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_video_scroll;
    }

    @Override
    public void init() {
        list = VideoStorge.getInstance().get(mVideoKey);
        if (list == null || list.size() == 0) {
            return;
        }
        List<VideoWithAds> adsList = convertList(list);
        mVideoScrollAdapter = new VideoScrollAdapter(mContext, adsList, mPosition);
        initAds();

        adValue = SpUtil.getInstance().getStringValue(SpUtil.AD);
        qqadValue = SpUtil.getInstance().getStringValue(SpUtil.QQAD);
        if (TextUtils.equals(adValue, "1")) {
            for (int i = 0; i < 3; i++) {
                loadAds(i);
            }
        }
        if (TextUtils.equals(qqadValue, "1")) {
            refreshAd();
        }
        llybot = findViewById(R.id.llybot);
        mVideoPlayViewHolder = new VideoPlayViewHolder(mContext, null);
        mVideoPlayViewHolder.setActionListener(this);
        mPlayView = mVideoPlayViewHolder.getContentView();
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeResources(R.color.global);
        mRefreshLayout.setEnabled(false);//产品不让使用刷新
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));

        mVideoScrollAdapter.setActionListener(this);
        mRecyclerView.setAdapter(mVideoScrollAdapter);
        mVideoLoadingBar = (VideoLoadingBar) findViewById(R.id.video_loading);
        findViewById(R.id.input_tip).setOnClickListener(this);
        findViewById(R.id.btn_face).setOnClickListener(this);
        EventBus.getDefault().register(this);
        mRefreshCallback = new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    List<VideoBean> list = JSON.parseArray(Arrays.toString(info), VideoBean.class);
                    if (mVideoScrollAdapter != null) {
                        mVideoScrollAdapter.setList(convertList(list));
                    }
                }
            }

            @Override
            public void onFinish() {
                if (mRefreshLayout != null) {
                    mRefreshLayout.setRefreshing(false);
                }
            }
        };
        mLoadMoreCallback = new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    List<VideoBean> list = JSON.parseArray(Arrays.toString(info), VideoBean.class);
                    if (list.size() > 0) {
                        if (mVideoScrollAdapter != null) {
                            mVideoScrollAdapter.insertList(convertList(list));
                        }
                        EventBus.getDefault().post(new VideoScrollPageEvent(mVideoKey, mPage));
                    } else {
                        ToastUtil.show(R.string.video_no_more_video);
                        mPage--;
                    }
                } else {
                    mPage--;
                }
            }
        };
        mVideoDataHelper = VideoStorge.getInstance().getDataHelper(Constants.VIDEO_HOME);
    }


    @Override
    public void onPageSelected(VideoPlayWrapViewHolder videoPlayWrapViewHolder, boolean needLoadMore, int scrollIndex) {
        if (videoPlayWrapViewHolder != null) {
            VideoWithAds withAds = videoPlayWrapViewHolder.getVideoBean();
            mVideoBean = withAds.videoBean;
            if (mVideoBean == null || TextUtils.isEmpty(mVideoBean.getHref())) {
                mVideoBean = getValidateVideoBean();
            }
            mVideoPlayWrapViewHolder = videoPlayWrapViewHolder;
            boolean loadAd = TextUtils.equals(adValue, "1") && !adList.isEmpty() ;
            boolean loadQQAd = TextUtils.equals(qqadValue, "1") && !qqAdList.isEmpty() ;
            if ((loadAd||loadQQAd) && scrollIndex % 7 == 0) {
                llybot.setVisibility(View.GONE);
                mVideoLoadingBar.setVisibility(View.GONE);
                videoPlayWrapViewHolder.showAd(true);
                if(loadAd && loadQQAd){
                    if(scrollIndex % 14 ==0){
                        showQQad(videoPlayWrapViewHolder);
                    }else{
                        showCsjAd(videoPlayWrapViewHolder, scrollIndex);
                    }
                }else if(loadAd){
                    showCsjAd(videoPlayWrapViewHolder, scrollIndex);
                }else {
                    showQQad(videoPlayWrapViewHolder);
                }
            } else {
                llybot.setVisibility(View.VISIBLE);
                mVideoLoadingBar.setVisibility(View.VISIBLE);
                videoPlayWrapViewHolder.showAd(false);
                videoPlayWrapViewHolder.addVideoView(mPlayView);
                if (mVideoPlayViewHolder != null) {
                    mVideoPlayViewHolder.startPlay(mVideoBean);
                }
                if (mVideoLoadingBar != null) {
                    mVideoLoadingBar.setLoading(true);
                }
            }
        }
            /*else {
                videoPlayWrapViewHolder.addVideoView(withAds.ad.getExpressAdView());
            }*/
        if (needLoadMore) {
            onLoadMore();
        }
    }

    private void showCsjAd(VideoPlayWrapViewHolder videoPlayWrapViewHolder, int scrollIndex) {
        View view = adViewMap.get(scrollIndex);
        if (view != null) {
            videoPlayWrapViewHolder.addVideoView(view);
        } else {
            int i = RandomUtil.nextInt(adList.size());
            View expressAdView = adList.get(i).getExpressAdView();
            adViewMap.put(scrollIndex, expressAdView);
            videoPlayWrapViewHolder.addVideoView(expressAdView);
        }
    }

    private void showQQad(VideoPlayWrapViewHolder videoPlayWrapViewHolder) {
        int i = RandomUtil.nextInt(qqAdList.size());
        NativeExpressADView qq = qqAdList.get(i);
        qq.render();
        videoPlayWrapViewHolder.addVideoView(qq);
    }

    private VideoBean getValidateVideoBean() {
        for (int i = 0; i < list.size(); i++) {
            VideoBean bean = list.get(i);
            String href = bean.getHref();
            if(TextUtils.equals(mVideoBean.getThumb(),bean.getThumb())&& !TextUtils.isEmpty(href)){
                return list.get(i);
            }
        }
        while (true) {
            int i = RandomUtil.nextInt(list.size());
            VideoBean bean = list.get(i);
            String href = bean.getHref();
            if(TextUtils.equals(mVideoBean.getThumb(),bean.getThumb())){}
            if (!TextUtils.isEmpty(href)) {
                return list.get(i);
            }
        }
    }

    @Override
    public void onPageOutWindow(VideoPlayWrapViewHolder vh) {
        if (mVideoPlayWrapViewHolder != null && mVideoPlayWrapViewHolder == vh && mVideoPlayViewHolder != null) {
            mVideoPlayViewHolder.stopPlay();
        }
    }

    private List<VideoWithAds> convertList(List<VideoBean> list) {
        List<VideoWithAds> adsList = new ArrayList<>();
        for (VideoBean video : list) {
            VideoWithAds withAd = new VideoWithAds();
            withAd.videoBean = video;
            withAd.itemType = VideoWithAds.ITEM_TYPE_SHORT_VIDEO;
            adsList.add(withAd);
        }
        return adsList;
    }

    @Override
    public void onVideoDeleteAll() {
        ((AbsVideoPlayActivity) mContext).onBackPressed();
    }

    public void release() {
        VideoHttpUtil.cancel(VideoHttpConsts.GET_HOME_VIDEO_LIST);
        EventBus.getDefault().unregister(this);
        if (mVideoPlayViewHolder != null) {
            mVideoPlayViewHolder.release();
        }
        mVideoPlayWrapViewHolder = null;
        if (mVideoLoadingBar != null) {
            mVideoLoadingBar.endLoading();
        }
        mVideoLoadingBar = null;
        if (mRefreshLayout != null) {
            mRefreshLayout.setOnRefreshListener(null);
        }
        mRefreshLayout = null;
        if (mVideoScrollAdapter != null) {
            mVideoScrollAdapter.release();
        }
        mVideoScrollAdapter = null;
        mVideoDataHelper = null;
    }


    /**
     * 下拉刷新
     */
    @Override
    public void onRefresh() {
        mPage = 1;
        if (mVideoDataHelper != null) {
            mVideoDataHelper.loadData(mPage, mRefreshCallback);
        }
    }

    /**
     * 加载更多
     */
    private void onLoadMore() {
        mPage++;
        if (mVideoDataHelper != null) {
            mVideoDataHelper.loadData(mPage, mLoadMoreCallback);
        }
    }

    @Override
    public void onPlayBegin() {
        if (mVideoLoadingBar != null) {
            mVideoLoadingBar.setLoading(false);
        }
    }

    @Override
    public void onPlayLoading() {
        if (mVideoLoadingBar != null) {
            mVideoLoadingBar.setLoading(true);
        }
    }

    @Override
    public void onFirstFrame() {
        if (mVideoPlayWrapViewHolder != null) {
            mVideoPlayWrapViewHolder.onFirstFrame();
        }
    }

    /**
     * 关注发生变化
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFollowEvent(FollowEvent e) {
        if (mVideoScrollAdapter != null && mVideoPlayWrapViewHolder != null) {
            VideoBean videoBean = mVideoPlayWrapViewHolder.getVideoBean().videoBean;
            if (videoBean != null) {
                mVideoScrollAdapter.onFollowChanged(!mPaused, videoBean.getId(), e.getToUid(), e.getIsAttention());
            }
        }
    }

    /**
     * 点赞发生变化
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoLikeEvent(VideoLikeEvent e) {
        if (mVideoScrollAdapter != null) {
            mVideoScrollAdapter.onLikeChanged(!mPaused, e.getVideoId(), e.getIsLike(), e.getLikeNum());
        }
    }

    /**
     * 分享数发生变化
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoShareEvent(VideoShareEvent e) {
        if (mVideoScrollAdapter != null) {
            mVideoScrollAdapter.onShareChanged(e.getVideoId(), e.getShareNum());
        }
    }

    /**
     * 评论数发生变化
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoCommentEvent(VideoCommentEvent e) {
        if (mVideoScrollAdapter != null) {
            mVideoScrollAdapter.onCommentChanged(e.getVideoId(), e.getCommentNum());
        }
    }

    /**
     * 删除视频
     */
    public void deleteVideo(VideoBean videoBean) {
        if (mVideoScrollAdapter != null) {
            mVideoScrollAdapter.deleteVideo(videoBean);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.input_tip) {
            openCommentInputWindow(false);

        } else if (i == R.id.btn_face) {
            openCommentInputWindow(true);

        }
    }

    /**
     * 打开评论输入框
     */
    private void openCommentInputWindow(boolean openFace) {
        if (mVideoBean != null) {
            ((AbsVideoPlayActivity) mContext).openCommentInputWindow(openFace, mVideoBean.getId(), mVideoBean.getUid(), null);
        }
    }

    /**
     * VideoBean 数据发生变化
     */
    public void onVideoBeanChanged(String videoId) {
        if (mVideoScrollAdapter != null) {
            mVideoScrollAdapter.onVideoBeanChanged(videoId);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mPaused = true;
        if (mVideoPlayViewHolder != null) {
            mVideoPlayViewHolder.pausePlay();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mPaused = false;
        if (mVideoPlayViewHolder != null) {
            mVideoPlayViewHolder.resumePlay();
        }
    }

    private void loadAds(final int position) {
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId("946046253") //广告位id
                .setSupportDeepLink(true)
                .setAdCount(3) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(DensityUtils.getScreenW(mContext), DensityUtils.getScreenH(mContext)) //期望模板广告view的size,单位dp
                .build();
        mTTAdNative.loadExpressDrawFeedAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            //广告加载失败
            @Override
            public void onError(int code, String message) {

            }

            //广告加载成功
            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                showAd(ads, position);
            }
        });
    }

    private void showAd(List<TTNativeExpressAd> ads, final int position) {
        final TTNativeExpressAd ad = ads.get(0);
        ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
            //广告点击的回调
            @Override
            public void onAdClicked(View view, int type) {

            }

            //广告展示回调
            @Override
            public void onAdShow(View view, int type) {

            }

            //广告渲染失败
            @Override
            public void onRenderFail(View view, String msg, int code) {

            }

            //广告渲染成功
            @Override
            public void onRenderSuccess(View view, float width, float height) {
//                root.addView(ad.getExpressAdView());
                adList.add(ad);
                /*if (mVideoScrollAdapter != null)
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mVideoScrollAdapter.insertBean(withAds, position);
                    }
                });*/
            }
        });
        ad.render();
    }

    private void initAds() {

        mTTAdNative = TTAdSdk.getAdManager().createAdNative(mContext);
        //step3:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdSdk.getAdManager().requestPermissionIfNecessary(mContext);
    }

    private List<NativeExpressADView> qqAdList = new ArrayList<>();

    private void refreshAd() {
        int expressViewWidth = DensityUtils.getScreenW(mContext);
        int expressViewHeight = DensityUtils.getScreenH(mContext);
        NativeExpressAD nativeExpressAD = new NativeExpressAD(mContext, new ADSize(expressViewWidth, expressViewHeight), "2022505652179259", new NativeExpressAD.NativeExpressADListener() {


            @Override
            public void onADLoaded(List<NativeExpressADView> list) {
                // 3.返回数据后，SDK 会返回可以用于展示 NativeExpressADView 列表
                /*qqad = list.get(0);
                qqad.render();*/
                qqAdList.addAll(list);
            }

            @Override
            public void onRenderFail(NativeExpressADView nativeExpressADView) {
            }

            @Override
            public void onRenderSuccess(NativeExpressADView nativeExpressADView) {

            }

            @Override
            public void onADExposure(NativeExpressADView nativeExpressADView) {
            }

            @Override
            public void onADClicked(NativeExpressADView nativeExpressADView) {

            }

            @Override
            public void onADClosed(NativeExpressADView nativeExpressADView) {

            }

            @Override
            public void onADLeftApplication(NativeExpressADView nativeExpressADView) {

            }

            @Override
            public void onADOpenOverlay(NativeExpressADView nativeExpressADView) {

            }

            @Override
            public void onADCloseOverlay(NativeExpressADView nativeExpressADView) {

            }

            @Override
            public void onNoAD(AdError adError) {
                Log.e("AD_DEMO", String.format("onADError, error code: %d, error msg: %s", adError.getErrorCode(), adError.getErrorMsg()));
            }
        }); // 传入Activity
        // 注意：如果您在平台上新建平台模板广告位时，选择了支持视频，那么可以进行个性化设置（可选）


        nativeExpressAD.loadAD(10);
        nativeExpressAD.setVideoOption(new VideoOption.Builder()
                .setAutoPlayPolicy(VideoOption.AutoPlayPolicy.WIFI) // WIFI 环境下可以自动播放视频
                .setAutoPlayMuted(false)
                .build()); //
    }
}

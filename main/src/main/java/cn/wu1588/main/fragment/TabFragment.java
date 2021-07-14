package cn.wu1588.main.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.comm.constants.AdPatternType;
import com.qq.e.comm.util.AdError;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.wu1588.common.Constants;
import cn.wu1588.common.adapter.RefreshAdapter;
import cn.wu1588.common.custom.CommonRefreshView;
import cn.wu1588.common.custom.ItemDecoration;
import cn.wu1588.common.http.HttpCallback;
import cn.wu1588.common.interfaces.OnItemClickListener;
import cn.wu1588.common.utils.DensityUtils;
import cn.wu1588.common.utils.DpUtil;
import cn.wu1588.common.utils.JsonUtil;
import cn.wu1588.common.utils.RandomUtil;
import cn.wu1588.common.utils.SpUtil;
import cn.wu1588.main.R;
import cn.wu1588.main.adapter.MainHomeVideoAdapter;
import cn.wu1588.video.activity.VideoLongDetailsActivity;
import cn.wu1588.video.activity.VideoPlayActivity;
import cn.wu1588.video.bean.VideoBean;
import cn.wu1588.video.bean.VideoWithAds;
import cn.wu1588.video.event.VideoDeleteEvent;
import cn.wu1588.video.event.VideoScrollPageEvent;
import cn.wu1588.video.http.VideoHttpConsts;
import cn.wu1588.video.http.VideoHttpUtil;
import cn.wu1588.video.interfaces.VideoScrollDataHelper;
import cn.wu1588.video.utils.VideoStorge;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TabFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TabFragment extends Fragment implements OnItemClickListener<VideoWithAds> {

    private CommonRefreshView mRefreshView;
    private MainHomeVideoAdapter mAdapter;
    private static final int ID_RECOMMEND = -1;
    private static final int ID_SHORT_VIDEO = -2;

    private int mVideoClassId = ID_RECOMMEND;
    /**
     * 视频类型 1：短视频 2：长视频
     */
    private int mItemType = VideoWithAds.ITEM_TYPE_SHORT_VIDEO;

    private VideoScrollDataHelper mVideoScrollDataHelper;
    private boolean isFirstLoadData = true;

    private static final String TAG = "TabFragment";
    private String index;
    //    private List<VideoWithAds> list = new ArrayList<>();
    private Context context;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private TTAdNative mTTAdNative;
    private boolean loadqqad = true;

    public static TabFragment newInstance(String label) {
        Bundle args = new Bundle();
        args.putString("label", label);
        TabFragment fragment = new TabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_refreshlist, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        mVideoClassId = getArguments().getInt("id");
        index = getArguments().getString("index");
        mItemType = getArguments().getInt("type", VideoWithAds.ITEM_TYPE_SHORT_VIDEO);
        mRefreshView = view.findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_live_video);
        initAds();
        if (mItemType == VideoWithAds.ITEM_TYPE_SHORT_VIDEO) {
            mRefreshView.setLayoutManager(new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false));
            ItemDecoration decoration = new ItemDecoration(getContext(), 0x00000000, 5, 0);
            decoration.setOnlySetItemOffsetsButNoDraw(true);
            mRefreshView.setItemDecoration(decoration);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.leftMargin = DpUtil.dp2px(5);
            layoutParams.rightMargin = DpUtil.dp2px(5);
            mRefreshView.setLayoutParams(layoutParams);
        } else if (mItemType == VideoWithAds.ITEM_TYPE_LONG_VIDEO) {
            mRefreshView.setLayoutManager(new LinearLayoutManager(getContext()));
            mRefreshView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<VideoWithAds>() {
            @Override
            public RefreshAdapter<VideoWithAds> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new MainHomeVideoAdapter(getContext());
                    mAdapter.setOnItemClickListener(TabFragment.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                if (mVideoClassId == ID_RECOMMEND) {
                    VideoHttpUtil.getHomeVideoList(p, callback);
                } else if (mVideoClassId == ID_SHORT_VIDEO) {
                    VideoHttpUtil.getHomeShortVideoList(p, callback);
                } else {
                    VideoHttpUtil.getHomeVideoClassList(mVideoClassId, p, callback);
                }
            }

            @Override
            public List<VideoWithAds> processData(String[] info) {
                List<VideoBean> infolist = JsonUtil.getJsonToList(Arrays.toString(info), VideoBean.class);
                List<VideoWithAds> processList = new ArrayList<>();
                if (infolist != null && !infolist.isEmpty()) {
//                    LogUtil.e(TAG, Arrays.toString(info));
                    for (VideoBean videoBean : infolist) {
                        VideoWithAds videoWithAds = new VideoWithAds();
                        videoWithAds.videoBean = videoBean;
                        videoWithAds.itemType = mItemType;
                        processList.add(videoWithAds);
                    }
                }
                return processList;

            }

            @Override
            public void onRefreshSuccess(List<VideoWithAds> list, int listCount) {
                if (list == null || list.isEmpty()) {
                    return;
                }
                VideoStorge.getInstance().put(String.valueOf(index), adsToVideo(list));
                String stringValue = SpUtil.getInstance().getStringValue(SpUtil.AD);
                String qqad = SpUtil.getInstance().getStringValue(SpUtil.QQAD);
                boolean loadAd = TextUtils.equals(stringValue, "1");
                boolean loadQQad = TextUtils.equals(qqad, "1");
                if (loadAd || loadQQad) {
                    int space = list.get(0).itemType == VideoWithAds.ITEM_TYPE_SHORT_VIDEO ? 10 : 5;
                    int size = list.size();
                    for (int i = 0; i <= size; i += space) {
                        if (i != 0 && i % space == 0) {
//                            loadListAd(space, i);
                            if(loadAd && loadQQad){
                                refreshAd(i,space);
                            }else if(loadAd){
                                loadListAd(space, i);
                            }else {
                                refreshAd(i,space);
                            }
                        }
                    }
                }
            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<VideoWithAds> loadItemList, int loadItemCount) {
                List<VideoBean> beans = VideoStorge.getInstance().get(index);
                beans.addAll(adsToVideo(loadItemList));
                VideoStorge.getInstance().put(String.valueOf(index), beans);
                String stringValue = SpUtil.getInstance().getStringValue(SpUtil.AD);
                String qqValue = SpUtil.getInstance().getStringValue(SpUtil.QQAD);
                if(TextUtils.equals(stringValue,"1")&&"1".equals(qqValue)){
                    int space = loadItemList.get(0).itemType == VideoWithAds.ITEM_TYPE_SHORT_VIDEO ? 10 : 5;
                    int size = loadItemList.size();
                    for (int i = 0; i <= size; i += space) {
                        List<VideoWithAds> list = mAdapter.getList();
                        int position = i<size? list.indexOf(loadItemList.get(i)):-1;
                        if (i != 0 && i % space == 0) {
                            loadqqad = !loadqqad;
                            if(loadqqad){
                                refreshAd(position,space);
                            }else{
                                loadListAd(space, position);
                            }
                        }
                    }
                }else if(TextUtils.equals(stringValue,"1")&&!"1".equals(qqValue)){
                    int space = loadItemList.get(0).itemType == VideoWithAds.ITEM_TYPE_SHORT_VIDEO ? 10 : 5;
                    int size = loadItemList.size();
                    for (int i = 0; i <= size; i += space) {
                        List<VideoWithAds> list = mAdapter.getList();
                        int position = i<size? list.indexOf(loadItemList.get(i)):-1;
                        if (i != 0 && i % space == 0) {
                                loadListAd(space, position);
                        }
                    }
                }else if(!TextUtils.equals(stringValue,"1")&&"1".equals(qqValue)){
                    int space = loadItemList.get(0).itemType == VideoWithAds.ITEM_TYPE_SHORT_VIDEO ? 10 : 5;
                    int size = loadItemList.size();
                    for (int i = 0; i <= size; i += space) {
                        List<VideoWithAds> list = mAdapter.getList();
                        int position = i<size? list.indexOf(loadItemList.get(i)):-1;
                        if (i != 0 && i % space == 0) {
                            refreshAd(position,space);
                        }
                    }
                }
            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        EventBus.getDefault().register(this);
    }

    private List<VideoBean> adsToVideo(List<VideoWithAds> list){
        List<VideoBean> videoBeans = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            VideoBean videoBean = list.get(i).videoBean;
            videoBeans.add(videoBean);
        }
        return videoBeans;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoScrollPageEvent(VideoScrollPageEvent e) {
        if (Constants.VIDEO_HOME.equals(e.getKey()) && mRefreshView != null) {
            mRefreshView.setPageCount(e.getPage());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoDeleteEvent(VideoDeleteEvent e) {
        if (mAdapter != null) {
            mAdapter.deleteVideo(e.getVideoId());
            if (mAdapter.getItemCount() == 0 && mRefreshView != null) {
                mRefreshView.showEmpty();
            }
        }
    }

    private List<VideoWithAds> videoToAds(List<VideoBean> list){
        List<VideoWithAds> adsList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            VideoWithAds videoWithAds = new VideoWithAds();
            videoWithAds.videoBean = list.get(i);
            videoWithAds.itemType = mItemType;
            adsList.add(videoWithAds);
        }
        return adsList;
    }
    private void initAds() {

        mTTAdNative = TTAdSdk.getAdManager().createAdNative(context);
        //step3:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdSdk.getAdManager().requestPermissionIfNecessary(context);
    }
    private void refreshAd(final int position,int type) {
        int expressViewWidth;
        int expressViewHeight;
        if (type == 10) {
            expressViewWidth = DensityUtils.getScreenWdp(context) / 2 - 12;
            expressViewHeight = (int) (expressViewWidth * 16f / 9 + 7);
        } else {
            expressViewWidth = DensityUtils.getScreenWdp(context);
            expressViewHeight = (int) (expressViewWidth * 3f / 4);
        }
        NativeExpressAD nativeExpressAD = new NativeExpressAD(getActivity(), new ADSize(DensityUtils.getScreenWdp(context)/2+10, ADSize.AUTO_HEIGHT), "2022505652179259", new NativeExpressAD.NativeExpressADListener() {
            @Override
            public void onADLoaded(List<NativeExpressADView> list) {
                // 3.返回数据后，SDK 会返回可以用于展示 NativeExpressADView 列表
                NativeExpressADView nativeExpressADView = list.get(0);
                if (nativeExpressADView.getBoundData().getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
//                    nativeExpressADView.setMediaListener(mediaListener);
                }
                nativeExpressADView.render();
                bindAdListener(nativeExpressADView,position);
            }

            @Override
            public void onRenderFail(NativeExpressADView nativeExpressADView) {
                Log.i(TAG, "onRenderFail");
            }

            @Override
            public void onRenderSuccess(NativeExpressADView nativeExpressADView) {

            }

            @Override
            public void onADExposure(NativeExpressADView nativeExpressADView) {
                Log.i(TAG, "onADExposure");
            }

            @Override
            public void onADClicked(NativeExpressADView nativeExpressADView) {

            }

            @Override
            public void onADClosed(NativeExpressADView nativeExpressADView) {
                mAdapter.getList().remove(position);
                mAdapter.notifyDataSetChanged();
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


        nativeExpressAD.loadAD(1);
       /* nativeExpressAD.setVideoOption(new VideoOption.Builder()
                .setAutoPlayPolicy(VideoOption.AutoPlayPolicy.WIFI) // WIFI 环境下可以自动播放视频
                .setAutoPlayMuted(true)
                .build()); //*/
    }

    private void loadListAd(int type, final int position) {
        float expressViewWidth;
        float expressViewHeight;
        String code;
        if (type == 10) {
            expressViewWidth = DensityUtils.getScreenWdp(context) / 2 - 12;
            expressViewHeight = expressViewWidth * 16f / 9 + 7;
            code = "946116655";
        } else {
            expressViewWidth = DensityUtils.getScreenWdp(context);
            expressViewHeight = expressViewWidth * 3f / 4;
            code = "946046240";
        }
        //step4:创建feed广告请求类型参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(code)
                .setExpressViewAcceptedSize(expressViewWidth, expressViewHeight) //期望模板广告view的size,单位dp
                .setAdType(AdSlot.TYPE_FEED)
                .setAdCount(1) //请求广告数量为1到3条
                .build();
        //step5:请求广告，调用feed广告异步请求接口，加载到广告后，拿到广告素材自定义渲染
        mTTAdNative.loadNativeExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.e(TAG, "onError: " + message);
            }

            @Override
            public void onNativeExpressAdLoad(final List<TTNativeExpressAd> ads) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        bindAdListener(ads, position);
                    }
                });

            }
        });
    }

    private void bindAdListener(final List<TTNativeExpressAd> ads, int position) {
        List<VideoBean> beans = VideoStorge.getInstance().get(index);
        for (int i = 0; i < ads.size(); i++) {
            VideoWithAds videoWithAds = new VideoWithAds();
            TTNativeExpressAd ad = ads.get(i);
            videoWithAds.ad = ad;
            videoWithAds.itemType = VideoWithAds.ITEM_TYPE_Ads;
            List<VideoWithAds> adapterList = mAdapter.getList();
            VideoBean element = new VideoBean();
            element.setThumb(getValidateThub());
            if(position>0){
                adapterList.add(position, videoWithAds);
                beans.add(position, element);
            }else{
                adapterList.add(videoWithAds);
                beans.add(element);
            }

            ad.render();
        }
        VideoStorge.getInstance().put(index, beans);
        mAdapter.notifyDataSetChanged();
    }

    private String getValidateThub() {
        while (true) {
            List<VideoWithAds> list = mAdapter.getList();
            int i = RandomUtil.nextInt(list.size());
            VideoBean bean = list.get(i).videoBean;
            if(bean!=null && !TextUtils.isEmpty(bean.getThumb())){
                return bean.getThumb();
            }
        }
    }

    private void bindAdListener(NativeExpressADView nativeExpressADView,int position){
        List<VideoBean> beans = VideoStorge.getInstance().get(index);
        VideoWithAds videoWithAds = new VideoWithAds();
        videoWithAds.qqAd = nativeExpressADView;
        videoWithAds.itemType = VideoWithAds.ITEM_TYPE_QQAD;
        List<VideoWithAds> adapterList = mAdapter.getList();
        VideoBean element = new VideoBean();
        element.setThumb(getValidateThub());
        if(position>0){
            adapterList.add(position, videoWithAds);
            beans.add(position, element);
        }else{
            adapterList.add(videoWithAds);
            beans.add(element);
        }
        VideoStorge.getInstance().put(index, beans);
        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    public void loadData() {
        if (!isFirstLoadData) {
            return;
        }
        if (mRefreshView != null) {
            mRefreshView.initData();
            isFirstLoadData = false;
        }
    }

    @Override
    public void onItemClick(VideoWithAds bean, int position) {
        if (bean.itemType == VideoWithAds.ITEM_TYPE_LONG_VIDEO) {
            VideoLongDetailsActivity.forward(getContext(), bean.videoBean);
        } else {
            int page = 1;
            if (mRefreshView != null) {
                page = mRefreshView.getPageCount();
            }
            if (mVideoScrollDataHelper == null) {
                mVideoScrollDataHelper = new VideoScrollDataHelper() {

                    @Override
                    public void loadData(int p, HttpCallback callback) {
                        if (mVideoClassId == ID_RECOMMEND) {
                            VideoHttpUtil.getHomeVideoList(p, callback);
                        } else if (mVideoClassId == ID_SHORT_VIDEO) {
                            VideoHttpUtil.getHomeShortVideoList(p, callback);
                        } else {
                            VideoHttpUtil.getHomeVideoClassList(mVideoClassId, p, callback);
                        }
                    }
                };
            }

            VideoStorge.getInstance().putDataHelper(Constants.VIDEO_HOME, mVideoScrollDataHelper);
            VideoPlayActivity.forward(getContext(), position, index, page);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFirstLoadData = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        VideoHttpUtil.cancel(VideoHttpConsts.GET_HOME_VIDEO_LIST);
        VideoHttpUtil.cancel(VideoHttpConsts.GET_HOME_VIDEO_CLASS_LIST);
        EventBus.getDefault().unregister(this);
        mVideoScrollDataHelper = null;
    }
}
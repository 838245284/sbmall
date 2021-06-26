package cn.wu1588.main.views;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;

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
import cn.wu1588.common.utils.LogUtil;
import cn.wu1588.main.R;
import cn.wu1588.main.adapter.MainHomeVideoAdapter;
import cn.wu1588.video.activity.VideoLongDetailsActivity;
import cn.wu1588.video.activity.VideoPlayActivity;
import cn.wu1588.video.bean.VideoBean;
import cn.wu1588.video.bean.VideoWithAds;
import cn.wu1588.video.http.VideoHttpUtil;
import cn.wu1588.video.interfaces.VideoScrollDataHelper;
import cn.wu1588.video.utils.VideoStorge;

public class TeachVideoView implements OnItemClickListener<VideoWithAds> {
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
    private List<VideoWithAds> list = new ArrayList<>();
    private Context context;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private TTAdNative mTTAdNative;

    public TeachVideoView(Context context,int id, String index, int itemType) {
        this.context = context;
        mVideoClassId = id;
        this.index = index;
        mItemType = itemType;
    }

    public View getContentView(){
        View root = View.inflate(context, R.layout.layout_refreshlist, null);
        mRefreshView = root.findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_live_video);
        initAds();
        setAdapter();
        loadData();
        return root;
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

    private void setAdapter() {
        if (mItemType == VideoWithAds.ITEM_TYPE_SHORT_VIDEO) {
            mRefreshView.setLayoutManager(new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false));
            ItemDecoration decoration = new ItemDecoration(context, 0x00000000, 5, 0);
            decoration.setOnlySetItemOffsetsButNoDraw(true);
            mRefreshView.setItemDecoration(decoration);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.leftMargin = DpUtil.dp2px(5);
            layoutParams.rightMargin = DpUtil.dp2px(5);
            mRefreshView.setLayoutParams(layoutParams);
        } else if (mItemType == VideoWithAds.ITEM_TYPE_LONG_VIDEO) {
            mRefreshView.setLayoutManager(new LinearLayoutManager(context));
            mRefreshView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<VideoWithAds>() {
            @Override
            public RefreshAdapter<VideoWithAds> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new MainHomeVideoAdapter(context);
                    mAdapter.setOnItemClickListener(TeachVideoView.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                if (mVideoClassId == ID_RECOMMEND) {
                    VideoHttpUtil.getTeachVideoList(p, callback);
                } else if (mVideoClassId == ID_SHORT_VIDEO) {
                    VideoHttpUtil.getHomeShortVideoList(p, callback);
                } else {
                    VideoHttpUtil.getTeachVideoClassList(mVideoClassId, p, callback);
                }
            }

            @Override
            public List<VideoWithAds> processData(String[] info) {
                List<VideoBean> infolist = JsonUtil.getJsonToList(Arrays.toString(info), VideoBean.class);
                if (infolist != null && !infolist.isEmpty()) {
                    LogUtil.e(TAG, Arrays.toString(info));
                    for (VideoBean videoBean : infolist) {
                        VideoWithAds videoWithAds = new VideoWithAds();
                        videoWithAds.videoBean = videoBean;
                        videoWithAds.itemType = mItemType;
                        list.add(videoWithAds);
                    }
                    VideoStorge.getInstance().put(String.valueOf(index), infolist);
                }
                return list;

            }

            @Override
            public void onRefreshSuccess(List<VideoWithAds> list, int listCount) {
                if (list == null || list.isEmpty()) {
                    return;
                }
                int space = list.get(0).itemType == VideoWithAds.ITEM_TYPE_SHORT_VIDEO ? 10 : 5;
                int size = list.size();
                for (int i = 0; i < size; i += space) {
                    if (i != 0 && i % space == 0) {
                        loadListAd(space, i);
                    }
                }
            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<VideoWithAds> loadItemList, int loadItemCount) {
//                loadListAd();
            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
    }

    private void initAds() {

        mTTAdNative = TTAdSdk.getAdManager().createAdNative(context);
        //step3:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdSdk.getAdManager().requestPermissionIfNecessary(context);
    }

    private void loadListAd(int type, final int position) {
        float expressViewWidth;
        float expressViewHeight;
        if(type==10){
            expressViewWidth = DensityUtils.getScreenWdp(context) / 2 - 12;
            expressViewHeight = expressViewWidth * 16f / 9 + 7;
        }else{
            expressViewWidth = DensityUtils.getScreenWdp(context);
            expressViewHeight = expressViewWidth * 3f /4;
        }
        //step4:创建feed广告请求类型参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId("946218632")
                .setExpressViewAcceptedSize(expressViewWidth, expressViewHeight) //期望模板广告view的size,单位dp
                .setAdType(AdSlot.TYPE_FEED)
                .setAdCount(1) //请求广告数量为1到3条
                .build();
        //step5:请求广告，调用feed广告异步请求接口，加载到广告后，拿到广告素材自定义渲染
        mTTAdNative.loadNativeExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {
            }

            @Override
            public void onNativeExpressAdLoad(final List<TTNativeExpressAd> ads) {
                if (list != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            bindAdListener(ads, position);
                        }
                    });

                }
            }
        });
    }

    private void bindAdListener(final List<TTNativeExpressAd> ads, int position) {
        for (int i = 0; i < ads.size(); i++) {
            VideoWithAds videoWithAds = new VideoWithAds();
            TTNativeExpressAd ad = ads.get(i);
            videoWithAds.ad = ad;
            videoWithAds.itemType = VideoWithAds.ITEM_TYPE_Ads;
            List<VideoWithAds> adapterList = mAdapter.getList();
            Log.e(TAG, "bindAdListener: " + adapterList.size());
            adapterList.add(position, videoWithAds);
            ad.render();
        }
        mAdapter.notifyDataSetChanged();

       /* ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
            @Override
            public void onAdClicked(View view, int type) {
            }

            @Override
            public void onAdShow(View view, int type) {
            }

            @Override
            public void onRenderFail(View view, String msg, int code) {
            }

            @Override
            public void onRenderSuccess(View view, float width, float height) {
                //返回view的宽高 单位 dp
                Log.e(TAG, "onRenderSuccess: " + width + ":" + height);
            }
        });
        ad.render();*/

    }

    @Override
    public void onItemClick(VideoWithAds bean, int position) {
        if (bean.itemType == VideoWithAds.ITEM_TYPE_LONG_VIDEO) {
            VideoLongDetailsActivity.forward(context, bean.videoBean);
        }else{
            int page = 1;
            if (mRefreshView != null) {
                page = mRefreshView.getPageCount();
            }
            if (mVideoScrollDataHelper == null) {
                mVideoScrollDataHelper = new VideoScrollDataHelper() {

                    @Override
                    public void loadData(int p, HttpCallback callback) {
                        if (mVideoClassId == ID_RECOMMEND) {
                            VideoHttpUtil.getTeachVideoList(p, callback);
                        } /*else if (mVideoClassId == ID_SHORT_VIDEO) {
                            VideoHttpUtil.getHomeShortVideoList(p, callback);
                        }*/ else {
                            VideoHttpUtil.getTeachVideoClassList(mVideoClassId, p, callback);
                        }
                    }
                };
            }

            VideoStorge.getInstance().putDataHelper(Constants.VIDEO_HOME, mVideoScrollDataHelper);
            VideoPlayActivity.forward(context, position, index, page);
        }
    }
}

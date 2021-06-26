package cn.wu1588.main.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoader;
import cn.wu1588.common.CommonAppConfig;
import cn.wu1588.common.Constants;
import cn.wu1588.common.activity.WebViewActivity;
import cn.wu1588.common.adapter.RefreshAdapter;
import cn.wu1588.common.bean.ConfigBean;
import cn.wu1588.common.bean.LiveClassBean;
import cn.wu1588.common.custom.CommonRefreshView;
import cn.wu1588.common.custom.ItemDecoration;
import cn.wu1588.common.glide.ImgLoader;
import cn.wu1588.common.http.HttpCallback;
import cn.wu1588.common.interfaces.OnItemClickListener;
import cn.wu1588.common.utils.WordUtil;
import cn.wu1588.live.bean.LiveBean;
import cn.wu1588.live.utils.LiveStorge;
import cn.wu1588.main.R;
import cn.wu1588.main.activity.LiveClassActivity;
import cn.wu1588.main.activity.LiveRecommendActivity;
import cn.wu1588.main.activity.RankActivity;
import cn.wu1588.main.adapter.MainHomeLiveAdapter;
import cn.wu1588.main.adapter.MainHomeLiveClassAdapter;
import cn.wu1588.main.adapter.MainHomeLiveRecomAdapter;
import cn.wu1588.main.bean.BannerBean;
import cn.wu1588.main.http.MainHttpConsts;
import cn.wu1588.main.http.MainHttpUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/9/22.
 * MainActivity 首页 直播
 */

public class MainHomeLiveViewHolder extends AbsMainHomeChildViewHolder implements OnItemClickListener<LiveBean>, View.OnClickListener {

    private RecyclerView mClassRecyclerViewDialog;
    private View mShadow;
    private View mBtnDismiss;
    private CommonRefreshView mRefreshView;
    private RecyclerView mClassRecyclerViewTop;
    private MainHomeLiveAdapter mAdapter;
    private ObjectAnimator mShowAnimator;
    private ObjectAnimator mHideAnimator;
    private Banner mBanner;
    private boolean mBannerNeedUpdate;
    private List<BannerBean> mBannerList;
    private View mGroupRecommend;
    private RecyclerView mRecyclerViewRecommend;
    private MainHomeLiveRecomAdapter mLiveRecomAdapter;
    private View mGifRecom;


    public MainHomeLiveViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_main_home_live;
    }

    @Override
    public void init() {
        mShadow = findViewById(R.id.shadow);
        mBtnDismiss = findViewById(R.id.btn_dismiss);
        mBtnDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canClick()) {
                    if (mShowAnimator != null) {
                        mShowAnimator.cancel();
                    }
                    if (mHideAnimator != null) {
                        mHideAnimator.start();
                    }
                }
            }
        });
        mRefreshView = (CommonRefreshView) findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_live);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 2, GridLayoutManager.VERTICAL, false);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 0) {
                    return 2;
                }
                return 1;
            }
        });
        mRefreshView.setLayoutManager(gridLayoutManager);
        ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 5, 0);
        decoration.setOnlySetItemOffsetsButNoDraw(true);
        mRefreshView.setItemDecoration(decoration);
        mAdapter = new MainHomeLiveAdapter(mContext);
        mAdapter.setOnItemClickListener(MainHomeLiveViewHolder.this);
        mRefreshView.setRecyclerViewAdapter(mAdapter);
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<LiveBean>() {
            @Override
            public RefreshAdapter<LiveBean> getAdapter() {
                return null;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                MainHttpUtil.getHot(p, callback);
            }

            @Override
            public List<LiveBean> processData(String[] info) {
                JSONObject obj = JSON.parseObject(info[0]);
                mBannerNeedUpdate = false;
                List<BannerBean> bannerList = JSON.parseArray(obj.getString("slide"), BannerBean.class);
                if (bannerList != null && bannerList.size() > 0) {
                    if (mBannerList == null || mBannerList.size() != bannerList.size()) {
                        mBannerNeedUpdate = true;
                    } else {
                        for (int i = 0; i < mBannerList.size(); i++) {
                            BannerBean bean = mBannerList.get(i);
                            if (bean == null || !bean.isEqual(bannerList.get(i))) {
                                mBannerNeedUpdate = true;
                                break;
                            }
                        }
                    }
                }
                mBannerList = bannerList;
                List<LiveBean> recomList = JSON.parseArray(obj.getString("recommend"), LiveBean.class);
                if (recomList != null && recomList.size() > 0) {
                    if (CommonAppConfig.LIVE_ROOM_SCROLL) {
                        LiveStorge.getInstance().put(Constants.LIVE_CLASS_RECOMMEND, recomList);
                    }
                    if (recomList.size() < 3) {
                        if (mGifRecom != null && mGifRecom.getVisibility() == View.VISIBLE) {
                            mGifRecom.setVisibility(View.INVISIBLE);
                        }
                    }
                    if (mGroupRecommend != null && mGroupRecommend.getVisibility() != View.VISIBLE) {
                        mGroupRecommend.setVisibility(View.VISIBLE);
                    }
                    if (mRecyclerViewRecommend != null) {
                        if (mLiveRecomAdapter == null) {
                            mLiveRecomAdapter = new MainHomeLiveRecomAdapter(mContext, recomList);
                            mLiveRecomAdapter.setOnItemClickListener(new OnItemClickListener<LiveBean>() {
                                @Override
                                public void onItemClick(LiveBean bean, int position) {
                                    watchLive(bean, Constants.LIVE_CLASS_RECOMMEND, position);
                                }
                            });
                            mRecyclerViewRecommend.setAdapter(mLiveRecomAdapter);
                        } else {
                            mLiveRecomAdapter.refreshData(recomList);
                        }
                    }
                } else {
                    if (mGroupRecommend != null && mGroupRecommend.getVisibility() != View.GONE) {
                        mGroupRecommend.setVisibility(View.GONE);
                    }
                }
                return JSON.parseArray(obj.getString("list"), LiveBean.class);
            }

            @Override
            public void onRefreshSuccess(List<LiveBean> list, int count) {
                if (CommonAppConfig.LIVE_ROOM_SCROLL) {
                    LiveStorge.getInstance().put(Constants.LIVE_HOME, list);
                }
                showBanner();
            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<LiveBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        View headView = mAdapter.getHeadView();
        mGroupRecommend = headView.findViewById(R.id.group_recommend);
        mRecyclerViewRecommend = headView.findViewById(R.id.recyclerView_recommend);
        mRecyclerViewRecommend.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mGifRecom = headView.findViewById(R.id.gif_recom);
        mRecyclerViewRecommend.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dx > 0) {
                    if (mGifRecom != null && mGifRecom.getVisibility() == View.VISIBLE) {
                        mGifRecom.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
        mClassRecyclerViewTop = headView.findViewById(R.id.classRecyclerView_top);
        mClassRecyclerViewTop.setHasFixedSize(true);
        mClassRecyclerViewTop.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mClassRecyclerViewDialog = (RecyclerView) findViewById(R.id.classRecyclerView_dialog);
        mClassRecyclerViewDialog.setHasFixedSize(true);
        mClassRecyclerViewDialog.setLayoutManager(new GridLayoutManager(mContext, 5, GridLayoutManager.VERTICAL, false));
        ConfigBean configBean = CommonAppConfig.getInstance().getConfig();
        if (configBean != null) {
            List<LiveClassBean> list = configBean.getLiveClass();
            if (list != null && list.size() > 0) {
                List<LiveClassBean> targetList = new ArrayList<>();
                if (list.size() <= 6) {
                    targetList.addAll(list);
                } else {
                    targetList.addAll(list.subList(0, 5));
                    LiveClassBean bean = new LiveClassBean();
                    bean.setAll(true);
                    bean.setName(WordUtil.getString(R.string.all));
                    targetList.add(bean);
                }
                MainHomeLiveClassAdapter topAdapter = new MainHomeLiveClassAdapter(mContext, targetList, false);
                topAdapter.setOnItemClickListener(new OnItemClickListener<LiveClassBean>() {
                    @Override
                    public void onItemClick(LiveClassBean bean, int position) {
                        if (!canClick()) {
                            return;
                        }
                        if (bean.isAll()) {//全部分类
                            showClassListDialog();
                        } else {
                            LiveClassActivity.forward(mContext, bean.getId(), bean.getName());
                        }
                    }
                });
                if (mClassRecyclerViewTop != null) {
                    mClassRecyclerViewTop.setAdapter(topAdapter);
                }
                MainHomeLiveClassAdapter dialogAdapter = new MainHomeLiveClassAdapter(mContext, list, true);
                dialogAdapter.setOnItemClickListener(new OnItemClickListener<LiveClassBean>() {
                    @Override
                    public void onItemClick(LiveClassBean bean, int position) {
                        if (!canClick()) {
                            return;
                        }
                        LiveClassActivity.forward(mContext, bean.getId(), bean.getName());
                    }
                });
                mClassRecyclerViewDialog.setAdapter(dialogAdapter);
                mClassRecyclerViewDialog.post(new Runnable() {
                    @Override
                    public void run() {
                        initAnim();
                    }
                });
            } else {
                if (mClassRecyclerViewTop != null) {
                    mClassRecyclerViewTop.setVisibility(View.GONE);
                }
            }
        }
        mBanner = (Banner) headView.findViewById(R.id.banner);
        mBanner.setImageLoader(new ImageLoader() {
            @Override
            public void displayImage(Context context, Object path, ImageView imageView) {
                ImgLoader.display(mContext, ((BannerBean) path).getImageUrl(), imageView);
            }
        });
        mBanner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int p) {
                if (mBannerList != null) {
                    if (p >= 0 && p < mBannerList.size()) {
                        BannerBean bean = mBannerList.get(p);
                        if (bean != null) {
                            String link = bean.getLink();
                            if (!TextUtils.isEmpty(link)) {
                                WebViewActivity.forward(mContext, link, false);
                            }
                        }
                    }
                }
            }
        });
        headView.findViewById(R.id.btn_profit).setOnClickListener(this);
        headView.findViewById(R.id.btn_con).setOnClickListener(this);
        headView.findViewById(R.id.btn_more_recom).setOnClickListener(this);
    }

    private void showBanner() {
        if (mBanner == null) {
            return;
        }
        if (mBannerList != null && mBannerList.size() > 0) {
            if (mBanner.getVisibility() != View.VISIBLE) {
                mBanner.setVisibility(View.VISIBLE);
            }
            if (mBannerNeedUpdate) {
                mBanner.update(mBannerList);
            }
        } else {
            if (mBanner.getVisibility() != View.GONE) {
                mBanner.setVisibility(View.GONE);
            }
        }
    }


    /**
     * 初始化弹窗动画
     */
    private void initAnim() {
        final int height = mClassRecyclerViewDialog.getHeight();
        mClassRecyclerViewDialog.setTranslationY(-height);
        mShowAnimator = ObjectAnimator.ofFloat(mClassRecyclerViewDialog, "translationY", 0);
        mShowAnimator.setDuration(200);
        mHideAnimator = ObjectAnimator.ofFloat(mClassRecyclerViewDialog, "translationY", -height);
        mHideAnimator.setDuration(200);
        TimeInterpolator interpolator = new AccelerateDecelerateInterpolator();
        mShowAnimator.setInterpolator(interpolator);
        mHideAnimator.setInterpolator(interpolator);
        ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float rate = 1 + ((float) animation.getAnimatedValue() / height);
                mShadow.setAlpha(rate);
            }
        };
        mShowAnimator.addUpdateListener(updateListener);
        mHideAnimator.addUpdateListener(updateListener);
        mHideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mBtnDismiss != null && mBtnDismiss.getVisibility() == View.VISIBLE) {
                    mBtnDismiss.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    /**
     * 显示分类列表弹窗
     */
    private void showClassListDialog() {
        if (mBtnDismiss != null && mBtnDismiss.getVisibility() != View.VISIBLE) {
            mBtnDismiss.setVisibility(View.VISIBLE);
        }
        if (mShowAnimator != null) {
            mShowAnimator.start();
        }
    }


    @Override
    public void onItemClick(LiveBean bean, int position) {
        watchLive(bean, Constants.LIVE_HOME, position);
    }

    @Override
    public void loadData() {
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }

    @Override
    public void release() {
        MainHttpUtil.cancel(MainHttpConsts.GET_HOT);
        if (mHideAnimator != null) {
            mHideAnimator.cancel();
        }
        if (mShowAnimator != null) {
            mShowAnimator.cancel();
        }
        mShowAnimator = null;
        mHideAnimator = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_profit) {
            RankActivity.forward(mContext, 0);
        } else if (id == R.id.btn_con) {
            RankActivity.forward(mContext, 1);
        } else if (id == R.id.btn_more_recom) {
            LiveRecommendActivity.forward(mContext);
        }
    }
}

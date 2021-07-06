package cn.wu1588.main.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;

import com.alibaba.fastjson.JSON;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cn.wu1588.common.CommonAppConfig;
import cn.wu1588.common.Constants;
import cn.wu1588.common.bean.ConfigBean;
import cn.wu1588.common.bean.VideoClassBean;
import cn.wu1588.common.custom.CommonRefreshView;
import cn.wu1588.common.interfaces.OnItemClickListener;
import cn.wu1588.common.utils.DpUtil;
import cn.wu1588.common.utils.WordUtil;
import cn.wu1588.main.R;
import cn.wu1588.main.adapter.MainHomeVideoAdapter;
import cn.wu1588.main.adapter.MainHomeVideoClassAdapter;
import cn.wu1588.main.adapter.ReportConditionAdapter;
import cn.wu1588.main.fragment.TabFragment;
import cn.wu1588.video.bean.VideoBean;
import cn.wu1588.video.bean.VideoWithAds;
import cn.wu1588.video.event.VideoDeleteEvent;
import cn.wu1588.video.event.VideoScrollPageEvent;
import cn.wu1588.video.http.VideoHttpConsts;
import cn.wu1588.video.http.VideoHttpUtil;
import cn.wu1588.video.interfaces.VideoScrollDataHelper;


/**
 * Created by cxf on 2018/9/22.
 * 首页视频
 */

public class MainHomeVideoViewHolder extends AbsMainHomeChildViewHolder implements OnItemClickListener<VideoBean>,
        View.OnClickListener {

    private CommonRefreshView mRefreshView;
    private MainHomeVideoAdapter mAdapter;
    private VideoScrollDataHelper mVideoScrollDataHelper;
    //    private RecyclerView mClassRecyclerView;
    private MainHomeVideoClassAdapter mClassAdapter;
    private static final int ID_RECOMMEND = -1;
    private static final int ID_SHORT_VIDEO = -2;
    private int mVideoClassId = ID_RECOMMEND;
    private ViewPager viewPager;
    private List<TabFragment> fragments;
    private List<VideoClassBean> videoClassList;
    private MagicIndicator mIndicator;
    private View mBtnMore;


    public MainHomeVideoViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_main_home_video;
    }

    @Override
    public void init() {
        viewPager = findViewById(R.id.vp_video);
        View more = findViewById(R.id.btn_more);
        videoClassList = new ArrayList<>();
        videoClassList.add(new VideoClassBean(ID_SHORT_VIDEO, WordUtil.getString(R.string.short_video), VideoWithAds.ITEM_TYPE_SHORT_VIDEO, false));
        videoClassList.add(new VideoClassBean(ID_RECOMMEND, WordUtil.getString(R.string.recommend), VideoWithAds.ITEM_TYPE_LONG_VIDEO, true));
        ConfigBean configBean = CommonAppConfig.getInstance().getConfig();
        if (configBean != null) {
            List<VideoClassBean> list = JSON.parseArray(configBean.getVideoClass(), VideoClassBean.class);
            if (list != null && list.size() > 0) {
                videoClassList.addAll(list);
            }
        }


        mIndicator = (MagicIndicator) findViewById(R.id.indicator);
        mBtnMore = findViewById(R.id.btn_more);
        mBtnMore.setOnClickListener(this);

        initTabData(videoClassList);

        FragmentActivity activity = (FragmentActivity) mContext;
        viewPager.setAdapter(new FragmentStatePagerAdapter(activity.getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return fragments.get(i);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return videoClassList.get(position).getName();
            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPop(v);
            }
        });
        initIndicator();

    }

    @SuppressLint("WrongConstant")
    private void showPop(View v) {
        View pop = View.inflate(mContext, R.layout.pop_report_condition, null);
        GridView listView = pop.findViewById(R.id.lv);
        final ReportConditionAdapter adapter = new ReportConditionAdapter(videoClassList);
        listView.setAdapter(adapter);
        final PopupWindow popupWindow = new PopupWindow(pop);
        popupWindow.setWidth(AbsListView.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(AbsListView.LayoutParams.WRAP_CONTENT);
       /* if (data.size() < 10) {
        } else {
            popupWindow.setHeight(DensityUtils.getScreenH(context) / 2);
        }*/
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
        } else {
            popupWindow.showAsDropDown(v);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                viewPager.setCurrentItem(position);
                popupWindow.dismiss();
            }
        });
    }


    private void initIndicator() {
        mBtnMore.setVisibility(videoClassList != null && videoClassList.size() > 6 ? View.VISIBLE : View.GONE);

        CommonNavigator commonNavigator = new CommonNavigator(mContext);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {

            @Override
            public int getCount() {
                return videoClassList.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                SimplePagerTitleView simplePagerTitleView = new ColorTransitionPagerTitleView(context);
                simplePagerTitleView.setNormalColor(ContextCompat.getColor(context, R.color.tab_unselect));
                simplePagerTitleView.setSelectedColor(ContextCompat.getColor(context, R.color.tab_select));
                simplePagerTitleView.setText(videoClassList.get(index).getName());
                simplePagerTitleView.setTextSize(18);
                simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (viewPager != null) {
                            viewPager.setCurrentItem(index);
                        }
                    }
                });
                return simplePagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
                linePagerIndicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
                linePagerIndicator.setXOffset(DpUtil.dp2px(13));
                linePagerIndicator.setRoundRadius(DpUtil.dp2px(2));
                linePagerIndicator.setColors(ContextCompat.getColor(mContext, R.color.white));
                return linePagerIndicator;
            }
        });
        mIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(mIndicator, viewPager);
    }

    private static final String TAG = "MainHomeVideoViewHolder";

    private void initTabData(List<VideoClassBean> videoClassList) {
        fragments = new ArrayList<>();
        for (int i = 0; i < videoClassList.size(); i++) {
            TabFragment tabFragment = new TabFragment();
            int id = videoClassList.get(i).getId();
            int type = videoClassList.get(i).getType();
//            Log.e(TAG, "initTabData: "+id+", name:"+ videoClassBean.getName() );
            Bundle bundle = new Bundle();
            bundle.putInt("id", id);
            bundle.putInt("type", type);
            bundle.putString("index", String.valueOf(i));
            tabFragment.setArguments(bundle);
            fragments.add(tabFragment);
        }
    }

    @Override
    public void loadData() {
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

    @Override
    public void onItemClick(VideoBean bean, int position) {
        /*int page = 1;
        if (mRefreshView != null) {
            page = mRefreshView.getPageCount();
        }
        if (mVideoScrollDataHelper == null) {
            mVideoScrollDataHelper = new VideoScrollDataHelper() {

                @Override
                public void loadData(int p, HttpCallback callback) {
                    if (mVideoClassId == ID_RECOMMEND) {
                        VideoHttpUtil.getHomeVideoList(p, callback);
                    } else {
                        VideoHttpUtil.getHomeVideoClassList(mVideoClassId, p, callback);
                    }
                }
            };
        }
        VideoStorge.getInstance().putDataHelper(Constants.VIDEO_HOME, mVideoScrollDataHelper);
        VideoPlayActivity.forward(mContext, position, Constants.VIDEO_HOME, page);*/
    }

    @Override
    public void release() {
        VideoHttpUtil.cancel(VideoHttpConsts.GET_HOME_VIDEO_LIST);
        VideoHttpUtil.cancel(VideoHttpConsts.GET_HOME_VIDEO_CLASS_LIST);
        EventBus.getDefault().unregister(this);
        mVideoScrollDataHelper = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        release();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_more) {
        }
    }
}

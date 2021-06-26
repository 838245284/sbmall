package cn.wu1588.main.views;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import cn.wu1588.common.bean.VideoClassBean;
import cn.wu1588.main.fragment.TabTeachFragment;
import cn.wu1588.video.bean.VideoBean;
import cn.wu1588.video.http.VideoHttpConsts;
import cn.wu1588.video.http.VideoHttpUtil;
import cn.wu1588.common.interfaces.OnItemClickListener;
import cn.wu1588.common.utils.DpUtil;
import cn.wu1588.main.R;

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

import java.util.ArrayList;
import java.util.List;

/**
 * 首页 教學
 */
public class MainMallViewHolder extends AbsMainHomeChildViewHolder implements OnItemClickListener<VideoBean>, View.OnClickListener {

    private static final int ID_RECOMMEND = -1;
    private static final int ID_SHORT_VIDEO = -2;
    private ViewPager viewPager;
    private List<TabTeachFragment> fragments;
    private List<VideoClassBean> videoClassList;
    private MagicIndicator mIndicator;
    private View mBtnMore;


    public MainMallViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_main_home_video;
    }

    @Override
    public void init() {
        viewPager = findViewById(R.id.vp_video);
        /*videoClassList = new ArrayList<>();
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
        mBtnMore.setOnClickListener(this);*/

        initTabData(videoClassList);

        FragmentActivity activity = (FragmentActivity) mContext;
        List<FrameLayout> mviews = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            FrameLayout frameLayout = new FrameLayout(mContext);
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mviews.add(frameLayout);
        }
        viewPager.setAdapter(new MyViewPagerAdapter(mviews));
        /*viewPager.setAdapter(new FragmentPagerAdapter(activity.getSupportFragmentManager()) {
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
                return "pos"+position;
            }
        });*/
//        initIndicator();

    }

    public class MyViewPagerAdapter extends PagerAdapter {
        private List<FrameLayout> mListViews;
        public MyViewPagerAdapter(List<FrameLayout> mListViews) {
            this.mListViews = mListViews;//构造方法，参数是我们的页卡，这样比较方便。
        }
        //直接继承PagerAdapter，至少必须重写下面的四个方法，否则会报错
        @Override
        public void destroyItem(ViewGroup container, int position, Object object)  {
            container.removeView(mListViews.get(position));//删除页卡
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position){
            //这个方法用来实例化页卡
            container.addView(mListViews.get(position), 0);//添加页卡
            return mListViews.get(position);
        }
        @Override
        public int getCount() {
            return  mListViews.size();//返回页卡的数量
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0==arg1;//官方提示这样写
        }
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
                simplePagerTitleView.setTextSize(14);
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
        for (int i = 0; i < 3; i++) {
            TabTeachFragment tabFragment = new TabTeachFragment();
            /*int id = videoClassList.get(i).getId();
            int type = videoClassList.get(i).getType();
//            Log.e(TAG, "initTabData: "+id+", name:"+ videoClassBean.getName() );
            Bundle bundle = new Bundle();
            bundle.putInt("id", id);
            bundle.putInt("type", type);
            bundle.putString("index", String.valueOf(i));
            tabFragment.setArguments(bundle);*/
            fragments.add(tabFragment);
        }
    }

    @Override
    public void loadData() {
    }

   /* @Subscribe(threadMode = ThreadMode.MAIN)
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
    }*/

    @Override
    public void onItemClick(VideoBean bean, int position) {
    }

    @Override
    public void release() {
        VideoHttpUtil.cancel(VideoHttpConsts.GET_HOME_VIDEO_LIST);
        VideoHttpUtil.cancel(VideoHttpConsts.GET_HOME_VIDEO_CLASS_LIST);
        EventBus.getDefault().unregister(this);
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

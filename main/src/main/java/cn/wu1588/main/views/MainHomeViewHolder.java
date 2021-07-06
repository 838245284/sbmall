package cn.wu1588.main.views;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import cn.wu1588.common.custom.HomeIndicatorTitle;
import cn.wu1588.common.utils.WordUtil;
import cn.wu1588.main.R;

/**
 * Created by cxf on 2018/9/22.
 * MainActivity 首页
 */

public class MainHomeViewHolder extends AbsMainHomeParentViewHolder {

    private MainHomeFollowViewHolder mFollowViewHolder;
    private MainHomeLiveViewHolder mLiveViewHolder;
    private MainHomeVideoViewHolder mVideoViewHolder;
    public int type;
    public static final int TYPE_HOMR = 0;
    public static final int TYPE_TEACH = 1;
    private MainTeachVideoViewHolder mVideoTeachViewHolder;

    public MainHomeViewHolder(Context context, ViewGroup parentView,int type) {
        super(context, parentView);
        this.type = type;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_main_home;
    }

    @Override
    public void init() {
        setStatusHeight();
        super.init();

    }

    @Override
    protected void loadPageData(int position) {
        if (mViewHolders == null) {
            return;
        }
        AbsMainHomeChildViewHolder vh = mViewHolders[position];
        if (vh == null) {
            if (mViewList != null && position < mViewList.size()) {
                FrameLayout parent = mViewList.get(position);
                if (parent == null) {
                    return;
                }
                if (position == 0) {
                    if(type==0){
                        mVideoViewHolder = new MainHomeVideoViewHolder(mContext, parent);
                        vh = mVideoViewHolder;
                    }else{
                        mVideoTeachViewHolder = new MainTeachVideoViewHolder(mContext,parent);
                        vh = mVideoTeachViewHolder;
                    }
                } else if (position == 1) {
                    mFollowViewHolder = new MainHomeFollowViewHolder(mContext, parent);
                    vh = mFollowViewHolder;
                } else if (position == 2) {
                    mLiveViewHolder = new MainHomeLiveViewHolder(mContext, parent);
                    vh = mLiveViewHolder;
                }
                if (vh == null) {
                    return;
                }
                mViewHolders[position] = vh;
                vh.addToParent();
                vh.subscribeActivityLifeCycle();
            }
        }
        if (vh != null) {
            vh.loadData();
        }
    }

    @Override
    protected int getPageCount() {
        return 3;
    }

    @Override
    protected String[] getTitles() {
        return new String[]{
                WordUtil.getString(R.string.video),
                WordUtil.getString(R.string.follow),
                WordUtil.getString(R.string.live),
        };
    }


    @Override
    protected IPagerTitleView getIndicatorTitleView(Context context, String[] titles, final int index) {
        HomeIndicatorTitle indicatorTitle = new HomeIndicatorTitle(mContext);
        SimplePagerTitleView simplePagerTitleView = new ColorTransitionPagerTitleView(context);
        simplePagerTitleView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        simplePagerTitleView.setNormalColor(ContextCompat.getColor(mContext, R.color.gray1));
        simplePagerTitleView.setSelectedColor(ContextCompat.getColor(mContext, R.color.textColor));
        simplePagerTitleView.setText(titles[index]);
        simplePagerTitleView.setTextSize(18);
        simplePagerTitleView.getPaint().setFakeBoldText(true);
        indicatorTitle.addView(simplePagerTitleView);
        indicatorTitle.setTitleView(simplePagerTitleView);
        indicatorTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mViewPager != null) {
                    mViewPager.setCurrentItem(index);
                }
            }
        });

        return indicatorTitle;
    }

}

package cn.wu1588.main.views;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.wu1588.common.CommonAppConfig;
import cn.wu1588.common.HtmlConfig;
import cn.wu1588.common.activity.WebViewActivity;
import cn.wu1588.common.bean.LevelBean;
import cn.wu1588.common.bean.UserBean;
import cn.wu1588.common.bean.UserItemBean;
import cn.wu1588.common.glide.ImgLoader;
import cn.wu1588.common.interfaces.CommonCallback;
import cn.wu1588.common.interfaces.OnItemClickListener;
import cn.wu1588.common.utils.CommonIconUtil;
import cn.wu1588.common.utils.RouteUtil;
import cn.wu1588.common.utils.StringUtil;
import cn.wu1588.im.activity.ChatActivity;
import cn.wu1588.live.activity.LiveRecordActivity;
import cn.wu1588.live.activity.RoomManageActivity;
import cn.wu1588.main.R;
import cn.wu1588.main.activity.DailyTaskActivity;
import cn.wu1588.main.activity.EditProfileActivity;
import cn.wu1588.main.activity.FansActivity;
import cn.wu1588.main.activity.FollowActivity;
import cn.wu1588.main.activity.MainActivity;
import cn.wu1588.main.activity.MallActivity;
import cn.wu1588.main.activity.MyActiveActivity;
import cn.wu1588.main.activity.MyProfitActivity;
import cn.wu1588.main.activity.MyVideoActivity;
import cn.wu1588.main.activity.SettingActivity;
import cn.wu1588.main.activity.ThreeDistributActivity;
import cn.wu1588.main.http.MainHttpConsts;
import cn.wu1588.main.http.MainHttpUtil;
import cn.wu1588.mall.activity.GoodsCollectActivity;
import cn.wu1588.mall.activity.PayContentActivity1;
import cn.wu1588.mall.activity.PayContentActivity2;

/**
 * Created by cxf on 2018/9/22.
 * 我的
 */

public class MainMeViewHolder extends AbsMainViewHolder implements OnItemClickListener<UserItemBean>, View.OnClickListener {


    private ImageView mAvatar;
    private TextView mName;
    private ImageView mSex;
    private TextView mID;
    private TextView mFollow;
    private TextView mFans;
    private TextView mCllocet;
    private boolean mPaused;
    private List<UserItemBean> list;

    public MainMeViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_me;
    }

    @Override
    public void init() {

        mAvatar = (ImageView) findViewById(R.id.avatar);
        mName = (TextView) findViewById(R.id.name);
        mSex = (ImageView) findViewById(R.id.sex);
        mID = (TextView) findViewById(R.id.id_val);
        mFollow = (TextView) findViewById(R.id.btn_follow);
        mFans = (TextView) findViewById(R.id.btn_fans);
        mCllocet = (TextView) findViewById(R.id.btn_collect);
        findViewById(R.id.fans).setOnClickListener(this);
        findViewById(R.id.follow).setOnClickListener(this);
        findViewById(R.id.collect).setOnClickListener(this);
        findViewById(R.id.edit).setOnClickListener(this);
        findViewById(R.id.msg).setOnClickListener(this);
        findViewById(R.id.wallet).setOnClickListener(this);
        findViewById(R.id.mingxi).setOnClickListener(this);
        findViewById(R.id.daoju).setOnClickListener(this);
        findViewById(R.id.myvideo).setOnClickListener(this);
        findViewById(R.id.my_dongtai).setOnClickListener(this);
        findViewById(R.id.my_shouyi).setOnClickListener(this);
        findViewById(R.id.my_renzheng).setOnClickListener(this);
        findViewById(R.id.my_risk).setOnClickListener(this);
        findViewById(R.id.pay_content).setOnClickListener(this);
        findViewById(R.id.my_xiaodian).setOnClickListener(this);
        findViewById(R.id.buycenter).setOnClickListener(this);
        findViewById(R.id.room_manage).setOnClickListener(this);
        findViewById(R.id.zhuangbeicenter).setOnClickListener(this);
        findViewById(R.id.mylevel).setOnClickListener(this);
        findViewById(R.id.invite_award).setOnClickListener(this);
        findViewById(R.id.person_setting).setOnClickListener(this);
        findViewById(R.id.shopmall).setOnClickListener(this);
        findViewById(R.id.familicenter).setOnClickListener(this);
        findViewById(R.id.online).setOnClickListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isShowed() && mPaused) {
            loadData();
        }
        mPaused = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MainHttpUtil.cancel(MainHttpConsts.GET_BASE_INFO);
    }

    @Override
    public void loadData() {
        if (isFirstLoadData()) {
            CommonAppConfig appConfig = CommonAppConfig.getInstance();
            UserBean u = appConfig.getUserBean();
            List<UserItemBean> list = appConfig.getUserItemList();
            if (u != null && list != null) {
                showData(u, list);
            }
        }
        MainHttpUtil.getBaseInfo(mCallback);
    }

    private CommonCallback<UserBean> mCallback = new CommonCallback<UserBean>() {
        @Override
        public void callback(UserBean bean) {
            List<UserItemBean> list = CommonAppConfig.getInstance().getUserItemList();
            if (bean != null) {
                showData(bean, list);
            }
        }
    };

    private void showData(UserBean u, List<UserItemBean> list) {
        this.list = list;
        ImgLoader.displayAvatar(mContext, u.getAvatar(), mAvatar);
        mName.setText(u.getUserNiceName());
        mSex.setImageResource(CommonIconUtil.getSexIcon(u.getSex()));
        CommonAppConfig appConfig = CommonAppConfig.getInstance();
        LevelBean anchorLevelBean = appConfig.getAnchorLevel(u.getLevelAnchor());
        /*if (anchorLevelBean != null) {
            ImgLoader.display(mContext, anchorLevelBean.getThumb(), mLevelAnchor);
        }
        LevelBean levelBean = appConfig.getLevel(u.getLevel());
        if (levelBean != null) {
            ImgLoader.display(mContext, levelBean.getThumb(), mLevel);
        }*/
        mID.setText(u.getLiangNameTip());
        mFollow.setText(StringUtil.toWan(u.getFollows()));
        mFans.setText(StringUtil.toWan(u.getFans()));
    }


    @Override
    public void onItemClick(UserItemBean bean, int position) {
    }

    private void toWeb(int id){
        for (int i = 0; i < list.size(); i++) {
            UserItemBean bean = null;
            if(list.get(i).getId()==id){
                bean = list.get(i);
            }else{
                continue;
            }
            String url = bean.getHref();
            if (!TextUtils.isEmpty(url)) {
                if (!url.contains("?")) {
                    url = StringUtil.contact(url, "?");
                }
                if (bean.getId() == 8) {//三级分销
                    ThreeDistributActivity.forward(mContext, bean.getName(), url);
                } else {
                    WebViewActivity.forward(mContext, url);
                }
            }
        }
    }

    /**
     * 我的小店 商城
     */
    private void forwardMall() {
        RouteUtil.forward(RouteUtil.PATH_MALL_BUYER);
    }

    private void forwardSell() {
        RouteUtil.forward(RouteUtil.PATH_MALL_SELLER);
       /* UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u != null) {
            if (u.getIsOpenShop() == 0) {

            } else {
                RouteUtil.forward(RouteUtil.PATH_MALL_SELLER);
            }
        }*/

    }


    /**
     * 付费内容
     */
    private void forwardPayContent() {
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u != null) {
            if (u.getIsOpenPayContent() == 0) {
                PayContentActivity1.forward(mContext);
            } else {
                PayContentActivity2.forward(mContext);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.edit) {
            RouteUtil.forwardUserHome(mContext, CommonAppConfig.getInstance().getUid());
        } else if (i == R.id.follow) {
            forwardFollow();
        } else if (i == R.id.fans) {
            forwardFans();
        } else if (i == R.id.msg) {
            ChatActivity.forward(mContext);
        }else if(i == R.id.collect){
            mContext.startActivity(new Intent(mContext, GoodsCollectActivity.class));
        } else if (i == R.id.wallet) {
            RouteUtil.forwardMyCoin(mContext);
        } else if (i == R.id.mingxi) {
            WebViewActivity.forward(mContext, HtmlConfig.DETAIL);
        } else if (i == R.id.daoju) {
            WebViewActivity.forward(mContext, HtmlConfig.SHOP);
        }else if(i == R.id.my_dongtai){
            ((MainActivity)mContext).getLocation();
            mContext.startActivity(new Intent(mContext, MyActiveActivity.class));
        }else if(i == R.id.myvideo){
            forwardMyVideo();
        }else if(i == R.id.my_shouyi){
            forwardProfit();
        }else if(i == R.id.my_renzheng){
            toWeb(11);
        }else if(i == R.id.my_risk){
            mContext.startActivity(new Intent(mContext, DailyTaskActivity.class));
        }else if(i == R.id.pay_content){
            forwardPayContent();
        }else if(i == R.id.my_xiaodian){
            forwardSell();
        }else if(i == R.id.room_manage){
            forwardRoomManage();
        }else if(i == R.id.zhuangbeicenter){
            toWeb(5);
        }else if(i == R.id.mylevel){
            toWeb(3);
        }else if(i == R.id.invite_award){
            toWeb(8);
        }else if(i == R.id.person_setting){
            forwardSetting();
        }else if(i == R.id.buycenter){
            forwardMall();
        }else if(i == R.id.familicenter){
            toWeb(6);
        }else if(i == R.id.online){
            toWeb(21);
        }else if(i == R.id.shopmall){
            forwardMallActivity();
        }
    }

    /**
     * 编辑个人资料
     */
    private void forwardEditProfile() {
        mContext.startActivity(new Intent(mContext, EditProfileActivity.class));
    }

    /**
     * 我的关注
     */
    private void forwardFollow() {
        FollowActivity.forward(mContext, CommonAppConfig.getInstance().getUid());
    }

    /**
     * 我的粉丝
     */
    private void forwardFans() {
        FansActivity.forward(mContext, CommonAppConfig.getInstance().getUid());
    }

    private void forwardMallActivity() {
        MallActivity.forward(mContext);
    }

    /**
     * 直播记录
     */
    private void forwardLiveRecord() {
        LiveRecordActivity.forward(mContext, CommonAppConfig.getInstance().getUserBean());
    }

    /**
     * 我的收益
     */
    private void forwardProfit() {
        mContext.startActivity(new Intent(mContext, MyProfitActivity.class));
    }

    /**
     * 我的钻石
     */
    private void forwardCoin() {
        RouteUtil.forwardMyCoin(mContext);
    }

    /**
     * 设置
     */
    private void forwardSetting() {
        mContext.startActivity(new Intent(mContext, SettingActivity.class));
    }

    /**
     * 我的视频
     */
    private void forwardMyVideo() {
        mContext.startActivity(new Intent(mContext, MyVideoActivity.class));
//        mContext.startActivity(new Intent(mContext, BannerListActivity.class));
    }

    /**
     * 房间管理
     */
    private void forwardRoomManage() {
        mContext.startActivity(new Intent(mContext, RoomManageActivity.class));
    }


}

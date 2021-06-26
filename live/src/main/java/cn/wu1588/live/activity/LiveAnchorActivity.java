package cn.wu1588.live.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.opensource.svgaplayer.SVGAImageView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

import cn.wu1588.beauty.simple.SimpleBeautyViewHolder;
import cn.wu1588.beauty.ui.interfaces.IBeautyViewHolder;
import cn.wu1588.beauty.ui.interfaces.StickerCanClickListener;
import cn.wu1588.beauty.ui.views.BaseBeautyViewHolder;
import cn.wu1588.beauty.ui.views.BeautyViewHolderFactory;
import cn.wu1588.common.CommonAppConfig;
import cn.wu1588.common.Constants;
import cn.wu1588.common.bean.GoodsBean;
import cn.wu1588.common.bean.UserBean;
import cn.wu1588.common.dialog.NotCancelableDialog;
import cn.wu1588.common.event.LoginInvalidEvent;
import cn.wu1588.common.http.CommonHttpConsts;
import cn.wu1588.common.http.CommonHttpUtil;
import cn.wu1588.common.http.HttpCallback;
import cn.wu1588.common.utils.DateFormatUtil;
import cn.wu1588.common.utils.DialogUitl;
import cn.wu1588.common.utils.L;
import cn.wu1588.common.utils.LogUtil;
import cn.wu1588.common.utils.RouteUtil;
import cn.wu1588.common.utils.ToastUtil;
import cn.wu1588.common.utils.WordUtil;
import cn.wu1588.game.bean.GameParam;
import cn.wu1588.game.dialog.GameDialogFragment;
import cn.wu1588.game.event.GameWindowChangedEvent;
import cn.wu1588.game.util.GamePresenter;
import cn.wu1588.im.utils.ImMessageUtil;
import cn.wu1588.im.utils.ImPushUtil;
import cn.wu1588.live.R;
import cn.wu1588.live.bean.LiveBean;
import cn.wu1588.live.bean.LiveConfigBean;
import cn.wu1588.live.bean.LiveGuardInfo;
import cn.wu1588.live.bean.LiveReceiveGiftBean;
import cn.wu1588.live.dialog.LiveFunctionDialogFragment;
import cn.wu1588.live.dialog.LiveLinkMicListDialogFragment;
import cn.wu1588.live.dialog.LiveShopDialogFragment;
import cn.wu1588.live.event.LinkMicTxMixStreamEvent;
import cn.wu1588.live.http.LiveHttpConsts;
import cn.wu1588.live.http.LiveHttpUtil;
import cn.wu1588.live.interfaces.LiveFunctionClickListener;
import cn.wu1588.live.interfaces.LivePushListener;
import cn.wu1588.live.music.LiveMusicDialogFragment;
import cn.wu1588.live.presenter.LiveLinkMicAnchorPresenter;
import cn.wu1588.live.presenter.LiveLinkMicPkPresenter;
import cn.wu1588.live.presenter.LiveLinkMicPresenter;
import cn.wu1588.live.socket.GameActionListenerImpl;
import cn.wu1588.live.socket.SocketChatUtil;
import cn.wu1588.live.socket.SocketClient;
import cn.wu1588.live.views.AbsLivePushViewHolder;
import cn.wu1588.live.views.LiveAnchorViewHolder;
import cn.wu1588.live.views.LiveEndViewHolder;
import cn.wu1588.live.views.LiveGoodsAddViewHolder;
import cn.wu1588.live.views.LiveMusicViewHolder;
import cn.wu1588.live.views.LivePlatGoodsAddViewHolder;
import cn.wu1588.live.views.LivePushKsyViewHolder;
import cn.wu1588.live.views.LivePushTxViewHolder;
import cn.wu1588.live.views.LiveReadyViewHolder;
import cn.wu1588.live.views.LiveRoomViewHolder;
import pl.droidsonroids.gif.GifImageView;


/**
 * Created by cxf on 2018/10/7.
 * 主播直播间
 */

public class LiveAnchorActivity extends LiveActivity implements LiveFunctionClickListener {

    private static final String TAG = "LiveAnchorActivity";
    private LiveGoodsAddViewHolder mLiveGoodsAddViewHolder;
    private LivePlatGoodsAddViewHolder mLivePlatGoodsAddViewHolder;

    public static void forward(Context context, int liveSdk, LiveConfigBean bean, int haveStore) {
        Intent intent = new Intent(context, LiveAnchorActivity.class);
        intent.putExtra(Constants.LIVE_SDK, liveSdk);
        intent.putExtra(Constants.LIVE_CONFIG, bean);
        intent.putExtra(Constants.HAVE_STORE, haveStore);
        context.startActivity(intent);
    }

    private ViewGroup mRoot;
    private ViewGroup mContainerWrap;
    private AbsLivePushViewHolder mLivePushViewHolder;
    private LiveReadyViewHolder mLiveReadyViewHolder;
    private IBeautyViewHolder mLiveBeautyViewHolder;
    private LiveAnchorViewHolder mLiveAnchorViewHolder;
    private LiveMusicViewHolder mLiveMusicViewHolder;
    private boolean mStartPreview;//是否开始预览
    private boolean mStartLive;//是否开始了直播
    private List<Integer> mGameList;//游戏开关
    private boolean mBgmPlaying;//是否在播放背景音乐
    private LiveConfigBean mLiveConfigBean;
    private HttpCallback mCheckLiveCallback;
    private File mLogFile;
    private int mReqCount;
    private boolean mPaused;
    private boolean mNeedCloseLive = true;
    private boolean mSuperCloseLive;//是否是超管关闭直播
    private boolean mLoginInvalid;//登录是否失效
    private boolean mEnd;
    private LiveLinkMicListDialogFragment mLiveLinkMicListDialogFragment;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_live_anchor;
    }

    @Override
    protected void main() {
        super.main();
        Intent intent = getIntent();
        mLiveSDK = intent.getIntExtra(Constants.LIVE_SDK, Constants.LIVE_SDK_KSY);
        mLiveConfigBean = intent.getParcelableExtra(Constants.LIVE_CONFIG);
        int haveStore = intent.getIntExtra(Constants.HAVE_STORE, 0);


        L.e(TAG, "直播sdk----->" + (mLiveSDK == Constants.LIVE_SDK_KSY ? "金山云" : "腾讯云"));
        mRoot = (ViewGroup) findViewById(R.id.root);
        mSocketUserType = Constants.SOCKET_USER_TYPE_ANCHOR;
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        mLiveUid = u.getId();
        mLiveBean = new LiveBean();
        mLiveBean.setUid(mLiveUid);
        mLiveBean.setUserNiceName(u.getUserNiceName());
        mLiveBean.setAvatar(u.getAvatar());
        mLiveBean.setAvatarThumb(u.getAvatarThumb());
        mLiveBean.setLevelAnchor(u.getLevelAnchor());
        mLiveBean.setGoodNum(u.getGoodName());
        mLiveBean.setCity(u.getCity());

        if (mLiveSDK == Constants.LIVE_SDK_TX) {
            //添加推流预览控件
            mLivePushViewHolder = new LivePushTxViewHolder(mContext, (ViewGroup) findViewById(R.id.preview_container), mLiveConfigBean);
        } else {
            mLivePushViewHolder = new LivePushKsyViewHolder(mContext, (ViewGroup) findViewById(R.id.preview_container), mLiveConfigBean);
        }
        mLivePushViewHolder.setLivePushListener(new LivePushListener() {
            @Override
            public void onPreviewStart() {
                //开始预览回调
                mStartPreview = true;
            }

            @Override
            public void onPushStart() {
                //开始推流回调
                LiveHttpUtil.changeLive(mStream);
            }

            @Override
            public void onPushFailed() {
                //推流失败回调
                ToastUtil.show(R.string.live_push_failed);
            }
        });
        mLivePushViewHolder.addToParent();
        mLivePushViewHolder.subscribeActivityLifeCycle();
        mContainerWrap = (ViewGroup) findViewById(R.id.container_wrap);
        mContainer = (ViewGroup) findViewById(R.id.container);
        //添加开播前设置控件
        mLiveReadyViewHolder = new LiveReadyViewHolder(mContext, mContainer, mLiveSDK, haveStore);
        mLiveReadyViewHolder.addToParent();
        mLiveReadyViewHolder.subscribeActivityLifeCycle();
        mLiveLinkMicPresenter = new LiveLinkMicPresenter(mContext, mLivePushViewHolder, true, mLiveSDK, mContainer);
        mLiveLinkMicPresenter.setLiveUid(mLiveUid);
        mLiveLinkMicAnchorPresenter = new LiveLinkMicAnchorPresenter(mContext, mLivePushViewHolder, true, mLiveSDK, mContainer);
        mLiveLinkMicPkPresenter = new LiveLinkMicPkPresenter(mContext, mLivePushViewHolder, true, mContainer);
    }

    public boolean isStartPreview() {
        return mStartPreview;
    }

    /**
     * 主播直播间功能按钮点击事件
     *
     * @param functionID
     */
    @Override
    public void onClick(int functionID) {
        switch (functionID) {
            case Constants.LIVE_FUNC_BEAUTY://美颜
                beauty();
                break;
            case Constants.LIVE_FUNC_CAMERA://切换镜头
                toggleCamera();
                break;
            case Constants.LIVE_FUNC_FLASH://切换闪光灯
                toggleFlash();
                break;
            case Constants.LIVE_FUNC_MUSIC://伴奏
                openMusicWindow();
                break;
            case Constants.LIVE_FUNC_SHARE://分享
                openShareWindow();
                break;
            case Constants.LIVE_FUNC_GAME://游戏
                openGameWindow();
                break;
            case Constants.LIVE_FUNC_RED_PACK://红包
                openRedPackSendWindow();
                break;
            case Constants.LIVE_FUNC_LINK_MIC://连麦
                openLinkMicAnchorWindow();
                break;
            case Constants.LIVE_FUNC_MIRROR://镜像
                togglePushMirror();
                break;
        }
    }

    /**
     * 切换镜像
     */
    private void togglePushMirror() {
        if (mLivePushViewHolder != null) {
            mLivePushViewHolder.togglePushMirror();
        }
    }


    public void openShop(boolean isOpen) {
        if (mLiveAnchorViewHolder != null) {
            mLiveAnchorViewHolder.setShopBtnVisible(isOpen);
        }
    }


    //打开相机前执行
    public void beforeCamera() {
        if (mLivePushViewHolder != null) {
            mLivePushViewHolder.setOpenCamera(true);
        }
    }


    /**
     * 切换镜头
     */
    public void toggleCamera() {
        if (mLivePushViewHolder != null) {
            mLivePushViewHolder.toggleCamera();
        }
    }

    /**
     * 切换闪光灯
     */
    public void toggleFlash() {
        if (mLivePushViewHolder != null) {
            mLivePushViewHolder.toggleFlash();
        }
    }

    /**
     * 设置美颜
     */

    public void beauty() {
        if (mLiveBeautyViewHolder == null) {
            if (!CommonAppConfig.getInstance().isTiBeautyEnable()) {
                mLiveBeautyViewHolder = new SimpleBeautyViewHolder(mContext, mRoot);
                mLiveBeautyViewHolder.setEffectListener(mLivePushViewHolder.getSimpleBeautyEffectListener());
            } else {
                mLiveBeautyViewHolder = BeautyViewHolderFactory.getBeautyViewHolder(mContext, mRoot, new StickerCanClickListener() {
                    @Override
                    public boolean canClick() {
                        if (mLivePushViewHolder != null && mLivePushViewHolder instanceof LivePushTxViewHolder) {
                            return !((LivePushTxViewHolder) mLivePushViewHolder).isPlayGiftSticker();
                        }
                        return true;
                    }
                });
                if (mLiveBeautyViewHolder != null && mLiveBeautyViewHolder instanceof BaseBeautyViewHolder) {
                    ((BaseBeautyViewHolder) mLiveBeautyViewHolder).setMhBeautyManager(mLivePushViewHolder.getMhSDKManager());
                    mLiveBeautyViewHolder.setEffectListener(mLivePushViewHolder.getDefaultEffectListener());
                }
            }
            mLiveBeautyViewHolder.setVisibleListener(new IBeautyViewHolder.VisibleListener() {
                @Override
                public void onVisibleChanged(boolean visible) {
                    if (mLiveReadyViewHolder != null) {
                        if (visible) {
                            mLiveReadyViewHolder.hide();
                        } else {
                            mLiveReadyViewHolder.show();
                        }
                    }
                }
            });
        }

        mLiveBeautyViewHolder.show();
    }

    /**
     * 飘心
     */
    public void light() {
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.playLightAnim();
        }
    }

    /**
     * 打开音乐窗口
     */
    private void openMusicWindow() {
        if (isLinkMic() || isLinkMicAnchor()) {
            ToastUtil.show(R.string.link_mic_not_bgm);
            return;
        }
        LiveMusicDialogFragment fragment = new LiveMusicDialogFragment();
        fragment.setLifeCycleListener(this);
        fragment.setActionListener(new LiveMusicDialogFragment.ActionListener() {
            @Override
            public void onChoose(String musicId) {
                if (mLivePushViewHolder != null) {
                    if (mLiveMusicViewHolder == null) {
                        mLiveMusicViewHolder = new LiveMusicViewHolder(mContext, mContainer, mLivePushViewHolder);
                        mLiveMusicViewHolder.subscribeActivityLifeCycle();
                        mLiveMusicViewHolder.addToParent();
                    }
                    mLiveMusicViewHolder.play(musicId);
                    mBgmPlaying = true;
                }
            }
        });
        fragment.show(getSupportFragmentManager(), "LiveMusicDialogFragment");
    }

    /**
     * 关闭背景音乐
     */
    public void stopBgm() {
        if (mLiveMusicViewHolder != null) {
            mLiveMusicViewHolder.release();
        }
        mLiveMusicViewHolder = null;
        mBgmPlaying = false;
    }

    public boolean isBgmPlaying() {
        return mBgmPlaying;
    }


    /**
     * 打开功能弹窗
     */
    public void showFunctionDialog() {
        LiveFunctionDialogFragment fragment = new LiveFunctionDialogFragment();
        fragment.setLifeCycleListener(this);
        Bundle bundle = new Bundle();
        boolean hasGame = false;
        if (CommonAppConfig.GAME_ENABLE && mGameList != null) {
            hasGame = mGameList.size() > 0;
        }
        bundle.putBoolean(Constants.HAS_GAME, hasGame);
        bundle.putBoolean(Constants.OPEN_FLASH, mLivePushViewHolder != null && mLivePushViewHolder.isFlashOpen());
        fragment.setArguments(bundle);
        fragment.setFunctionClickListener(this);
        fragment.show(getSupportFragmentManager(), "LiveFunctionDialogFragment");
    }

    public void hideLinkMicAnchorWindow() {
        if (mLiveLinkMicListDialogFragment != null) {
            mLiveLinkMicListDialogFragment.dismissAllowingStateLoss();
        }
        mLiveLinkMicListDialogFragment = null;
    }

    public void hideLinkMicAnchorWindow2() {
        mLiveLinkMicListDialogFragment = null;
    }

    /**
     * 打开主播连麦窗口
     */
    private void openLinkMicAnchorWindow() {
        if (mLiveLinkMicAnchorPresenter != null && !mLiveLinkMicAnchorPresenter.canOpenLinkMicAnchor()) {
            return;
        }
        LiveLinkMicListDialogFragment fragment = new LiveLinkMicListDialogFragment();
        fragment.setLifeCycleListener(this);
        mLiveLinkMicListDialogFragment = fragment;
        fragment.show(getSupportFragmentManager(), "LiveLinkMicListDialogFragment");
    }


    /**
     * 打开选择游戏窗口
     */
    private void openGameWindow() {
        if (isLinkMic() || isLinkMicAnchor()) {
            ToastUtil.show(R.string.live_link_mic_cannot_game);
            return;
        }
        if (mGamePresenter != null) {
            GameDialogFragment fragment = new GameDialogFragment();
            fragment.setLifeCycleListener(this);
            fragment.setGamePresenter(mGamePresenter);
            fragment.show(getSupportFragmentManager(), "GameDialogFragment");
        }
    }

    /**
     * 关闭游戏
     */
    public void closeGame() {
        if (mGamePresenter != null) {
            mGamePresenter.closeGame();
        }
    }

    /**
     * 开播成功
     *
     * @param data createRoom返回的数据
     */
    public void startLiveSuccess(String data, int liveType, int liveTypeVal, String title) {
        mLiveType = liveType;
        mLiveTypeVal = liveTypeVal;
        if (mLiveBean != null) {
            mLiveBean.setTitle(title);
        }
        //处理createRoom返回的数据
        JSONObject obj = JSON.parseObject(data);
        mStream = obj.getString("stream");
        mDanmuPrice = obj.getString("barrage_fee");
        String playUrl = obj.getString("pull");
        L.e("createRoom----播放地址--->" + playUrl);
        mLiveBean.setPull(playUrl);
        mTxAppId = obj.getString("tx_appid");
        //移除开播前的设置控件，添加直播间控件
        if (mLiveReadyViewHolder != null) {
            mLiveReadyViewHolder.removeFromParent();
            mLiveReadyViewHolder.release();
        }
        mLiveReadyViewHolder = null;
        if (mLiveRoomViewHolder == null) {
            mLiveRoomViewHolder = new LiveRoomViewHolder(mContext, mContainer, (GifImageView) findViewById(R.id.gift_gif), (SVGAImageView) findViewById(R.id.gift_svga), mContainerWrap);
            mLiveRoomViewHolder.addToParent();
            mLiveRoomViewHolder.subscribeActivityLifeCycle();
            mLiveRoomViewHolder.setLiveInfo(mLiveUid, mStream, obj.getIntValue("userlist_time") * 1000);
            mLiveRoomViewHolder.setVotes(obj.getString("votestotal"));
            UserBean u = CommonAppConfig.getInstance().getUserBean();
            if (u != null) {
                mLiveRoomViewHolder.setRoomNum(u.getLiangNameTip());
                mLiveRoomViewHolder.setName(u.getUserNiceName());
                mLiveRoomViewHolder.setAvatar(u.getAvatar());
                mLiveRoomViewHolder.setAnchorLevel(u.getLevelAnchor());
            }
            mLiveRoomViewHolder.startAnchorLight();
        }
        if (mLiveAnchorViewHolder == null) {
            mLiveAnchorViewHolder = new LiveAnchorViewHolder(mContext, mContainer);
            mLiveAnchorViewHolder.addToParent();
            mLiveAnchorViewHolder.setUnReadCount(((LiveActivity) mContext).getImUnReadCount());
        }
        mLiveBottomViewHolder = mLiveAnchorViewHolder;

        //连接socket
        if (mSocketClient == null) {
            mSocketClient = new SocketClient(obj.getString("chatserver"), this);
            if (mLiveLinkMicPresenter != null) {
                mLiveLinkMicPresenter.setSocketClient(mSocketClient);
            }
            if (mLiveLinkMicAnchorPresenter != null) {
                mLiveLinkMicAnchorPresenter.setSocketClient(mSocketClient);
                mLiveLinkMicAnchorPresenter.setPlayUrl(playUrl);
                mLiveLinkMicAnchorPresenter.setSelfStream(mStream);
            }
            if (mLiveLinkMicPkPresenter != null) {
                mLiveLinkMicPkPresenter.setSocketClient(mSocketClient);
                mLiveLinkMicPkPresenter.setLiveUid(mLiveUid);
                mLiveLinkMicPkPresenter.setSelfStream(mStream);
            }
        }
        mSocketClient.connect(mLiveUid, mStream);

        //开始推流
        if (mLivePushViewHolder != null) {
            mLivePushViewHolder.startPush(obj.getString("push"));
        }
        //开始显示直播时长
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.startAnchorLiveTime();
            mLiveRoomViewHolder.startAnchorCheckLive();
        }
        mStartLive = true;
        mLiveRoomViewHolder.startRefreshUserList();

        //守护相关
        mLiveGuardInfo = new LiveGuardInfo();
        int guardNum = obj.getIntValue("guard_nums");
        mLiveGuardInfo.setGuardNum(guardNum);
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.setGuardNum(guardNum);
        }

        //奖池等级
        int giftPrizePoolLevel = obj.getIntValue("jackpot_level");
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.showBtn(false, giftPrizePoolLevel);
        }
        //游戏相关
        if (CommonAppConfig.GAME_ENABLE) {
            mGameList = JSON.parseArray(obj.getString("game_switch"), Integer.class);
            GameParam param = new GameParam();
            param.setContext(mContext);
            param.setParentView(mContainerWrap);
            param.setTopView(mContainer);
            param.setInnerContainer(mLiveRoomViewHolder.getInnerContainer());
            param.setGameActionListener(new GameActionListenerImpl(LiveAnchorActivity.this, mSocketClient));
            param.setLiveUid(mLiveUid);
            param.setStream(mStream);
            param.setAnchor(true);
            param.setCoinName(CommonAppConfig.getInstance().getScoreName());
            param.setObj(obj);
            mGamePresenter = new GamePresenter(param);
            mGamePresenter.setGameList(mGameList);
        }
    }

    /**
     * 关闭直播
     */
    public void closeLive() {
        DialogUitl.showSimpleDialog(mContext, WordUtil.getString(R.string.live_end_live), new DialogUitl.SimpleCallback() {
            @Override
            public void onConfirmClick(Dialog dialog, String content) {
                endLive();
            }
        });
    }


    /**
     * 结束直播
     */
    public void endLive() {
        if (mEnd) {
            return;
        }
        mEnd = true;
        //请求关播的接口
        LiveHttpUtil.stopLive(mStream, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    //断开socket
                    if (mSocketClient != null) {
                        mSocketClient.disConnect();
                        mSocketClient = null;
                    }

                    if (mLiveEndViewHolder == null) {
                        mLiveEndViewHolder = new LiveEndViewHolder(mContext, mRoot);
                        mLiveEndViewHolder.subscribeActivityLifeCycle();
                        mLiveEndViewHolder.addToParent();
                        mLiveEndViewHolder.showData(mLiveBean, mStream);
                    }
                    release();
                    mStartLive = false;
                    if (mSuperCloseLive) {
                        DialogUitl.showSimpleTipDialog(mContext, WordUtil.getString(R.string.live_illegal));
                    }
                } else {
                    ToastUtil.show(msg);
                }
            }

            @Override
            public boolean showLoadingDialog() {
                return true;
            }

            @Override
            public Dialog createLoadingDialog() {
                return DialogUitl.loadingDialog(mContext, WordUtil.getString(R.string.live_end_ing));
            }

            @Override
            public void onLoginInvalid() {
                mStartLive = false;
                release();
                finish();
                CommonAppConfig.getInstance().clearLoginInfo();
                //退出极光
                ImMessageUtil.getInstance().logoutImClient();
                ImPushUtil.getInstance().logout();
                if (mSuperCloseLive) {
                    RouteUtil.forwardLogin(WordUtil.getString(R.string.live_illegal));
                } else if (mLoginInvalid) {
                    RouteUtil.forwardLogin(WordUtil.getString(R.string.login_tip_0));
                } else {
                    RouteUtil.forwardLogin("");
                }
            }

            @Override
            public boolean isUseLoginInvalid() {
                return true;
            }
        });
    }


    @Override
    public void onBackPressed() {
        if (mLiveBeautyViewHolder != null && mLiveBeautyViewHolder.isShowed()) {
            mLiveBeautyViewHolder.hide();
            return;
        }
        if (mStartLive) {
            if (!canBackPressed()) {
                return;
            }
            closeLive();
        } else {
            if (mLivePushViewHolder != null) {
                mLivePushViewHolder.release();
            }
            if (mLiveLinkMicPresenter != null) {
                mLiveLinkMicPresenter.release();
            }
            mLivePushViewHolder = null;
            mLiveLinkMicPresenter = null;
            superBackPressed();
        }
    }


    public void superBackPressed() {
        super.onBackPressed();
    }

    public void release() {
        if (mSocketClient != null) {
            mSocketClient.disConnect();
            mSocketClient = null;
        }
        LiveHttpUtil.cancel(LiveHttpConsts.CHANGE_LIVE);
        LiveHttpUtil.cancel(LiveHttpConsts.STOP_LIVE);
        LiveHttpUtil.cancel(LiveHttpConsts.LIVE_PK_CHECK_LIVE);
        LiveHttpUtil.cancel(LiveHttpConsts.SET_LINK_MIC_ENABLE);
        CommonHttpUtil.cancel(CommonHttpConsts.CHECK_TOKEN_INVALID);
        if (mLiveReadyViewHolder != null) {
            mLiveReadyViewHolder.release();
        }
        if (mLiveMusicViewHolder != null) {
            mLiveMusicViewHolder.release();
        }
        if (mLivePushViewHolder != null) {
            mLivePushViewHolder.release();
        }
        if (mLiveLinkMicPresenter != null) {
            mLiveLinkMicPresenter.release();
        }
        if (mLiveBeautyViewHolder != null) {
            mLiveBeautyViewHolder.release();
        }
        if (mGamePresenter != null) {
            mGamePresenter.release();
        }
        mLiveMusicViewHolder = null;
        mLiveReadyViewHolder = null;
        mLivePushViewHolder = null;
        mLiveLinkMicPresenter = null;
        mLiveBeautyViewHolder = null;
        mGamePresenter = null;
        super.release();
    }

    @Override
    protected void onDestroy() {
        LiveHttpUtil.cancel(LiveHttpConsts.ANCHOR_CHECK_LIVE);
        super.onDestroy();
        L.e("LiveAnchorActivity-------onDestroy------->");
    }


    @Override
    protected void onPause() {
        if (mNeedCloseLive && mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.anchorPause();
        }
        super.onPause();
        sendSystemMessage(WordUtil.getString(R.string.live_anchor_leave));
        mPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPaused) {
            if (mLiveRoomViewHolder != null) {
                mLiveRoomViewHolder.anchorResume();
            }
            sendSystemMessage(WordUtil.getString(R.string.live_anchor_come_back));
            CommonHttpUtil.checkTokenInvalid();
        }
        mPaused = false;
        mNeedCloseLive = true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginInvalidEvent(LoginInvalidEvent e) {
        onAnchorInvalid();
    }

    /**
     * 直播间  主播登录失效
     */
    @Override
    public void onAnchorInvalid() {
        mLoginInvalid = true;
        super.onAnchorInvalid();
        endLive();
    }

    /**
     * 超管关闭直播间
     */
    @Override
    public void onSuperCloseLive() {
        mSuperCloseLive = true;
        super.onAnchorInvalid();
        endLive();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGameWindowChangedEvent(GameWindowChangedEvent e) {
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.setOffsetY(e.getGameViewHeight());
        }
        if (mLiveAnchorViewHolder != null) {
            mLiveAnchorViewHolder.setGameBtnVisible(e.isOpen());
        }
    }


    public void setBtnFunctionDark() {
        if (mLiveAnchorViewHolder != null) {
            mLiveAnchorViewHolder.setBtnFunctionDark();
        }
    }

    /**
     * 主播与主播连麦  主播收到其他主播发过来的连麦申请
     */
    @Override
    public void onLinkMicAnchorApply(UserBean u, String stream) {
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.onLinkMicAnchorApply(u, stream);
        }
    }

    /**
     * 主播与主播连麦  对方主播拒绝连麦的回调
     */
    @Override
    public void onLinkMicAnchorRefuse() {
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.onLinkMicAnchorRefuse();
        }
    }

    /**
     * 主播与主播连麦  对方主播无响应的回调
     */
    @Override
    public void onLinkMicAnchorNotResponse() {
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.onLinkMicNotResponse();
        }
    }

    /**
     * 主播与主播连麦  对方主播正在游戏
     */
    @Override
    public void onlinkMicPlayGaming() {
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.onlinkMicPlayGaming();
        }
    }


    /**
     * 主播与主播连麦  对方主播正在忙的回调
     */
    @Override
    public void onLinkMicAnchorBusy() {
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.onLinkMicAnchorBusy();
        }
    }

    /**
     * 发起主播连麦申请
     *
     * @param pkUid  对方主播的uid
     * @param stream 对方主播的stream
     */
    public void linkMicAnchorApply(final String pkUid, final String stream) {
        if (isBgmPlaying()) {
            DialogUitl.showSimpleDialog(mContext, WordUtil.getString(R.string.link_mic_close_bgm), new DialogUitl.SimpleCallback() {
                @Override
                public void onConfirmClick(Dialog dialog, String content) {
                    stopBgm();
                    checkLiveAnchorMic(pkUid, stream);
                }
            });
        } else {
            checkLiveAnchorMic(pkUid, stream);
        }
    }

    /**
     * 发起主播连麦申请
     *
     * @param pkUid  对方主播的uid
     * @param stream 对方主播的stream
     */
    private void checkLiveAnchorMic(final String pkUid, String stream) {
        LiveHttpUtil.livePkCheckLive(pkUid, stream, mStream, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    if (mLiveSDK == Constants.LIVE_SDK_TX) {
                        String playUrl = null;
                        JSONObject obj = JSON.parseObject(info[0]);
                        if (obj != null) {
                            String accUrl = obj.getString("pull");//自己主播的低延时流
                            if (!TextUtils.isEmpty(accUrl)) {
                                playUrl = accUrl;
                            }
                        }
                        if (mLiveLinkMicAnchorPresenter != null) {
                            mLiveLinkMicAnchorPresenter.applyLinkMicAnchor(pkUid, playUrl, mStream);
                        }
                    } else {
                        if (mLiveLinkMicAnchorPresenter != null) {
                            mLiveLinkMicAnchorPresenter.applyLinkMicAnchor(pkUid, null, mStream);
                        }
                    }
                } else {
                    ToastUtil.show(msg);
                }
            }
        });
    }

    /**
     * 设置连麦pk按钮是否可见
     */
    public void setPkBtnVisible(boolean visible) {
        if (mLiveAnchorViewHolder != null) {
            if (visible) {
                if (mLiveLinkMicAnchorPresenter.isLinkMic()) {
                    mLiveAnchorViewHolder.setPkBtnVisible(true);
                }
            } else {
                mLiveAnchorViewHolder.setPkBtnVisible(false);
            }
        }
    }

    /**
     * 发起主播连麦pk
     */
    public void applyLinkMicPk() {
        String pkUid = null;
        if (mLiveLinkMicAnchorPresenter != null) {
            pkUid = mLiveLinkMicAnchorPresenter.getPkUid();
        }
        if (!TextUtils.isEmpty(pkUid) && mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.applyLinkMicPk(pkUid, mStream);
        }
    }

    /**
     * 主播与主播PK  主播收到对方主播发过来的PK申请的回调
     *
     * @param u      对方主播的信息
     * @param stream 对方主播的stream
     */
    @Override
    public void onLinkMicPkApply(UserBean u, String stream) {
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.onLinkMicPkApply(u, stream);
        }
    }

    /**
     * 主播与主播PK  对方主播拒绝pk的回调
     */
    @Override
    public void onLinkMicPkRefuse() {
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.onLinkMicPkRefuse();
        }
    }

    /**
     * 主播与主播PK   对方主播正在忙的回调
     */
    @Override
    public void onLinkMicPkBusy() {
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.onLinkMicPkBusy();
        }
    }

    /**
     * 主播与主播PK   对方主播无响应的回调
     */
    @Override
    public void onLinkMicPkNotResponse() {
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.onLinkMicPkNotResponse();
        }
    }

    /**
     * 腾讯sdk连麦时候主播混流
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLinkMicTxMixStreamEvent(LinkMicTxMixStreamEvent e) {
        if (mLivePushViewHolder != null && mLivePushViewHolder instanceof LivePushTxViewHolder) {
            ((LivePushTxViewHolder) mLivePushViewHolder).onLinkMicTxMixStreamEvent(e.getType(), e.getToStream());
        }
    }

    /**
     * 主播checkLive
     */
    public void checkLive() {
        if (mCheckLiveCallback == null) {
            mCheckLiveCallback = new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0 && info.length > 0) {
                        int status = JSON.parseObject(info[0]).getIntValue("status");
                        printLog(DateFormatUtil.getCurTimeString2() + " <=== " + mReqCount + "----status=" + status + "\n");
                        if (status == 0) {
                            NotCancelableDialog dialog = new NotCancelableDialog();
                            dialog.setContent(WordUtil.getString(R.string.live_anchor_error));
                            dialog.setActionListener(new NotCancelableDialog.ActionListener() {
                                @Override
                                public void onConfirmClick(Context context, DialogFragment dialog) {
                                    dialog.dismiss();
                                    release();
                                    superBackPressed();
                                }
                            });
                            dialog.show(getSupportFragmentManager(), "VersionUpdateDialog");
                        }
                    }
                }

            };
        }
        mReqCount++;
        printLog(DateFormatUtil.getCurTimeString2() + " ===> " + mReqCount + "\n");
        LiveHttpUtil.anchorCheckLive(mLiveUid, mStream, mCheckLiveCallback);
    }


    private void printLog(String content) {
        if (mLogFile == null) {
            File dir = new File(CommonAppConfig.LOG_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            mLogFile = new File(dir, DateFormatUtil.getCurTimeString2() + "_" + mLiveUid + "_" + mStream + ".txt");
        }
//        L.e(TAG, content);
        LogUtil.print(mLogFile, content);
    }

    /**
     * 打开商品窗口
     */
    public void openGoodsWindow() {
        LiveShopDialogFragment fragment = new LiveShopDialogFragment();
        fragment.setLifeCycleListener(this);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.LIVE_UID, mLiveUid);
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "LiveShopDialogFragment");
    }


    public void forwardAddGoods() {
        if (mLiveGoodsAddViewHolder == null) {
            mLiveGoodsAddViewHolder = new LiveGoodsAddViewHolder(mContext, mPageContainer);
            mLiveGoodsAddViewHolder.subscribeActivityLifeCycle();
            mLiveGoodsAddViewHolder.addToParent();
        }
        mLiveGoodsAddViewHolder.show();
    }

    /**
     * 添加代卖的平台商品
     */
    public void forwardAddPlatGoods() {
        if (mLivePlatGoodsAddViewHolder == null) {
            mLivePlatGoodsAddViewHolder = new LivePlatGoodsAddViewHolder(mContext, mPageContainer);
            mLivePlatGoodsAddViewHolder.subscribeActivityLifeCycle();
            mLivePlatGoodsAddViewHolder.addToParent();
        }
        mLivePlatGoodsAddViewHolder.show();
    }


    @Override
    protected boolean canBackPressed() {
        if (mLiveGoodsAddViewHolder != null && mLiveGoodsAddViewHolder.isShowed()) {
            mLiveGoodsAddViewHolder.hide();
            return false;
        }
        if (mLivePlatGoodsAddViewHolder != null && mLivePlatGoodsAddViewHolder.isShowed()) {
            mLivePlatGoodsAddViewHolder.hide();
            return false;
        }
        return super.canBackPressed();
    }

    /**
     * 显示道具礼物
     */
    public void showStickerGift(LiveReceiveGiftBean bean) {
        if (mLivePushViewHolder != null && mLivePushViewHolder instanceof LivePushTxViewHolder) {
            ((LivePushTxViewHolder) mLivePushViewHolder).setLiveStickerGift(bean);
        }
    }


    /**
     * 发送展示直播间商品的消息
     */
    public void sendLiveGoodsShow(GoodsBean goodsBean) {
        SocketChatUtil.sendLiveGoodsShow(mSocketClient, goodsBean);
    }
}

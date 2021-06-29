package cn.wu1588.video.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.greenrobot.eventbus.EventBus;

import cn.wu1588.common.CommonAppConfig;
import cn.wu1588.common.Constants;
import cn.wu1588.common.bean.UserBean;
import cn.wu1588.common.glide.ImgLoader;
import cn.wu1588.common.http.CommonHttpUtil;
import cn.wu1588.common.http.HttpCallback;
import cn.wu1588.common.interfaces.CommonCallback;
import cn.wu1588.common.utils.RouteUtil;
import cn.wu1588.common.utils.ToastUtil;
import cn.wu1588.common.views.AbsViewHolder;
import cn.wu1588.video.R;
import cn.wu1588.video.activity.AbsVideoPlayActivity;
import cn.wu1588.video.bean.VideoBean;
import cn.wu1588.video.bean.VideoWithAds;
import cn.wu1588.video.dialog.VideoShareDialogFragment;
import cn.wu1588.video.event.VideoLikeEvent;
import cn.wu1588.video.http.VideoHttpUtil;
/**
 * Created by cxf on 2018/11/26.
 * 视频播放外框
 */

public class VideoPlayWrapViewHolder extends AbsViewHolder implements View.OnClickListener {

    private ViewGroup mVideoContainer;
    private ImageView mCover;
    private ImageView mAvatar;
    private TextView mName;
    private TextView mTitle;
    private ImageView mBtnLike;//点赞按钮
    private TextView mLikeNum;//点赞数
    private TextView mCommentNum;//评论数
    private TextView mShareNum;//分享数
    private ImageView mBtnFollow;//关注按钮
    private TextView mBtnGoods;//查看同款商品
    private VideoBean mVideoBean;
    private Drawable mFollowDrawable;//已关注
    private Drawable mUnFollowDrawable;//未关注
    private Animation mFollowAnimation;
    private boolean mCurPageShowed;//当前页面是否可见
    private ValueAnimator mLikeAnimtor;
    private Drawable[] mLikeAnimDrawables;//点赞帧动画
    private int mLikeAnimIndex;
    private String mTag;
    private View mRightSection;
    private View mBottomSection;
    private VideoWithAds mWithAd;


    public VideoPlayWrapViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_video_play_wrap;
    }

    @Override
    public void init() {
        mTag = this.toString();
        mVideoContainer = (ViewGroup) findViewById(R.id.video_container);
        mCover = (ImageView) findViewById(R.id.cover);
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mName = (TextView) findViewById(R.id.name);
        mTitle = (TextView) findViewById(R.id.title);
        mBtnLike = (ImageView) findViewById(R.id.btn_like);
        mLikeNum = (TextView) findViewById(R.id.like_num);
        mCommentNum = (TextView) findViewById(R.id.comment_num);
        mShareNum = (TextView) findViewById(R.id.share_num);
        mBtnFollow = (ImageView) findViewById(R.id.btn_follow);
        mRightSection = findViewById(R.id.right_section);
        mBottomSection = findViewById(R.id.bottom_section);
        mFollowDrawable = ContextCompat.getDrawable(mContext, R.mipmap.icon_video_follow_1);
        mUnFollowDrawable = ContextCompat.getDrawable(mContext, R.mipmap.icon_video_follow_0);
        mAvatar.setOnClickListener(this);
        mBtnFollow.setOnClickListener(this);
        mBtnLike.setOnClickListener(this);
        findViewById(R.id.btn_comment).setOnClickListener(this);
        findViewById(R.id.btn_share).setOnClickListener(this);
        mBtnGoods = findViewById(R.id.btn_goods);
        mBtnGoods.setOnClickListener(this);
    }

    /**
     * 初始化点赞动画
     */
    private void initLikeAnimtor() {
        if (mLikeAnimDrawables != null && mLikeAnimDrawables.length > 0) {
            mLikeAnimtor = ValueAnimator.ofFloat(0, mLikeAnimDrawables.length);
            mLikeAnimtor.setDuration(800);
            mLikeAnimtor.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float v = (float) animation.getAnimatedValue();
                    int index = (int) v;
                    if (mLikeAnimIndex != index) {
                        mLikeAnimIndex = index;
                        if (mBtnLike != null && mLikeAnimDrawables != null && index < mLikeAnimDrawables.length) {
                            mBtnLike.setImageDrawable(mLikeAnimDrawables[index]);
                        }
                    }
                }
            });
        }
    }

    /**
     * 初始化关注动画
     */
    public void initFollowAnimation() {
        mFollowAnimation = new ScaleAnimation(1, 0.3f, 1, 0.3f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mFollowAnimation.setRepeatMode(Animation.REVERSE);
        mFollowAnimation.setRepeatCount(1);
        mFollowAnimation.setDuration(200);
        mFollowAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if (mBtnFollow != null && mVideoBean != null) {
                    if (mVideoBean.getAttent() == 1) {
                        mBtnFollow.setImageDrawable(mFollowDrawable);
                    } else {
                        mBtnFollow.setImageDrawable(mUnFollowDrawable);
                    }
                }
            }
        });
    }

    public void setLikeAnimDrawables(Drawable[] drawables) {
        mLikeAnimDrawables = drawables;
    }

    public void setData(VideoWithAds bean, Object payload) {
        mWithAd = bean;
        mVideoBean = bean.videoBean;
        if (mVideoBean == null) {
            return;
        }
        UserBean u = mVideoBean.getUserBean();
        if (payload == null) {
            if (mCover != null) {
                setCoverImage();
            }
            if (mTitle != null) {
                mTitle.setText(mVideoBean.getTitle());
            }
            if (u != null) {
                if (mAvatar != null) {
                    ImgLoader.displayAvatar(mContext, u.getAvatar(), mAvatar);
                }
                if (mName != null) {
                    mName.setText("@" + u.getUserNiceName());
                }
            }
        }
        if (mBtnLike != null) {
            if (mVideoBean.getLike() == 1) {
                if (mLikeAnimDrawables != null && mLikeAnimDrawables.length > 0) {
                    mBtnLike.setImageDrawable(mLikeAnimDrawables[mLikeAnimDrawables.length - 1]);
                }
            } else {
                mBtnLike.setImageResource(R.mipmap.icon_video_zan_01);
            }
        }
        if (mLikeNum != null) {
            mLikeNum.setText(mVideoBean.getLikeNum());
        }
        if (mCommentNum != null) {
            mCommentNum.setText(mVideoBean.getCommentNum());
        }
        if (mShareNum != null) {
            mShareNum.setText(mVideoBean.getShareNum());
        }
        if (u != null && mBtnFollow != null) {
            String toUid = u.getId();
            if (!TextUtils.isEmpty(toUid) && !toUid.equals(CommonAppConfig.getInstance().getUid())) {
                if (mBtnFollow.getVisibility() != View.VISIBLE) {
                    mBtnFollow.setVisibility(View.VISIBLE);
                }
                if (mVideoBean.getAttent() == 1) {
                    mBtnFollow.setImageDrawable(mFollowDrawable);
                } else {
                    mBtnFollow.setImageDrawable(mUnFollowDrawable);
                }
            } else {
                if (mBtnFollow.getVisibility() == View.VISIBLE) {
                    mBtnFollow.setVisibility(View.INVISIBLE);
                }
            }
        }
        if (mBtnGoods != null) {
            int goodsType = mVideoBean.getType();
            if (goodsType == 0) {
                if (mBtnGoods.getVisibility() != View.GONE) {
                    mBtnGoods.setVisibility(View.GONE);
                }
            } else {
                if (mBtnGoods.getVisibility() != View.VISIBLE) {
                    mBtnGoods.setVisibility(View.VISIBLE);
                }
                if (goodsType == 1) {//商品
                    mBtnGoods.setText(R.string.goods_tip_32);
                } else if (goodsType == 2) {//付费内容
                    mBtnGoods.setText(R.string.mall_356);
                }
            }
        }

    }

    private void setCoverImage() {
        if(mVideoBean==null) return;
        ImgLoader.displayDrawable(mContext, mVideoBean.getThumb(), new ImgLoader.DrawableCallback() {
            @Override
            public void onLoadSuccess(Drawable drawable) {
                if (mCover != null && drawable != null) {
                    float w = drawable.getIntrinsicWidth();
                    float h = drawable.getIntrinsicHeight();
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mCover.getLayoutParams();
                    int targetH = 0;
                    if (w / h > 0.5625f) {//横屏  9:16=0.5625
                        targetH = (int) (mCover.getWidth() / w * h);
                    } else {
                        targetH = ViewGroup.LayoutParams.MATCH_PARENT;
                    }
                    if (targetH != params.height) {
                        params.height = targetH;
                        mCover.requestLayout();
                    }
                    mCover.setImageDrawable(drawable);
                }
            }

            @Override
            public void onLoadFailed() {

            }
        });
    }

    public void addVideoView(View view) {
        if (mVideoContainer != null && view != null) {
            mVideoContainer.removeAllViews();
            ViewParent parent = view.getParent();
            if (parent != null) {
                ViewGroup viewGroup = (ViewGroup) parent;
                if (viewGroup != mVideoContainer) {
                    viewGroup.removeView(view);
                    mVideoContainer.addView(view);
                }
            } else {
                mVideoContainer.addView(view);
            }
        }
    }

    public VideoWithAds getVideoBean() {
        return mWithAd;
    }


    /**
     * 获取到视频首帧回调
     */
    public void onFirstFrame() {
        if (mCover != null && mCover.getVisibility() == View.VISIBLE) {
            mCover.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 滑出屏幕
     */
    public void onPageOutWindow() {
        mCurPageShowed = false;
        if (mCover != null && mCover.getVisibility() != View.VISIBLE) {
            mCover.setVisibility(View.VISIBLE);
        }
        showAd(mWithAd.ad!=null);
    }

    /**
     * 滑入屏幕
     */
    public void onPageInWindow() {

        if (mCover != null) {
            if (mCover.getVisibility() != View.VISIBLE) {
                mCover.setVisibility(View.VISIBLE);
            }
            mCover.setImageDrawable(null);
            setCoverImage();
        }
        showAd(mWithAd.ad!=null);
    }

    private void showAd(boolean isShow){
        mCover.setVisibility(isShow?View.INVISIBLE:View.VISIBLE);
        mRightSection.setVisibility(isShow?View.GONE:View.VISIBLE);
        mBottomSection.setVisibility(isShow?View.GONE:View.VISIBLE);
    }
    /**
     * 滑动到这一页 准备开始播放
     */
    public void onPageSelected() {
        mCurPageShowed = true;
    }

    @Override
    public void onClick(View v) {
        if (!canClick()) {
            return;
        }
        int i = v.getId();
        if (i == R.id.btn_follow) {
            clickFollow();

        } else if (i == R.id.btn_comment) {
            clickComment();

        } else if (i == R.id.btn_share) {
            clickShare();

        } else if (i == R.id.btn_like) {
            clickLike();

        } else if (i == R.id.avatar) {
            clickAvatar();

        } else if (i == R.id.btn_goods) {
            clickGoods();
        }
    }

    /**
     * 点击  查看同款商品
     */
    private void clickGoods() {
        if (mContext != null && mVideoBean != null) {
            if (mVideoBean.getType() == 1) {//商品
                if (mVideoBean.getGoodsType() == 1) {
                    RouteUtil.forwardGoodsDetailOutSide(mVideoBean.getGoodsId(), false);
                } else {
                    RouteUtil.forwardGoodsDetail(mVideoBean.getGoodsId(), false, mVideoBean.getUid());
                }
            } else if (mVideoBean.getType() == 2) {//付费内容
                RouteUtil.forwardPayContentDetail(mVideoBean.getGoodsId());
            }
        }
    }


    /**
     * 点击头像
     */
    public void clickAvatar() {
        if (mVideoBean != null) {
            RouteUtil.forwardUserHome(mContext, mVideoBean.getUid());
        }
    }

    /**
     * 点赞,取消点赞
     */
    private void clickLike() {
        if (mVideoBean == null) {
            return;
        }
        VideoHttpUtil.setVideoLike(mTag, mVideoBean.getId(), new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    String likeNum = obj.getString("likes");
                    int like = obj.getIntValue("islike");
                    if (mVideoBean != null) {
                        mVideoBean.setLikeNum(likeNum);
                        mVideoBean.setLike(like);
                        EventBus.getDefault().post(new VideoLikeEvent(mVideoBean.getId(), like, likeNum));
                    }
                    if (mLikeNum != null) {
                        mLikeNum.setText(likeNum);
                    }
                    if (mBtnLike != null) {
                        if (like == 1) {
                            if (mLikeAnimtor == null) {
                                initLikeAnimtor();
                            }
                            mLikeAnimIndex = -1;
                            if (mLikeAnimtor != null) {
                                mLikeAnimtor.start();
                            }
                        } else {
                            mBtnLike.setImageResource(R.mipmap.icon_video_zan_01);
                        }
                    }

                } else {
                    ToastUtil.show(msg);
                }
            }
        });
    }

    /**
     * 点击关注按钮
     */
    private void clickFollow() {
        if (mVideoBean == null) {
            return;
        }
        final UserBean u = mVideoBean.getUserBean();
        if (u == null) {
            return;
        }
        CommonHttpUtil.setAttention(mTag, u.getId(), new CommonCallback<Integer>() {
            @Override
            public void callback(Integer attent) {
                mVideoBean.setAttent(attent);
                if (mCurPageShowed) {
                    if (mFollowAnimation == null) {
                        initFollowAnimation();
                    }
                    mBtnFollow.startAnimation(mFollowAnimation);
                } else {
                    if (attent == 1) {
                        mBtnFollow.setImageDrawable(mFollowDrawable);
                    } else {
                        mBtnFollow.setImageDrawable(mUnFollowDrawable);
                    }
                }
            }
        });
    }

    /**
     * 点击评论按钮
     */
    private void clickComment() {
        ((AbsVideoPlayActivity) mContext).openCommentWindow(mVideoBean.getId(), mVideoBean.getUid());
    }

    /**
     * 点击分享按钮
     */
    private void clickShare() {
        if (mVideoBean == null) {
            return;
        }
        VideoShareDialogFragment fragment = new VideoShareDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.VIDEO_BEAN, mVideoBean);
        fragment.setArguments(bundle);
        fragment.show(((AbsVideoPlayActivity) mContext).getSupportFragmentManager(), "VideoShareDialogFragment");
    }

    public void release() {
        VideoHttpUtil.cancel(mTag);
        if (mLikeAnimtor != null) {
            mLikeAnimtor.cancel();
        }
        if (mBtnFollow != null && mFollowAnimation != null) {
            mBtnFollow.clearAnimation();
        }
    }


}

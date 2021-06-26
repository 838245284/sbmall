package cn.wu1588.main.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cn.wu1588.common.CommonAppConfig;
import cn.wu1588.common.Constants;
import cn.wu1588.common.activity.AbsActivity;
import cn.wu1588.common.adapter.ImChatFacePagerAdapter;
import cn.wu1588.common.adapter.RefreshAdapter;
import cn.wu1588.common.bean.UserBean;
import cn.wu1588.common.custom.CommonRefreshView;
import cn.wu1588.common.event.FollowEvent;
import cn.wu1588.common.http.HttpCallback;
import cn.wu1588.common.interfaces.OnFaceClickListener;
import cn.wu1588.common.interfaces.OnItemClickListener;
import cn.wu1588.common.utils.DialogUitl;
import cn.wu1588.common.utils.StringUtil;
import cn.wu1588.common.utils.ToastUtil;
import cn.wu1588.common.utils.WordUtil;
import cn.wu1588.main.R;
import cn.wu1588.main.adapter.ActiveCommentAdapter;
import cn.wu1588.main.bean.ActiveBean;
import cn.wu1588.main.bean.ActiveCommentBean;
import cn.wu1588.main.dialog.ActiveInputDialogFragment;
import cn.wu1588.main.event.ActiveCommentEvent;
import cn.wu1588.main.http.MainHttpConsts;
import cn.wu1588.main.http.MainHttpUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;

/**
 * 动态详情页面
 */
public class ActiveDetailActivity extends AbsActivity implements View.OnClickListener, OnFaceClickListener, ActiveCommentAdapter.ActionListener, OnItemClickListener<ActiveCommentBean> {

    public static void forward(Context context, ActiveBean activeBean) {
        Intent intent = new Intent(context, ActiveDetailActivity.class);
        intent.putExtra(Constants.ACTIVE_BEAN, activeBean);
        context.startActivity(intent);
    }

    private CommonRefreshView mRefreshView;
    private ActiveCommentAdapter mAdapter;
    private ActiveBean mActiveBean;
    private View mFaceView;//表情面板
    private int mFaceHeight;//表情面板高度
    private ActiveInputDialogFragment mActiveInputDialogFragment;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_active_detail;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.active_detail));
        EventBus.getDefault().register(this);
        mActiveBean = getIntent().getParcelableExtra(Constants.ACTIVE_BEAN);
        findViewById(R.id.btn_input).setOnClickListener(this);
        findViewById(R.id.btn_face).setOnClickListener(this);
        mRefreshView = findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_active_comment);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<ActiveCommentBean>() {
            @Override
            public RefreshAdapter<ActiveCommentBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new ActiveCommentAdapter(mContext, mActiveBean);
                    mAdapter.setOnItemClickListener(ActiveDetailActivity.this);
                    mAdapter.setActionListener(ActiveDetailActivity.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                if (mActiveBean != null) {
                    MainHttpUtil.getActiveComments(mActiveBean.getId(), p, callback);
                }
            }

            @Override
            public List<ActiveCommentBean> processData(String[] info) {
                JSONObject obj = JSON.parseObject(info[0]);
                if (mAdapter != null) {
                    mAdapter.setCommentNum(obj.getIntValue("comments"));
                }
                List<ActiveCommentBean> list = JSON.parseArray(obj.getString("commentlist"), ActiveCommentBean.class);
                for (ActiveCommentBean bean : list) {
                    if (bean != null) {
                        bean.setParentNode(true);
                    }
                }
                return list;
            }

            @Override
            public void onRefreshSuccess(List<ActiveCommentBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<ActiveCommentBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        mRefreshView.initData();
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_input) {
            openCommentInputWindow(false, null);
        } else if (i == R.id.btn_face) {
            openCommentInputWindow(true, null);
        } else if (i == R.id.btn_send) {
            if (mActiveInputDialogFragment != null) {
                mActiveInputDialogFragment.sendComment();
            }
        }
    }

    /**
     * 打开评论输入框
     */
    public void openCommentInputWindow(boolean openFace, ActiveCommentBean bean) {
        if (bean != null && !TextUtils.isEmpty(bean.getUid()) && bean.getUid().equals(CommonAppConfig.getInstance().getUid())) {
            ToastUtil.show(R.string.video_cannot_apply_self);
            return;
        }
        if (mActiveBean == null) {
            return;
        }
        if (mFaceView == null) {
            mFaceView = initFaceView();
        }
        ActiveInputDialogFragment fragment = new ActiveInputDialogFragment();
        fragment.setActiveInfo(mActiveBean.getId(), mActiveBean.getUid());
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.VIDEO_FACE_OPEN, openFace);
        bundle.putInt(Constants.VIDEO_FACE_HEIGHT, mFaceHeight);
        bundle.putParcelable(Constants.VIDEO_COMMENT_BEAN, bean);
        fragment.setArguments(bundle);
        mActiveInputDialogFragment = fragment;
        fragment.show(getSupportFragmentManager(), "ActiveInputDialogFragment");
    }


    public View getFaceView() {
        if (mFaceView == null) {
            mFaceView = initFaceView();
        }
        return mFaceView;
    }

    /**
     * 初始化表情控件
     */
    private View initFaceView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.view_chat_face, null);
        v.measure(0, 0);
        mFaceHeight = v.getMeasuredHeight();
        v.findViewById(R.id.btn_send).setOnClickListener(this);
        final RadioGroup radioGroup = (RadioGroup) v.findViewById(R.id.radio_group);
        ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(10);
        ImChatFacePagerAdapter adapter = new ImChatFacePagerAdapter(mContext, this);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ((RadioButton) radioGroup.getChildAt(position)).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        for (int i = 0, pageCount = adapter.getCount(); i < pageCount; i++) {
            RadioButton radioButton = (RadioButton) inflater.inflate(R.layout.view_chat_indicator, radioGroup, false);
            radioButton.setId(i + 10000);
            if (i == 0) {
                radioButton.setChecked(true);
            }
            radioGroup.addView(radioButton);
        }
        return v;
    }


    @Override
    public void onFaceClick(String str, int faceImageRes) {
        if (mActiveInputDialogFragment != null) {
            mActiveInputDialogFragment.onFaceClick(str, faceImageRes);
        }
    }

    @Override
    public void onFaceDeleteClick() {
        if (mActiveInputDialogFragment != null) {
            mActiveInputDialogFragment.onFaceDeleteClick();
        }
    }

    @Override
    public void onExpandClicked(final ActiveCommentBean commentBean) {
        final ActiveCommentBean parentNodeBean = commentBean.getParentNodeBean();
        if (parentNodeBean == null) {
            return;
        }
        MainHttpUtil.getActiveCommentReply(parentNodeBean.getId(), parentNodeBean.getChildPage(), new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    List<ActiveCommentBean> list = JSON.parseArray(Arrays.toString(info), ActiveCommentBean.class);
                    if (list == null || list.size() == 0) {
                        return;
                    }
                    if (parentNodeBean.getChildPage() == 1) {
                        if (list.size() > 1) {
                            list = list.subList(1, list.size());
                        }
                    }
                    for (ActiveCommentBean bean : list) {
                        bean.setParentNodeBean(parentNodeBean);
                    }
                    List<ActiveCommentBean> childList = parentNodeBean.getChildList();
                    if (childList != null) {
                        childList.addAll(list);
                        if (childList.size() < parentNodeBean.getReplyNum()) {
                            parentNodeBean.setChildPage(parentNodeBean.getChildPage() + 1);
                        }
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }


    @Override
    public void onCollapsedClicked(ActiveCommentBean commentBean) {
        ActiveCommentBean parentNodeBean = commentBean.getParentNodeBean();
        if (parentNodeBean == null) {
            return;
        }
        List<ActiveCommentBean> childList = parentNodeBean.getChildList();
        ActiveCommentBean node0 = childList.get(0);
        int orignSize = childList.size();
        parentNodeBean.removeChild();
        parentNodeBean.setChildPage(1);
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 长按事件
     */
    @Override
    public void onItemLongClick(final ActiveCommentBean bean) {
        if (mActiveBean == null) {
            return;
        }
        String uid = CommonAppConfig.getInstance().getUid();
        Integer[] arr = null;
        //是否是自己的视频
        String activeUid = mActiveBean.getUid();
        boolean isSelfVideo = !TextUtils.isEmpty(activeUid) && activeUid.equals(uid);
        //是否是自己的评论
        String commentUid = bean.getUid();
        boolean isSelfComment = !TextUtils.isEmpty(commentUid) && commentUid.equals(uid);
        if (isSelfVideo || isSelfComment) {
            arr = new Integer[]{R.string.copy, R.string.delete};
        } else {
            arr = new Integer[]{R.string.copy};
        }
        if (arr == null) {
            return;
        }
        DialogUitl.showStringArrayDialog(mContext, arr, new DialogUitl.StringArrayDialogCallback() {
            @Override
            public void onItemClick(String text, int tag) {
                if (tag == R.string.copy) {//复制评论
                    UserBean u = bean.getUserBean();
                    if (u != null) {
                        String content = StringUtil.contact("@", u.getUserNiceName(), ": ", bean.getContent());
                        ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText("text", content);
                        cm.setPrimaryClip(clipData);
                        ToastUtil.show(R.string.copy_success);
                    }
                } else if (tag == R.string.delete) {
                    //删除评论
                    MainHttpUtil.deleteComment(mActiveBean.getId(), bean.getId(), bean.getUid(), new HttpCallback() {

                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            if (code == 0) {
                                if (mRefreshView != null) {
                                    mRefreshView.initData();
                                }
                            }
                            ToastUtil.show(msg);
                        }
                    });

                }
            }
        });
    }


    @Override
    public void onItemClick(ActiveCommentBean bean, int position) {
        openCommentInputWindow(false, bean);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActiveCommentEvent(ActiveCommentEvent e) {
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFollowEvent(FollowEvent e) {
        if (mActiveBean != null && mActiveBean.getUid().equals(e.getToUid())) {
            if (mAdapter != null) {
                mAdapter.onFollowChanged(e.getIsAttention());
            }
        }
    }


    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        MainHttpUtil.cancel(MainHttpConsts.GET_ACTIVE_COMMENTS);
        MainHttpUtil.cancel(MainHttpConsts.SET_ACTIVE_COMMENT_LIKE);
        MainHttpUtil.cancel(MainHttpConsts.GET_ACTIVE_COMMENT_REPLY);
        MainHttpUtil.cancel(MainHttpConsts.DELETE_COMMENT);
        if (mAdapter != null) {
            mAdapter.release();
        }
        super.onDestroy();
    }
}

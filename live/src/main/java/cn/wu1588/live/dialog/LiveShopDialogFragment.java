package cn.wu1588.live.dialog;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

import cn.wu1588.common.Constants;
import cn.wu1588.common.adapter.RefreshAdapter;
import cn.wu1588.common.bean.GoodsBean;
import cn.wu1588.common.custom.CommonRefreshView;
import cn.wu1588.common.dialog.AbsDialogFragment;
import cn.wu1588.common.http.HttpCallback;
import cn.wu1588.common.utils.DialogUitl;
import cn.wu1588.common.utils.DpUtil;
import cn.wu1588.common.utils.StringUtil;
import cn.wu1588.common.utils.WordUtil;
import cn.wu1588.live.R;
import cn.wu1588.live.activity.LiveAnchorActivity;
import cn.wu1588.live.adapter.LiveShopAdapter;
import cn.wu1588.live.http.LiveHttpConsts;
import cn.wu1588.live.http.LiveHttpUtil;

/**
 * Created by cxf on 2019/8/29.
 */

public class LiveShopDialogFragment extends AbsDialogFragment implements View.OnClickListener, LiveShopAdapter.ActionListener {

    private CommonRefreshView mRefreshView;
    private LiveShopAdapter mAdapter;
    private TextView mTitle;
    private String mLiveUid;
    private int mGoodsNum;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_live_shop;
    }

    @Override
    protected int getDialogStyle() {
        return R.style.dialog2;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        window.setWindowAnimations(R.style.bottomToTopAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = DpUtil.dp2px(320);
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mLiveUid = bundle.getString(Constants.LIVE_UID);
        }
        mTitle = (TextView) findViewById(R.id.title);
        findViewById(R.id.btn_add).setOnClickListener(this);
        mRefreshView = (CommonRefreshView) findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_shop);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<GoodsBean>() {
            @Override
            public RefreshAdapter<GoodsBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new LiveShopAdapter(mContext);
                    mAdapter.setActionListener(LiveShopDialogFragment.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                LiveHttpUtil.getSale(p, mLiveUid, callback);
            }

            @Override
            public List<GoodsBean> processData(String[] info) {
                JSONObject obj = JSON.parseObject(info[0]);
                mGoodsNum = obj.getIntValue("nums");
                mTitle.setText(StringUtil.contact(WordUtil.getString(R.string.goods_tip_17), " ", String.valueOf(mGoodsNum)));
                return JSON.parseArray(obj.getString("list"), GoodsBean.class);
            }

            @Override
            public void onRefreshSuccess(List<GoodsBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<GoodsBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        mRefreshView.initData();
    }


    @Override
    public void onDeleteSuccess() {
        if (mTitle != null) {
            mGoodsNum--;
            if (mGoodsNum < 0) {
                mGoodsNum = 0;
            }
            mTitle.setText(StringUtil.contact(WordUtil.getString(R.string.goods_tip_17), " ", String.valueOf(mGoodsNum)));
        }
    }

    @Override
    public void onGoodsShowChanged(GoodsBean bean) {
        if (mContext != null) {
            ((LiveAnchorActivity) mContext).sendLiveGoodsShow(bean);
        }
        dismiss();
    }


    @Override
    public void onDestroy() {
        mContext = null;
        if (mAdapter != null) {
            mAdapter.setActionListener(null);
        }
        LiveHttpUtil.cancel(LiveHttpConsts.GET_SALE);
        LiveHttpUtil.cancel(LiveHttpConsts.SET_SALE);
        LiveHttpUtil.cancel(LiveHttpConsts.SET_LIVE_GOODS_SHOW);
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_add) {
            if (mContext != null) {
                DialogUitl.showStringArrayDialog(mContext, new Integer[]{
                        R.string.mall_404, R.string.mall_406
                }, new DialogUitl.StringArrayDialogCallback() {
                    @Override
                    public void onItemClick(String text, int tag) {
                        if (tag == R.string.mall_406) {
                            ((LiveAnchorActivity) mContext).forwardAddPlatGoods();
                            dismiss();
                        } else if (tag == R.string.mall_404) {
                            ((LiveAnchorActivity) mContext).forwardAddGoods();
                            dismiss();
                        }
                    }
                });
            }
        }
    }


}

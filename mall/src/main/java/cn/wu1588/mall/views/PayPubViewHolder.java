package cn.wu1588.mall.views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;

import java.util.Arrays;
import java.util.List;

import cn.wu1588.common.adapter.RefreshAdapter;
import cn.wu1588.common.custom.CommonRefreshView;
import cn.wu1588.common.http.HttpCallback;
import cn.wu1588.common.views.AbsCommonViewHolder;
import cn.wu1588.mall.R;
import cn.wu1588.mall.activity.PayContentPubActivity;
import cn.wu1588.mall.adapter.PayPubAdapter;
import cn.wu1588.mall.bean.PayContentBean;
import cn.wu1588.mall.http.MallHttpUtil;

/**
 * 我上传的
 */
public class PayPubViewHolder extends AbsCommonViewHolder implements View.OnClickListener {

    private CommonRefreshView mRefreshView;
    private PayPubAdapter mAdapter;

    public PayPubViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_pay_pub;
    }

    @Override
    public void init() {
        findViewById(R.id.btn_pub).setOnClickListener(this);
        mRefreshView = findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_pay_pub);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<PayContentBean>() {
            @Override
            public RefreshAdapter<PayContentBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new PayPubAdapter(mContext);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                MallHttpUtil.getMyPayContentList(p, callback);
            }

            @Override
            public List<PayContentBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), PayContentBean.class);
            }

            @Override
            public void onRefreshSuccess(List<PayContentBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<PayContentBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
    }

    @Override
    public void onClick(View v) {
        PayContentPubActivity.forward(mContext);
    }


    @Override
    public void loadData() {
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }

}

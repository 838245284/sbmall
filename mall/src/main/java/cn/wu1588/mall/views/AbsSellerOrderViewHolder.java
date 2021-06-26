package cn.wu1588.mall.views;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

import cn.wu1588.common.Constants;
import cn.wu1588.common.HtmlConfig;
import cn.wu1588.common.activity.WebViewActivity;
import cn.wu1588.common.adapter.RefreshAdapter;
import cn.wu1588.common.custom.CommonRefreshView;
import cn.wu1588.common.http.HttpCallback;
import cn.wu1588.common.utils.DialogUitl;
import cn.wu1588.common.utils.StringUtil;
import cn.wu1588.common.utils.ToastUtil;
import cn.wu1588.common.utils.WordUtil;
import cn.wu1588.common.views.AbsCommonViewHolder;
import cn.wu1588.im.activity.ChatRoomActivity;
import cn.wu1588.im.bean.ImUserBean;
import cn.wu1588.im.http.ImHttpUtil;
import cn.wu1588.mall.R;
import cn.wu1588.mall.activity.SellerOrderActivity;
import cn.wu1588.mall.activity.SellerOrderDetailActivity;
import cn.wu1588.mall.activity.SellerRefundDetailActivity;
import cn.wu1588.mall.activity.SellerSendActivity;
import cn.wu1588.mall.adapter.SellerOrderBaseAdapter;
import cn.wu1588.mall.bean.SellerOrderBean;
import cn.wu1588.mall.http.MallHttpUtil;

public abstract class AbsSellerOrderViewHolder extends AbsCommonViewHolder implements SellerOrderBaseAdapter.ActionListener {

    private CommonRefreshView mRefreshView;
    private SellerOrderBaseAdapter mAdapter;
    private String mNumJsonString;

    public AbsSellerOrderViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_seller_order_list;
    }

    @Override
    public void init() {
        mRefreshView = findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_buyer_order);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<SellerOrderBean>() {
            @Override
            public RefreshAdapter<SellerOrderBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = getSellerOrderAdapter();
                    mAdapter.setActionListener(AbsSellerOrderViewHolder.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                MallHttpUtil.getSellerOrderList(getOrderType(), p, callback);
            }

            @Override
            public List<SellerOrderBean> processData(String[] info) {
                JSONObject obj = JSON.parseObject(info[0]);
                mNumJsonString = obj.getString("type_list_nums");
                return JSON.parseArray(obj.getString("list"), SellerOrderBean.class);
            }

            @Override
            public void onRefreshSuccess(List<SellerOrderBean> list, int listCount) {
                if (!TextUtils.isEmpty(mNumJsonString) && mContext != null) {
                    ((SellerOrderActivity) mContext).setOrderNum(mNumJsonString);
                }
            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<SellerOrderBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
    }

    public abstract String getOrderType();

    public abstract SellerOrderBaseAdapter getSellerOrderAdapter();

    @Override
    public void loadData() {
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }

    public void refreshData() {
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }

    /**
     * 点击item
     */
    @Override
    public void onItemClick(SellerOrderBean bean) {
        if (bean.getStatus() == Constants.MALL_ORDER_STATUS_REFUND) {
            SellerRefundDetailActivity.forward(mContext, bean.getId());
        } else {
            SellerOrderDetailActivity.forward(mContext, bean.getId());
        }
    }


    /**
     * 去发货
     */
    @Override
    public void onSendClick(SellerOrderBean bean) {
        SellerSendActivity.forward(mContext, bean.getId());
    }

    /**
     * 删除订单
     */
    @Override
    public void onDeleteClick(final SellerOrderBean bean) {
        new DialogUitl.Builder(mContext)
                .setContent(WordUtil.getString(R.string.mall_370))
                .setCancelable(true)
                .setBackgroundDimEnabled(true)
                .setClickCallback(new DialogUitl.SimpleCallback() {
                    @Override
                    public void onConfirmClick(Dialog dialog, String content) {
                        MallHttpUtil.sellerDeleteOrder(bean.getId(), new HttpCallback() {
                            @Override
                            public void onSuccess(int code, String msg, String[] info) {
                                if (code == 0) {
                                    refreshData();
                                }
                                ToastUtil.show(msg);
                            }
                        });
                    }
                })
                .build()
                .show();
    }

    /**
     * 查看物流
     */
    @Override
    public void onWuLiuClick(SellerOrderBean bean) {
        String url = StringUtil.contact(HtmlConfig.MALL_BUYER_WULIU, "orderid=", bean.getId(), "&user_type=seller");
        WebViewActivity.forward(mContext, url);
    }

    /**
     * 退款详情
     */
    @Override
    public void onRefundClick(SellerOrderBean bean) {
        SellerRefundDetailActivity.forward(mContext, bean.getId());
    }

    /**
     * 联系买家
     */
    @Override
    public void onContactBuyerClick(SellerOrderBean bean) {
        ImHttpUtil.getImUserInfo(bean.getUid(), new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    ImUserBean bean = JSON.parseObject(info[0], ImUserBean.class);
                    if (bean != null) {
                        ChatRoomActivity.forward(mContext, bean, bean.getAttent() == 1, false);
                    }
                }
            }
        });
    }

}

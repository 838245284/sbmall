package cn.wu1588.mall.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;

import java.util.Arrays;
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
import cn.wu1588.mall.R;
import cn.wu1588.mall.activity.BuyerCommentActivity;
import cn.wu1588.mall.activity.BuyerCommentAppendActivity;
import cn.wu1588.mall.activity.BuyerOrderActivity;
import cn.wu1588.mall.activity.BuyerOrderDetailActivity;
import cn.wu1588.mall.activity.BuyerRefundDetailActivity;
import cn.wu1588.mall.activity.ShopHomeActivity;
import cn.wu1588.mall.adapter.BuyerOrderBaseAdapter;
import cn.wu1588.mall.bean.BuyerOrderBean;
import cn.wu1588.mall.dialog.GoodsPayDialogFragment;
import cn.wu1588.mall.http.MallHttpUtil;

public abstract class AbsBuyerOrderViewHolder extends AbsCommonViewHolder implements BuyerOrderBaseAdapter.ActionListener, GoodsPayDialogFragment.ActionListener {

    private CommonRefreshView mRefreshView;
    private BuyerOrderBaseAdapter mAdapter;

    public AbsBuyerOrderViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_buyer_order_list;
    }

    @Override
    public void init() {
        mRefreshView = findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_buyer_order);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<BuyerOrderBean>() {
            @Override
            public RefreshAdapter<BuyerOrderBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = getBuyerOrderAdapter();
                    mAdapter.setActionListener(AbsBuyerOrderViewHolder.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                MallHttpUtil.getBuyerOrderList(getOrderType(), p, callback);
            }

            @Override
            public List<BuyerOrderBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), BuyerOrderBean.class);
            }

            @Override
            public void onRefreshSuccess(List<BuyerOrderBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<BuyerOrderBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
    }

    public abstract String getOrderType();

    public abstract BuyerOrderBaseAdapter getBuyerOrderAdapter();

    @Override
    public void loadData() {
//        if (isFirstLoadData() && mRefreshView != null) {
//            mRefreshView.initData();
//        }
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
     * 点击商店名字
     */
    @Override
    public void onShopClick(BuyerOrderBean bean) {
        ShopHomeActivity.forward(mContext, bean.getSellerId());
    }

    /**
     * 点击item
     */
    @Override
    public void onItemClick(BuyerOrderBean bean) {
        if (bean.getStatus() == Constants.MALL_ORDER_STATUS_REFUND) {
            BuyerRefundDetailActivity.forward(mContext, bean.getId());
        } else {
            BuyerOrderDetailActivity.forward(mContext, bean.getId());
        }
    }

    /**
     * 取消订单
     */
    @Override
    public void onCancelOrderClick(final BuyerOrderBean bean) {
        new DialogUitl.Builder(mContext)
                .setContent(WordUtil.getString(R.string.mall_371))
                .setCancelable(true)
                .setBackgroundDimEnabled(true)
                .setClickCallback(new DialogUitl.SimpleCallback() {
                    @Override
                    public void onConfirmClick(Dialog dialog, String content) {
                        MallHttpUtil.buyerCancelOrder(bean.getId(), new HttpCallback() {
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
     * 付款
     */
    @Override
    public void onPayClick(BuyerOrderBean bean) {
        GoodsPayDialogFragment fragment = new GoodsPayDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.MALL_ORDER_ID, bean.getId());
        bundle.putDouble(Constants.MALL_ORDER_MONEY, Double.parseDouble(bean.getTotalPrice()));
        bundle.putString(Constants.MALL_GOODS_NAME, bean.getGoodsName());
        fragment.setArguments(bundle);
        fragment.setActionListener(this);
        fragment.show(((BuyerOrderActivity) mContext).getSupportFragmentManager(), "GoodsPayDialogFragment");
    }

    /**
     * 付款成功回调
     */
    @Override
    public void onPayResult(boolean paySuccess) {
        if (paySuccess) {
            refreshData();
        }
    }

    /**
     * 删除订单
     */
    @Override
    public void onDeleteClick(final BuyerOrderBean bean) {
        new DialogUitl.Builder(mContext)
                .setContent(WordUtil.getString(R.string.mall_370))
                .setCancelable(true)
                .setBackgroundDimEnabled(true)
                .setClickCallback(new DialogUitl.SimpleCallback() {
                    @Override
                    public void onConfirmClick(Dialog dialog, String content) {
                        MallHttpUtil.buyerDeleteOrder(bean.getId(), new HttpCallback() {
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
    public void onWuLiuClick(BuyerOrderBean bean) {
        String url = StringUtil.contact(HtmlConfig.MALL_BUYER_WULIU, "orderid=", bean.getId(), "&user_type=buyer");
        WebViewActivity.forward(mContext, url);
    }

    /**
     * 确认收货
     */
    @Override
    public void onConfirmClick(final BuyerOrderBean bean) {
        new DialogUitl.Builder(mContext)
                .setContent(WordUtil.getString(R.string.mall_372))
                .setCancelable(true)
                .setBackgroundDimEnabled(true)
                .setClickCallback(new DialogUitl.SimpleCallback() {
                    @Override
                    public void onConfirmClick(Dialog dialog, String content) {
                        MallHttpUtil.buyerConfirmReceive(bean.getId(), new HttpCallback() {
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
     * 评价
     */
    @Override
    public void onCommentClick(BuyerOrderBean bean) {
        BuyerCommentActivity.forward(mContext, bean.getId());
    }

    /**
     * 发表追评
     */
    @Override
    public void onAppendCommentClick(BuyerOrderBean bean) {
        BuyerCommentAppendActivity.forward(mContext, bean.getId());
    }

    /**
     * 退款详情
     */
    @Override
    public void onRefundClick(BuyerOrderBean bean) {
        BuyerRefundDetailActivity.forward(mContext, bean.getId());
    }


}

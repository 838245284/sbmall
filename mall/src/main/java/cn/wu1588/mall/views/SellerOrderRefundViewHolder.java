package cn.wu1588.mall.views;

import android.content.Context;
import android.view.ViewGroup;

import cn.wu1588.mall.adapter.SellerOrderBaseAdapter;
import cn.wu1588.mall.adapter.SellerOrderRefundAdapter;

/**
 * 卖家 订单列表 待退款
 */
public class SellerOrderRefundViewHolder extends AbsSellerOrderViewHolder {

    public SellerOrderRefundViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    public String getOrderType() {
        return "wait_refund";
    }

    @Override
    public SellerOrderBaseAdapter getSellerOrderAdapter() {
        return new SellerOrderRefundAdapter(mContext);
    }

}

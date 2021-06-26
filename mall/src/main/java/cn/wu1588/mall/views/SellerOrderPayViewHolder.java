package cn.wu1588.mall.views;

import android.content.Context;
import android.view.ViewGroup;

import cn.wu1588.mall.adapter.SellerOrderBaseAdapter;
import cn.wu1588.mall.adapter.SellerOrderPayAdapter;

/**
 * 卖家 订单列表 待付款
 */
public class SellerOrderPayViewHolder extends AbsSellerOrderViewHolder {

    public SellerOrderPayViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    public String getOrderType() {
        return "wait_payment";
    }

    @Override
    public SellerOrderBaseAdapter getSellerOrderAdapter() {
        return new SellerOrderPayAdapter(mContext);
    }

}

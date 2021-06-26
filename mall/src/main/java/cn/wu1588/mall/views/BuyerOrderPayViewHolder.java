package cn.wu1588.mall.views;

import android.content.Context;
import android.view.ViewGroup;

import cn.wu1588.mall.adapter.BuyerOrderBaseAdapter;
import cn.wu1588.mall.adapter.BuyerOrderPayAdapter;

/**
 * 买家 订单列表 待付款
 */
public class BuyerOrderPayViewHolder extends AbsBuyerOrderViewHolder {

    public BuyerOrderPayViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    public String getOrderType() {
        return "wait_payment";
    }

    @Override
    public BuyerOrderBaseAdapter getBuyerOrderAdapter() {
        return new BuyerOrderPayAdapter(mContext);
    }

}

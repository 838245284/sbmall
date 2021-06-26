package cn.wu1588.mall.views;

import android.content.Context;
import android.view.ViewGroup;

import cn.wu1588.mall.adapter.BuyerOrderBaseAdapter;
import cn.wu1588.mall.adapter.BuyerOrderSendAdapter;

/**
 * 买家 订单列表 待发货
 */
public class BuyerOrderSendViewHolder extends AbsBuyerOrderViewHolder {

    public BuyerOrderSendViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    public String getOrderType() {
        return "wait_shipment";
    }

    @Override
    public BuyerOrderBaseAdapter getBuyerOrderAdapter() {
        return new BuyerOrderSendAdapter(mContext);
    }


}

package cn.wu1588.mall.views;

import android.content.Context;
import android.view.ViewGroup;

import cn.wu1588.mall.adapter.BuyerOrderAllAdapter;
import cn.wu1588.mall.adapter.BuyerOrderBaseAdapter;

/**
 * 买家 订单列表 全部
 */
public class BuyerOrderAllViewHolder extends AbsBuyerOrderViewHolder {

    public BuyerOrderAllViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    public String getOrderType() {
        return "all";
    }

    @Override
    public BuyerOrderBaseAdapter getBuyerOrderAdapter() {
        return new BuyerOrderAllAdapter(mContext);
    }


}

package cn.wu1588.mall.views;

import android.content.Context;
import android.view.ViewGroup;

import cn.wu1588.mall.adapter.BuyerOrderBaseAdapter;
import cn.wu1588.mall.adapter.BuyerOrderRefundAdapter;
import cn.wu1588.mall.bean.BuyerOrderBean;

/**
 * 买家 订单列表 退款
 */
public class BuyerOrderRefundViewHolder extends AbsBuyerOrderViewHolder {

    public BuyerOrderRefundViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    public String getOrderType() {
        return "refund";
    }

    @Override
    public BuyerOrderBaseAdapter getBuyerOrderAdapter() {
        return new BuyerOrderRefundAdapter(mContext);
    }

    @Override
    public void onItemClick(BuyerOrderBean bean) {
        onRefundClick(bean);
    }
}

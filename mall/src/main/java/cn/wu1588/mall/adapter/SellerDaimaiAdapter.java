package cn.wu1588.mall.adapter;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.wu1588.mall.R;
import cn.wu1588.common.adapter.RefreshAdapter;
import cn.wu1588.common.glide.ImgLoader;
import cn.wu1588.common.utils.DialogUitl;
import cn.wu1588.common.utils.StringUtil;
import cn.wu1588.common.utils.WordUtil;

import cn.wu1588.mall.bean.GoodsManageBean;
import cn.wu1588.mall.http.MallHttpUtil;

public class SellerDaimaiAdapter extends RefreshAdapter<GoodsManageBean> {

    private View.OnClickListener mOnClickListener;
    private View.OnClickListener mCancelClickListener;
    private ActionListener mActionListener;
    private String mSaleString;
    private String mMoneySymbol;
    private String mStringYong;


    public SellerDaimaiAdapter(Context context) {
        super(context);
        mSaleString = WordUtil.getString(R.string.mall_114);
        mMoneySymbol = WordUtil.getString(R.string.money_symbol);
        mStringYong = WordUtil.getString(R.string.mall_408);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null && mActionListener != null) {
                    mActionListener.onItemClick((GoodsManageBean) tag);
                }
            }
        };
        mCancelClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final GoodsManageBean bean = (GoodsManageBean) v.getTag();
                if (bean != null) {
                    new DialogUitl.Builder(mContext)
                            .setContent(WordUtil.getString(R.string.mall_411))
                            .setCancelable(true)
                            .setIsHideTitle(true)
                            .setBackgroundDimEnabled(true)
                            .setClickCallback(new DialogUitl.SimpleCallback() {
                                @Override
                                public void onConfirmClick(Dialog dialog, String content) {
                                    MallHttpUtil.setPlatGoods(bean.getId());
                                }
                            })
                            .build()
                            .show();

                }
            }
        };
    }


    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.item_seller_daimai, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
        ((Vh) vh).setData(mList.get(position));
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mThumb;
        TextView mName;
        TextView mSaleNum;
        TextView mPrice;
        TextView mPriceYong;
        View mBtnCancel;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mThumb = itemView.findViewById(R.id.thumb);
            mName = itemView.findViewById(R.id.name);
            mSaleNum = itemView.findViewById(R.id.sale_num);
            mPrice = itemView.findViewById(R.id.price);
            mPriceYong = itemView.findViewById(R.id.price_yong);
            mBtnCancel = itemView.findViewById(R.id.btn_cancel);
            itemView.setOnClickListener(mOnClickListener);
            mBtnCancel.setOnClickListener(mCancelClickListener);
        }


        void setData(GoodsManageBean bean) {
            itemView.setTag(bean);
            mBtnCancel.setTag(bean);
            ImgLoader.display(mContext, bean.getThumb(), mThumb);
            mName.setText(bean.getName());
            mSaleNum.setText(String.format(mSaleString, bean.getSaleNum()));
            mPrice.setText(StringUtil.contact(mMoneySymbol, bean.getPrice()));
            mPriceYong.setText(StringUtil.contact(mStringYong, mMoneySymbol, bean.getPriceYong()));

        }
    }

    public interface ActionListener {
        void onItemClick(GoodsManageBean bean);
    }

}

package cn.wu1588.mall.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.wu1588.mall.R;
import cn.wu1588.common.adapter.RefreshAdapter;
import cn.wu1588.common.bean.GoodsBean;
import cn.wu1588.common.glide.ImgLoader;
import cn.wu1588.common.utils.StringUtil;
import cn.wu1588.common.utils.WordUtil;

import cn.wu1588.mall.activity.GoodsDetailActivity;

public class GoodsCollectAdapter extends RefreshAdapter<GoodsBean> {

    private String mMoneySymbol;
    private View.OnClickListener mOnClickListener;

    public GoodsCollectAdapter(Context context) {
        super(context);
        mMoneySymbol = WordUtil.getString(R.string.money_symbol);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoodsBean bean = (GoodsBean) v.getTag();
                GoodsDetailActivity.forward(mContext, bean.getId(), bean.getType());
            }
        };
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.item_goods_collect, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i) {
        ((Vh) vh).setData(mList.get(i));
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mThumb;
        TextView mName;
        TextView mPriceNow;
        TextView mPriceOrigin;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mThumb = itemView.findViewById(R.id.thumb);
            mName = itemView.findViewById(R.id.name);
            mPriceNow = itemView.findViewById(R.id.price_now);
            mPriceOrigin = itemView.findViewById(R.id.price_origin);
            mPriceOrigin.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(GoodsBean bean) {
            itemView.setTag(bean);
            ImgLoader.display(mContext, bean.getThumb(), mThumb);
            mName.setText(bean.getName());
            mPriceNow.setText(bean.getPriceNow());
            if(bean.getType()==1){
                mPriceOrigin.setText(StringUtil.contact(mMoneySymbol, bean.getOriginPrice()));
            }
            else{
                mPriceOrigin.setText(null);
            }
        }
    }
}

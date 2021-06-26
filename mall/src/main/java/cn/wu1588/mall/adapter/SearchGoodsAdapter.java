package cn.wu1588.mall.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.wu1588.common.Constants;
import cn.wu1588.common.adapter.RefreshAdapter;
import cn.wu1588.common.glide.ImgLoader;
import cn.wu1588.mall.R;
import cn.wu1588.mall.bean.SearchGoodsBean;

public class SearchGoodsAdapter extends RefreshAdapter<SearchGoodsBean> {

    private Drawable mCheckedDrawable;
    private View.OnClickListener mOnClickListener;
    private int mCheckedPosition = -1;

    public SearchGoodsAdapter(Context context) {
        super(context);
        mCheckedDrawable = ContextCompat.getDrawable(context, R.mipmap.ic_check_1);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                if (position == mCheckedPosition) {
                    cancelChecked();
                    return;
                }
                if (mCheckedPosition >= 0) {
                    mList.get(mCheckedPosition).setChecked(false);
                    notifyItemChanged(mCheckedPosition, Constants.PAYLOAD);
                }
                SearchGoodsBean bean = mList.get(position);
                bean.setChecked(true);
                notifyItemChanged(position, Constants.PAYLOAD);
                mCheckedPosition = position;
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(bean, position);
                }
            }
        };
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.item_search_goods, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position, @NonNull List payloads) {
        Object payload = payloads.size() > 0 ? payloads.get(0) : null;
        ((Vh) vh).setData(mList.get(position), position, payload);
    }

    public SearchGoodsBean getCheckedBean() {
        if (mCheckedPosition >= 0) {
            return mList.get(mCheckedPosition);
        }
        return null;
    }

    public void cancelChecked() {
        if (mCheckedPosition >= 0) {
            mList.get(mCheckedPosition).setChecked(false);
            notifyItemChanged(mCheckedPosition, Constants.PAYLOAD);
        }
        mCheckedPosition = -1;
    }

    @Override
    public void refreshData(List<SearchGoodsBean> list) {
        super.refreshData(list);
        mCheckedPosition = -1;
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mThumb;
        TextView mName;
        TextView mPrice;
        ImageView mImgCheck;
        TextView mOriginPrice;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mThumb = itemView.findViewById(R.id.thumb);
            mName = itemView.findViewById(R.id.name);
            mPrice = itemView.findViewById(R.id.price);
            mImgCheck = itemView.findViewById(R.id.img_check);
            mOriginPrice = itemView.findViewById(R.id.origin_price);
            mOriginPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(SearchGoodsBean bean, int position, Object payload) {
            if (payload == null) {
                itemView.setTag(position);
                ImgLoader.display(mContext, bean.getThumb(), mThumb);
                mName.setText(bean.getName());
                mPrice.setText(bean.getPrice());
                if (bean.getType() == 0) {
                    mOriginPrice.setText(null);
                } else {
                    mOriginPrice.setText(bean.getOriginPrice());
                }
            }
            mImgCheck.setImageDrawable(bean.isChecked() ? mCheckedDrawable : null);
        }

    }


}

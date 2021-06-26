package cn.wu1588.mall.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.wu1588.mall.R;
import cn.wu1588.common.adapter.RefreshAdapter;

import cn.wu1588.mall.bean.GoodsClassBean;

public class GoodsClassRightAdapter extends RefreshAdapter<GoodsClassBean> {

    private View.OnClickListener mOnClickListener;

    public GoodsClassRightAdapter(Context context) {
        super(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null && mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick((GoodsClassBean) tag, 0);
                }
            }
        };
    }


    @Override
    public int getItemViewType(int position) {
        if (mList.get(position).isTitle()) {
            return -1;
        }
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int itemType) {
        if (itemType == -1) {
            return new Vh(mInflater.inflate(R.layout.item_goods_class_right_title, viewGroup, false));
        }
        return new NormalVh(mInflater.inflate(R.layout.item_goods_class_right, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
        ((Vh) vh).setData(mList.get(position));
    }

    class Vh extends RecyclerView.ViewHolder {

        TextView mTextView;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.text);
        }

        void setData(GoodsClassBean bean) {
            mTextView.setText(bean.getName());
        }
    }

    class NormalVh extends Vh {

        public NormalVh(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(GoodsClassBean bean) {
            itemView.setTag(bean);
            super.setData(bean);
        }
    }
}

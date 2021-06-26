package cn.wu1588.main.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cn.wu1588.common.adapter.RefreshAdapter;
import cn.wu1588.main.R;
import cn.wu1588.main.activity.ActiveTopicActivity;
import cn.wu1588.main.bean.ActiveTopicBean;

public class ActiveRecomTopicAdapter extends RefreshAdapter<ActiveTopicBean> {

    private View.OnClickListener mOnClickListener;

    public ActiveRecomTopicAdapter(Context context, List<ActiveTopicBean> list) {
        super(context, list);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActiveTopicBean bean = (ActiveTopicBean) v.getTag();
                if (bean != null) {
                    ActiveTopicActivity.forward(mContext, bean.getId(), bean.getName());
                }
            }
        };
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Vh(mInflater.inflate(R.layout.item_active_recom_topic, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i) {
        ((Vh) vh).setData(mList.get(i));
    }

    class Vh extends RecyclerView.ViewHolder {

        TextView mName;

        public Vh(@NonNull View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.name);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(ActiveTopicBean bean) {
            itemView.setTag(bean);
            mName.setText(bean.getName());
        }

    }
}

package cn.wu1588.mall.views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

import cn.wu1588.common.interfaces.OnItemClickListener;
import cn.wu1588.common.views.AbsCommonViewHolder;
import cn.wu1588.mall.R;
import cn.wu1588.mall.activity.PayContentPubActivity;
import cn.wu1588.mall.adapter.PayContentMulAdapter;
import cn.wu1588.mall.bean.PayContentVideoBean;

/**
 * 多视频
 */
public class PayContentMulViewHolder extends AbsCommonViewHolder implements OnItemClickListener<PayContentVideoBean> {

    private RecyclerView mRecyclerView;
    private PayContentMulAdapter mAdapter;
    private int mTargetPosition = -1;

    public PayContentMulViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_pay_content_mul;
    }

    @Override
    public void init() {
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mAdapter = new PayContentMulAdapter(mContext);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
    }


    public void setFilePath(String filePath, String duration) {
        if (mTargetPosition >= 0) {
            mAdapter.setFilePath(filePath, duration, mTargetPosition);
        }
    }

    @Override
    public void onItemClick(PayContentVideoBean bean, int position) {
        mTargetPosition = position;
        ((PayContentPubActivity) mContext).chooseVideo();
    }

    public List<PayContentVideoBean> getList() {
        if (mAdapter != null) {
            return mAdapter.getList();
        }
        return null;
    }
}

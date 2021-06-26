package cn.wu1588.mall.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.android.flexbox.FlexboxLayoutManager;

import java.util.List;

import cn.wu1588.common.Constants;
import cn.wu1588.common.activity.AbsActivity;
import cn.wu1588.common.adapter.RefreshAdapter;
import cn.wu1588.common.custom.CommonRefreshView;
import cn.wu1588.common.http.HttpCallback;
import cn.wu1588.common.interfaces.OnItemClickListener;
import cn.wu1588.common.utils.WordUtil;
import cn.wu1588.mall.R;
import cn.wu1588.mall.adapter.GoodsCommentAdapter;
import cn.wu1588.mall.adapter.GoodsCommentTypeAdapter;
import cn.wu1588.mall.bean.GoodsCommentBean;
import cn.wu1588.mall.bean.GoodsCommentTypeBean;
import cn.wu1588.mall.http.MallHttpConsts;
import cn.wu1588.mall.http.MallHttpUtil;

public class GoodsCommentActivity extends AbsActivity implements OnItemClickListener<GoodsCommentTypeBean> {

    public static void forward(Context context, String goodsId) {
        Intent intent = new Intent(context, GoodsCommentActivity.class);
        intent.putExtra(Constants.MALL_GOODS_ID, goodsId);
        context.startActivity(intent);
    }

    private String mGoodsId;
    private RecyclerView mRecyclerView;
    private GoodsCommentTypeAdapter mTypeAdapter;
    private CommonRefreshView mRefreshView;
    private GoodsCommentAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_gooods_comment;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.mall_292));
        mGoodsId = getIntent().getStringExtra(Constants.MALL_GOODS_ID);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new FlexboxLayoutManager(mContext));
        mTypeAdapter = new GoodsCommentTypeAdapter(mContext);
        mTypeAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mTypeAdapter);
        mRefreshView = findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_goods_comment);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<GoodsCommentBean>() {
            @Override
            public RefreshAdapter<GoodsCommentBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new GoodsCommentAdapter(mContext, true);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                if (mTypeAdapter == null) {
                    return;
                }
                MallHttpUtil.getGoodsCommentList(mGoodsId, mTypeAdapter.getCheckedType(), p, callback);
            }

            @Override
            public List<GoodsCommentBean> processData(String[] info) {
                JSONObject obj = JSON.parseObject(info[0]);
                JSONObject typeNums = obj.getJSONObject("type_nums");
                if (mTypeAdapter != null) {
                    mTypeAdapter.setTypeCount(
                            typeNums.getString("all_nums"),
                            typeNums.getString("img_nums"),
                            typeNums.getString("video_nums"),
                            typeNums.getString("append_nums"));
                }

                return JSON.parseArray(obj.getString("comment_lists"), GoodsCommentBean.class);
            }

            @Override
            public void onRefreshSuccess(List<GoodsCommentBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<GoodsCommentBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        mRefreshView.initData();
    }

    @Override
    protected void onDestroy() {
        MallHttpUtil.cancel(MallHttpConsts.GET_GOODS_COMMENT_LIST);
        super.onDestroy();
    }

    @Override
    public void onItemClick(GoodsCommentTypeBean bean, int position) {
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }
}

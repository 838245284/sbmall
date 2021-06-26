package cn.wu1588.mall.activity;

import android.support.v7.widget.LinearLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.fastjson.JSON;

import java.util.Arrays;
import java.util.List;

import cn.wu1588.common.activity.AbsActivity;
import cn.wu1588.common.adapter.RefreshAdapter;
import cn.wu1588.common.custom.CommonRefreshView;
import cn.wu1588.common.http.HttpCallback;
import cn.wu1588.common.utils.RouteUtil;
import cn.wu1588.common.utils.WordUtil;
import cn.wu1588.mall.R;
import cn.wu1588.mall.adapter.OrderMessageAdapter;
import cn.wu1588.mall.bean.OrderMsgBean;
import cn.wu1588.mall.http.MallHttpConsts;
import cn.wu1588.mall.http.MallHttpUtil;

@Route(path = RouteUtil.PATH_MALL_ORDER_MSG)
public class OrderMessageActivity extends AbsActivity {

    private CommonRefreshView mRefreshView;
    private OrderMessageAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_order_message;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.mall_365));
        mRefreshView = findViewById(R.id.refreshView);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<OrderMsgBean>() {
            @Override
            public RefreshAdapter<OrderMsgBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new OrderMessageAdapter(mContext);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                MallHttpUtil.getOrderMsgList(p, callback);
            }

            @Override
            public List<OrderMsgBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), OrderMsgBean.class);
            }

            @Override
            public void onRefreshSuccess(List<OrderMsgBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<OrderMsgBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        mRefreshView.initData();
    }

    @Override
    protected void onDestroy() {
        MallHttpUtil.cancel(MallHttpConsts.GET_ORDER_MSG_LIST);
        super.onDestroy();
    }
}

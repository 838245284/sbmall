package cn.wu1588.mall.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.alibaba.fastjson.JSON;

import java.util.Arrays;
import java.util.List;

import cn.wu1588.common.Constants;
import cn.wu1588.common.activity.AbsActivity;
import cn.wu1588.common.adapter.RefreshAdapter;
import cn.wu1588.common.custom.CommonRefreshView;
import cn.wu1588.common.http.HttpCallback;
import cn.wu1588.common.utils.WordUtil;
import cn.wu1588.mall.R;
import cn.wu1588.mall.adapter.BuyerAddressAdapter;
import cn.wu1588.mall.bean.BuyerAddressBean;
import cn.wu1588.mall.http.MallHttpConsts;
import cn.wu1588.mall.http.MallHttpUtil;

/**
 * 买家 收货地址
 */
public class BuyerAddressActivity extends AbsActivity implements View.OnClickListener, BuyerAddressAdapter.ActionListener {

    public static void forward(Context context) {
        context.startActivity(new Intent(context, BuyerAddressActivity.class));
    }


    private CommonRefreshView mRefreshView;
    private BuyerAddressAdapter mAdapter;
    private boolean mSetBuyerAddress;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_buyer_address;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.mall_156));
        mSetBuyerAddress = getIntent().getBooleanExtra(Constants.MALL_BUYER_ADDRESS, false);
        findViewById(R.id.btn_add_address).setOnClickListener(this);
        mRefreshView = findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_buyer_address);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<BuyerAddressBean>() {
            @Override
            public RefreshAdapter<BuyerAddressBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new BuyerAddressAdapter(mContext);
                    mAdapter.setActionListener(BuyerAddressActivity.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                MallHttpUtil.getBuyerAddress(callback);
            }

            @Override
            public List<BuyerAddressBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), BuyerAddressBean.class);
            }

            @Override
            public void onRefreshSuccess(List<BuyerAddressBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<BuyerAddressBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        mRefreshView.initData();
    }

    @Override
    public void onClick(View v) {
        forwardEditAddress(null);
    }

    private void forwardEditAddress(BuyerAddressBean bean) {
        Intent intent = new Intent(mContext, BuyerAddressEditActivity.class);
        if (bean != null) {
            intent.putExtra(Constants.MALL_BUYER_ADDRESS, bean);
        }
        startActivityForResult(intent, 100);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 100) {
            if (mRefreshView != null) {
                mRefreshView.initData();
            }
        }
    }

    @Override
    protected void onDestroy() {
        MallHttpUtil.cancel(MallHttpConsts.GET_BUYER_ADDRESS);
        super.onDestroy();
    }

    @Override
    public void onItemClick(BuyerAddressBean bean) {
        if (mSetBuyerAddress) {
            Intent intent = new Intent();
            intent.putExtra(Constants.MALL_BUYER_ADDRESS, bean);
            setResult(RESULT_OK, intent);
            finish();
        }else{
            forwardEditAddress(bean);
        }
    }

    @Override
    public void onEditClick(BuyerAddressBean bean) {
        forwardEditAddress(bean);
    }
}

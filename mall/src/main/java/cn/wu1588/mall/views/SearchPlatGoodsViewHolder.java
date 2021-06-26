package cn.wu1588.mall.views;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;

import java.util.Arrays;
import java.util.List;

import cn.wu1588.common.adapter.RefreshAdapter;
import cn.wu1588.common.custom.CommonRefreshView;
import cn.wu1588.common.http.HttpCallback;
import cn.wu1588.common.interfaces.OnItemClickListener;
import cn.wu1588.common.views.AbsCommonViewHolder;
import cn.wu1588.mall.R;
import cn.wu1588.mall.activity.GoodsSearchActivity;
import cn.wu1588.mall.adapter.SearchPlatGoodsAdapter;
import cn.wu1588.mall.bean.SearchGoodsBean;
import cn.wu1588.mall.http.MallHttpConsts;
import cn.wu1588.mall.http.MallHttpUtil;

/**
 * 搜索平台自营商品
 */
public class SearchPlatGoodsViewHolder extends AbsCommonViewHolder implements OnItemClickListener<SearchGoodsBean> {

    private CommonRefreshView mRefreshView;
    private SearchPlatGoodsAdapter mAdapter;
    private EditText mEditText;
    private Handler mHandler;
    private String mKey;

    public SearchPlatGoodsViewHolder(Context context, ViewGroup parentView, Handler handler) {
        super(context, parentView);
        mHandler = handler;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_search_goods;
    }

    @Override
    public void init() {
        mEditText = findViewById(R.id.edit);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    MallHttpUtil.cancel(MallHttpConsts.SEARCH_GOODS_LIST);
                    if (mHandler != null) {
                        mHandler.removeCallbacksAndMessages(null);
                    }
                    search();
                    return true;
                }
                return false;
            }
        });
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MallHttpUtil.cancel(MallHttpConsts.SEARCH_GOODS_LIST);
                if (mHandler != null) {
                    mHandler.removeCallbacksAndMessages(null);
                    mHandler.sendEmptyMessageDelayed(0, 500);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mRefreshView = findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_goods_seller);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<SearchGoodsBean>() {
            @Override
            public RefreshAdapter<SearchGoodsBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new SearchPlatGoodsAdapter(mContext);
                    mAdapter.setOnItemClickListener(SearchPlatGoodsViewHolder.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                MallHttpUtil.searchPlatGoodsList(mKey, p, callback);
            }

            @Override
            public List<SearchGoodsBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), SearchGoodsBean.class);
            }

            @Override
            public void onRefreshSuccess(List<SearchGoodsBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<SearchGoodsBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
    }


    @Override
    public void loadData() {
        if (isFirstLoadData()) {
            search();
        }
    }

    public SearchGoodsBean getCheckedBean() {
        if (mAdapter != null) {
            return mAdapter.getCheckedBean();
        }
        return null;
    }

    public void cancelCheck() {
        if (mAdapter != null) {
            mAdapter.cancelChecked();
        }
    }

    public void search() {
        mKey = mEditText.getText().toString().trim();
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }

    @Override
    public void onDestroy() {
        mHandler = null;
        super.onDestroy();
    }

    @Override
    public void onItemClick(SearchGoodsBean bean, int position) {
        ((GoodsSearchActivity) mContext).cancelCheckGoods();
        ((GoodsSearchActivity) mContext).cancelCheckPay();
    }
}

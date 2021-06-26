package cn.wu1588.main.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cn.wu1588.common.Constants;
import cn.wu1588.common.activity.AbsActivity;
import cn.wu1588.common.http.HttpCallback;
import cn.wu1588.common.utils.WordUtil;
import cn.wu1588.main.R;
import cn.wu1588.main.adapter.CancelConditionAdapter;
import cn.wu1588.main.bean.CancelConditionBean;
import cn.wu1588.main.http.MainHttpConsts;
import cn.wu1588.main.http.MainHttpUtil;

import java.util.List;

/**
 * 注销条件
 */
public class CancelConditionActivity extends AbsActivity implements View.OnClickListener {

    public static void forward(Context context, String url) {
        Intent intent = new Intent(context, CancelConditionActivity.class);
        intent.putExtra(Constants.URL, url);
        context.startActivity(intent);
    }

    private String mUrl;
    private RecyclerView mRecyclerView;
    private View mBtnNext;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_cancel_condition;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.cancel_account_4));
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mBtnNext = findViewById(R.id.btn_next);
        mBtnNext.setOnClickListener(this);
        mUrl = getIntent().getStringExtra(Constants.URL);
        MainHttpUtil.getCancelCondition(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    if (mRecyclerView != null) {
                        List<CancelConditionBean> list = JSON.parseArray(obj.getString("list"), CancelConditionBean.class);
                        mRecyclerView.setAdapter(new CancelConditionAdapter(mContext, list));
                    }
                    if (mBtnNext != null) {
                        mBtnNext.setEnabled(obj.getIntValue("can_cancel") == 1);
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        CancelAccountActivity.forward(mContext, mUrl);
    }


    @Override
    protected void onDestroy() {
        MainHttpUtil.cancel(MainHttpConsts.GET_CANCEL_CONDITION);
        super.onDestroy();
    }
}

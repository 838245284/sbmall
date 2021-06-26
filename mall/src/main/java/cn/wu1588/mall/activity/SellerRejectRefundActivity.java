package cn.wu1588.mall.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;

import java.util.Arrays;
import java.util.List;

import cn.wu1588.common.Constants;
import cn.wu1588.common.activity.AbsActivity;
import cn.wu1588.common.http.HttpCallback;
import cn.wu1588.common.utils.StringUtil;
import cn.wu1588.common.utils.ToastUtil;
import cn.wu1588.common.utils.WordUtil;
import cn.wu1588.mall.R;
import cn.wu1588.mall.bean.RejectRefundBean;
import cn.wu1588.mall.dialog.SellerRejectReasonDialogFragment;
import cn.wu1588.mall.http.MallHttpConsts;
import cn.wu1588.mall.http.MallHttpUtil;

/**
 * 卖家  拒绝退款
 */
public class SellerRejectRefundActivity extends AbsActivity implements View.OnClickListener {

    public static void forward(Context context, String orderId) {
        Intent intent = new Intent(context, SellerRejectRefundActivity.class);
        intent.putExtra(Constants.MALL_ORDER_ID, orderId);
        ((Activity) context).startActivityForResult(intent, 0);
    }

    private String mOrderId;
    private TextView mReason;
    private EditText mEditText;
    private TextView mCount;
    private List<RejectRefundBean> mReasonList;
    private String mReasonId;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_seller_reject_refund;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.mall_281));
        mOrderId = getIntent().getStringExtra(Constants.MALL_ORDER_ID);
        mReason = findViewById(R.id.reason);
        mEditText = findViewById(R.id.edit);
        mCount = findViewById(R.id.count);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mCount != null) {
                    mCount.setText(StringUtil.contact(String.valueOf(s.length()), "/300"));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mCount = findViewById(R.id.count);
        findViewById(R.id.btn_reason).setOnClickListener(this);
        findViewById(R.id.btn_submit).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_reason) {
            chooseRejectReason();
        } else if (id == R.id.btn_submit) {
            submit();
        }
    }

    /**
     * 选择拒绝原因
     */
    private void chooseRejectReason() {
        if (mReasonList == null) {
            MallHttpUtil.getRejectRefundReason(new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0) {
                        mReasonList = JSON.parseArray(Arrays.toString(info), RejectRefundBean.class);
                        showReasonDialog();
                    }
                }
            });
        } else {
            showReasonDialog();
        }
    }

    private void showReasonDialog() {
        SellerRejectReasonDialogFragment fragment = new SellerRejectReasonDialogFragment();
        fragment.setList(mReasonList);
        fragment.show(getSupportFragmentManager(), "SellerRejectReasonDialogFragment");
    }


    public void setRejectReason(RejectRefundBean bean) {
        mReasonId = bean.getId();
        if (mReason != null) {
            mReason.setText(bean.getName());
        }
    }

    private void submit() {
        if (TextUtils.isEmpty(mReasonId)) {
            ToastUtil.show(R.string.mall_284);
            return;
        }
        String content = mEditText.getText().toString().trim();
        MallHttpUtil.sellerSetRefund(mOrderId, 0, mReasonId, content, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    setResult(RESULT_OK);
                    finish();
                }
                ToastUtil.show(msg);
            }
        });
    }


    @Override
    protected void onDestroy() {
        MallHttpUtil.cancel(MallHttpConsts.GET_REJECT_REFUND_REASON);
        MallHttpUtil.cancel(MallHttpConsts.SELLER_SET_REFUND);
        super.onDestroy();
    }
}

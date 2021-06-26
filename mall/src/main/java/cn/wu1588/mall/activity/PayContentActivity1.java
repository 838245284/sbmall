package cn.wu1588.mall.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cn.wu1588.common.HtmlConfig;
import cn.wu1588.common.activity.AbsActivity;
import cn.wu1588.common.activity.WebViewActivity;
import cn.wu1588.common.http.HttpCallback;
import cn.wu1588.common.utils.DialogUitl;
import cn.wu1588.common.utils.WordUtil;

import cn.wu1588.mall.dialog.PayContentResultDialogFragment;
import cn.wu1588.mall.dialog.PayContentTipDialogFragment;
import cn.wu1588.mall.http.MallHttpConsts;
import cn.wu1588.mall.http.MallHttpUtil;
import cn.wu1588.mall.views.PayBuyViewHolder;
import cn.wu1588.mall.R;
/**
 * 付费内容 未开通
 */
public class PayContentActivity1 extends AbsActivity implements View.OnClickListener {

    public static void forward(Context context) {
        context.startActivity(new Intent(context, PayContentActivity1.class));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_pay_content_1;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.mall_309));
        findViewById(R.id.btn_apply).setOnClickListener(this);
        PayBuyViewHolder viewHolder = new PayBuyViewHolder(mContext, (ViewGroup) findViewById(R.id.content));
        viewHolder.addToParent();
        viewHolder.loadData();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_apply) {
            apply();
        }
    }

    private void apply() {
        MallHttpUtil.getPayApplyStatus(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    if (obj.getIntValue("isauth") == 0) {
                        new DialogUitl.Builder(mContext)
                                .setContent(obj.getString("auth_msg"))
                                .setCancelable(true)
                                .setBackgroundDimEnabled(true)
                                .setConfrimString(WordUtil.getString(R.string.mall_143))
                                .setClickCallback(new DialogUitl.SimpleCallback() {
                                    @Override
                                    public void onConfirmClick(Dialog dialog, String content) {
                                        WebViewActivity.forward(mContext, HtmlConfig.MALL_PAY_AUTH);
                                    }
                                })
                                .build()
                                .show();
                    } else {
                        int applyStatus = obj.getIntValue("apply_status");//付费内容申请状态  -2 没有申请  -1 拒绝   0  审核中  1 同意
                        if (applyStatus == -2) {
                            PayContentTipDialogFragment fragment = new PayContentTipDialogFragment();
                            fragment.setDes(obj.getString("desc"), obj.getString("payment_title"));
                            fragment.show(getSupportFragmentManager(), "PayContentTipDialogFragment");
                        } else if (applyStatus == -1 || applyStatus == 0) {
                            PayContentResultDialogFragment fragment = new PayContentResultDialogFragment();
                            fragment.setResult(applyStatus, obj.getString("desc"), obj.getString("title"));
                            fragment.show(getSupportFragmentManager(), "PayContentResultDialogFragment");
                        }
                    }
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        MallHttpUtil.cancel(MallHttpConsts.GET_PAY_APPLY_STATUS);
        MallHttpUtil.cancel(MallHttpConsts.APPLY_PAY_OPEN);
        super.onDestroy();
    }
}

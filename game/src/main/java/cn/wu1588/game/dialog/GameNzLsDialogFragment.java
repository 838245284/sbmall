package cn.wu1588.game.dialog;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.alibaba.fastjson.JSON;

import java.util.Arrays;
import java.util.List;

import cn.wu1588.common.Constants;
import cn.wu1588.common.dialog.AbsDialogFragment;
import cn.wu1588.common.http.HttpCallback;
import cn.wu1588.common.utils.DpUtil;
import cn.wu1588.common.utils.ToastUtil;
import cn.wu1588.game.R;
import cn.wu1588.game.adapter.GameNzLsAdapter;
import cn.wu1588.game.bean.GameNzLsBean;
import cn.wu1588.game.http.GameHttpConsts;
import cn.wu1588.game.http.GameHttpUtil;

/**
 * Created by cxf on 2018/11/5.
 * 开心牛仔庄家流水
 */

public class GameNzLsDialogFragment extends AbsDialogFragment implements View.OnClickListener {

    private RecyclerView mRecyclerView;

    @Override
    protected int getLayoutId() {
        return R.layout.game_dialog_nz_ls;
    }

    @Override
    protected int getDialogStyle() {
        return R.style.dialog2;
    }

    @Override
    protected boolean canCancel() {
        return false;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = DpUtil.dp2px(280);
        params.height = DpUtil.dp2px(360);
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        String bankerId = bundle.getString(Constants.UID);
        if (TextUtils.isEmpty(bankerId)) {
            return;
        }
        String stream = bundle.getString(Constants.STREAM);
        if (TextUtils.isEmpty(stream)) {
            return;
        }
        mRecyclerView = mRootView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRootView.findViewById(R.id.btn_close).setOnClickListener(this);
        GameHttpUtil.gameNiuBankerWater(bankerId, stream, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    List<GameNzLsBean> list = JSON.parseArray(Arrays.toString(info), GameNzLsBean.class);
                    GameNzLsAdapter adapter = new GameNzLsAdapter(mContext, list);
                    mRecyclerView.setAdapter(adapter);
                } else {
                    ToastUtil.show(msg);
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        dismiss();
    }

    @Override
    public void onDestroy() {
        GameHttpUtil.cancel(GameHttpConsts.GAME_NIU_RECORD);
        super.onDestroy();
    }
}

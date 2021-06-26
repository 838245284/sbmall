package cn.wu1588.live.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import cn.wu1588.common.CommonAppConfig;
import cn.wu1588.common.Constants;
import cn.wu1588.common.activity.AbsActivity;
import cn.wu1588.common.bean.ConfigBean;
import cn.wu1588.common.bean.LiveClassBean;
import cn.wu1588.common.interfaces.CommonCallback;
import cn.wu1588.common.interfaces.OnItemClickListener;
import cn.wu1588.common.utils.WordUtil;
import cn.wu1588.live.R;
import cn.wu1588.live.adapter.LiveReadyClassAdapter;

/**
 * Created by cxf on 2018/10/7.
 * 选择直播频道
 */

public class LiveChooseClassActivity extends AbsActivity implements OnItemClickListener<LiveClassBean> {

    private RecyclerView mRecyclerView;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_live_choose_class;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.live_class_choose));
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        final int checkedClassId = getIntent().getIntExtra(Constants.CLASS_ID, 0);
        CommonAppConfig.getInstance().getConfig(new CommonCallback<ConfigBean>() {
            @Override
            public void callback(ConfigBean configBean) {
                if (configBean != null) {
                    List<LiveClassBean> list = configBean.getLiveClass();
                    if (list == null) {
                        return;
                    }
                    for (int i = 0, size = list.size(); i < size; i++) {
                        LiveClassBean bean = list.get(i);
                        if (bean.getId() == checkedClassId) {
                            bean.setChecked(true);
                        } else {
                            bean.setChecked(false);
                        }
                    }
                    LiveReadyClassAdapter adapter = new LiveReadyClassAdapter(mContext, list);
                    adapter.setOnItemClickListener(LiveChooseClassActivity.this);
                    mRecyclerView.setAdapter(adapter);
                }
            }
        });
    }


    @Override
    public void onItemClick(LiveClassBean bean, int position) {
        Intent intent = new Intent();
        intent.putExtra(Constants.CLASS_ID, bean.getId());
        intent.putExtra(Constants.CLASS_NAME, bean.getName());
        setResult(RESULT_OK, intent);
        finish();
    }
}

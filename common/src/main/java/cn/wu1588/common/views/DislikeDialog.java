package cn.wu1588.common.views;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bytedance.sdk.openadsdk.DislikeInfo;
import com.bytedance.sdk.openadsdk.FilterWord;
import com.bytedance.sdk.openadsdk.PersonalizationPrompt;
import com.bytedance.sdk.openadsdk.TTDislikeDialogAbstract;
import com.bytedance.sdk.openadsdk.dislike.TTDislikeListView;

import java.util.ArrayList;
import java.util.List;

import cn.wu1588.common.R;

/**
 * Create by hanweiwei on 14/12/2018
 */
public class DislikeDialog extends TTDislikeDialogAbstract {
    final DislikeInfo mDislikeInfo;
    final List<FilterWord> mList;
    final PersonalizationPrompt mPersonalizationPrompt;
    private OnDislikeItemClick mOnDislikeItemClick;
    private OnPersonalizationPromptClick mOnPersonalizationPromptClick;

    public DislikeDialog(@NonNull Context context, DislikeInfo dislikeInfo) {
        super(context);
        mDislikeInfo = dislikeInfo;
        mList = initData(dislikeInfo.getFilterWords());
        mPersonalizationPrompt = dislikeInfo.getPersonalizationPrompt();
    }

    public void setOnDislikeItemClick(OnDislikeItemClick onDislikeItemClick) {
        mOnDislikeItemClick = onDislikeItemClick;
    }

    public void setOnPersonalizationPromptClick(OnPersonalizationPromptClick onClickListener) {
        mOnPersonalizationPromptClick = onClickListener;
    }

    private List<FilterWord> initData(List<FilterWord> list) {
        //目前网盟的负反馈存在二级选项，需要特殊处理
        List<FilterWord> data = new ArrayList<>();
        if (list != null) {
            for (FilterWord filterWord : list) {
                if (filterWord.hasSecondOptions()) {
                    data.addAll(initData(filterWord.getOptions()));
                } else {
                    data.add(filterWord);
                }
            }
        }
        return data;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TTDislikeListView listView = (TTDislikeListView) findViewById(R.id.lv_dislike_custom);
        listView.setAdapter(new MyDislikeAdapter());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DislikeDialog.this.dismiss();

                if (mOnDislikeItemClick != null) {
                    FilterWord word = null;
                    try {
                        word = (FilterWord) parent.getAdapter().getItem(position);
                    } catch (Throwable ignore) {
                    }
                    mOnDislikeItemClick.onItemClick(word);
                }
            }
        });
        if (mPersonalizationPrompt != null) {
            TextView textView = findViewById(R.id.tv_personalize_prompt);
            textView.setVisibility(View.VISIBLE);
            textView.setText(mPersonalizationPrompt.getName());
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnPersonalizationPromptClick != null) {
                        mOnPersonalizationPromptClick.onClick(mPersonalizationPrompt);
                    }
                    // 跳转到为什么看到此广告界面
                    startPersonalizePromptActivity();
                }
            });
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.dlg_dislike_custom;
    }

    @Override
    public int[] getTTDislikeListViewIds() {
        return new int[]{R.id.lv_dislike_custom};
    }

    @Override
    public ViewGroup.LayoutParams getLayoutParams() {
        return null;
    }

    class MyDislikeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mList == null ? 0 : mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList == null ? null : mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FilterWord word = (FilterWord) getItem(position);

            TextView textView = new TextView(getContext());
            textView.setPadding(40, 40, 40, 40);
            textView.setTextColor(Color.BLACK);
            textView.setGravity(Gravity.CENTER);
            textView.setText(word.getName());
            notifyDataSetChanged();

            return textView;
        }
    }

    public interface OnDislikeItemClick {
        void onItemClick(FilterWord filterWord);
    }

    public interface OnPersonalizationPromptClick {
        void onClick(PersonalizationPrompt personalizationPrompt);
    }
}

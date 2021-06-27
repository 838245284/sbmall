package cn.wu1588.video.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import cn.wu1588.common.mob.CoverBean;
import cn.wu1588.video.R;

/**
 * Created by cxf on 2018/10/19.
 * 视频封面
 */

public class VideoCoverAdapter extends RecyclerView.Adapter<VideoCoverAdapter.Vh> {

    private List<CoverBean> mList;
    private LayoutInflater mInflater;
    private View.OnClickListener mOnClickListener;
    private int mCheckedPosition;
    private Context mContext;

    public List<CoverBean> getList() {
        return mList;
    }

    public VideoCoverAdapter(Context context) {
        mList = new ArrayList<>();
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mCheckedPosition = -1;
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag == null) {
                    return;
                }
                int position = (int) tag;
                for (CoverBean coverBean : mList) {
                    coverBean.setChecked(false);
                }

                CoverBean bean = mList.get(position);
                bean.setChecked(true);
                notifyDataSetChanged();
                mCheckedPosition = position;
            }
        };
    }


    @NonNull
    @Override
    public Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_video_pub_cover, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Vh vh, int position) {

    }

    @Override
    public void onBindViewHolder(@NonNull Vh vh, int position, @NonNull List<Object> payloads) {
        ((Vh) vh).setData(mList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class Vh extends RecyclerView.ViewHolder {

        ImageView mIcon;
        View mCheck;

        public Vh(View itemView) {
            super(itemView);
            mIcon = (ImageView) itemView.findViewById(R.id.image);
            mCheck = itemView.findViewById(R.id.check);
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(CoverBean bean, int position) {
            itemView.setTag(position);
            mCheck.setVisibility(bean.isChecked() ? View.VISIBLE : View.GONE);
//            ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
//            layoutParams.width = bean.getWidth();
//            layoutParams.height = bean.getHight();
//            itemView.setLayoutParams(layoutParams);

//            mIcon.setLayoutParams(new FrameLayout.LayoutParams(bean.getWidth(), bean.getHight()));
//            ImgLoader.display(mContext, bean.getBitmap(), mIcon);
//            mIcon.setImageBitmap(bean.getBitmap());
            Glide.with(mContext)
                    .load(bean.getBitmap())
                    .override(bean.getWidth(), bean.getHight())
                    .into(mIcon);
//
        }
    }

}

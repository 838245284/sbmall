package cn.wu1588.main.adapter;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Iterator;

import cn.wu1588.common.adapter.RefreshAdapter;
import cn.wu1588.common.bean.UserBean;
import cn.wu1588.common.glide.ImgLoader;
import cn.wu1588.common.http.HttpCallback;
import cn.wu1588.common.utils.DensityUtils;

import cn.wu1588.common.utils.ToastUtil;
import cn.wu1588.main.R;
import cn.wu1588.video.activity.VideoRePublishActivity;
import cn.wu1588.video.bean.VideoBean;
import cn.wu1588.video.http.VideoHttpUtil;

/**
 * Created by cxf on 2018/12/14.
 */

public class MyLongVideoAdapter extends RefreshAdapter<VideoBean> {

    private View.OnClickListener mOnClickListener;

    public MyLongVideoAdapter(Context context) {
        super(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag == null) {
                    return;
                }
                int position = (int) tag;
                VideoBean bean = mList.get(position);
                if (bean != null && mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(bean, position);
                }
            }
        };
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Vh(mInflater.inflate(R.layout.item_my_video_long, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
        ((Vh) vh).setData(mList.get(position), position);
    }

    /**
     * 删除视频
     */
    public void deleteVideo(final String videoId) {
        if (TextUtils.isEmpty(videoId)) {
            return;
        }
        VideoHttpUtil.videoDelete(videoId, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if(code==0){
                    Iterator<VideoBean> iterator = mList.iterator();
                    while (iterator.hasNext()){
                        VideoBean next = iterator.next();
                        if(videoId.equals(next.getId())){
                            iterator.remove();
                        }
                    }
                    notifyDataSetChanged();
                    ToastUtil.show("删除成功");
                }
            }
        });

    }

    class Vh extends RecyclerView.ViewHolder {
        private final TextView mPlayCount;
        private final TextView mDate;
        ImageView mCover;
        TextView mTitle;
        private final TextView mTime;
        private String id;
        private VideoBean bean;

        public Vh(View itemView) {
            super(itemView);
            mCover = (ImageView) itemView.findViewById(R.id.cover);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mTime = (TextView) itemView.findViewById(R.id.time);
            mPlayCount = (TextView) itemView.findViewById(R.id.tv_play);
            mDate = (TextView) itemView.findViewById(R.id.tv_date);
            itemView.findViewById(R.id.oprate).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSetDialog(v);
                }
            });
            itemView.setOnClickListener(mOnClickListener);
        }

        void setData(VideoBean bean, int position) {
            id = bean.getId();
            this.bean = bean;
            itemView.setTag(position);
            ImgLoader.display(mContext, bean.getThumb(), mCover);
            mTitle.setText(bean.getTitle());
            UserBean userBean = bean.getUserBean();
            /*if (userBean != null) {
                ImgLoader.display(mContext, userBean.getAvatar(), mAvatar);

            }*/
            mPlayCount.setText(bean.getViewNum()+"播放");
            mDate.setText(bean.getDatetime()); //
            mTime.setText(bean.getVideo_time());
        }

        private void showSetDialog(View v) {
            View view = View.inflate(mContext, R.layout.dialog_jigou, null);
            final Dialog dialog = new Dialog(mContext);
            dialog.setContentView(view);
            Window window = dialog.getWindow();
            //设置弹出位置
            window.setGravity(Gravity.BOTTOM);
            //设置弹出动画
            window.setWindowAnimations(R.style.popwindow_bottom_anim);
            window.setBackgroundDrawableResource(R.color.transparent);
            //设置对话框大小
//        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            WindowManager.LayoutParams lp = window.getAttributes();
            int width = DensityUtils.getScreenW(mContext);
            lp.width = width;
            window.setAttributes(lp);
            dialog.show();
            view.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    VideoRePublishActivity.forward(mContext,bean);
                }
            });
            view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            view.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteVideo(id);
                    dialog.dismiss();
                }
            });
        }

    }
}

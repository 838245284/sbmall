package cn.wu1588.video.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import cn.wu1588.beauty.simple.SimpleFilterAdapter;
import cn.wu1588.beauty.simple.SimpleFilterBean;
import cn.wu1588.common.interfaces.OnItemClickListener;
import cn.wu1588.common.utils.BitmapUtil;
import cn.wu1588.common.views.AbsViewHolder;
import cn.wu1588.video.R;

/**
 * Created by cxf on 2018/12/8.
 * 视频编辑 滤镜
 */

public class VideoEditFilterViewHolder extends AbsViewHolder implements OnItemClickListener<SimpleFilterBean>, View.OnClickListener {

    private ActionListener mActionListener;
    private boolean mShowed;
//    private Bitmap mBitmap;


    public VideoEditFilterViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_video_edit_filter;
    }

    @Override
    public void init() {
        findViewById(R.id.root).setOnClickListener(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        SimpleFilterAdapter adapter = new SimpleFilterAdapter(mContext);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(SimpleFilterBean bean, int position) {
//        if (mBitmap != null) {
//            mBitmap.recycle();
//        }
        if (mActionListener != null) {
            int filterSrc = bean.getFilterSrc();
            if (filterSrc != 0) {
                Bitmap bitmap = BitmapUtil.getInstance().decodeBitmap(filterSrc);
                if (bitmap != null) {
//                    mBitmap = bitmap;
                    mActionListener.onFilterChanged(bitmap);
                } else {
                    mActionListener.onFilterChanged(null);
                }
            } else {
                mActionListener.onFilterChanged(null);
            }
        }
    }

    public void show() {
        mShowed = true;
        if (mContentView != null && mContentView.getVisibility() != View.VISIBLE) {
            mContentView.setVisibility(View.VISIBLE);
        }
    }

    public void hide() {
        mShowed = false;
        if (mContentView != null && mContentView.getVisibility() == View.VISIBLE) {
            mContentView.setVisibility(View.INVISIBLE);
        }
        if (mActionListener != null) {
            mActionListener.onHide();
        }
    }

    @Override
    public void onClick(View v) {
        hide();
    }

    public interface ActionListener {
        void onHide();

        void onFilterChanged(Bitmap bitmap);
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public void release() {
        mActionListener = null;
//        if (mBitmap != null) {
//            mBitmap.recycle();
//        }
//        mBitmap = null;
    }

    public boolean isShowed() {
        return mShowed;
    }
}

package cn.wu1588.beauty.ui.views;

import android.content.Context;
import android.view.ViewGroup;

import com.meihu.beautylibrary.MHSDK;
import cn.wu1588.beauty.ui.interfaces.StickerCanClickListener;
import cn.wu1588.common.utils.ToastUtil;

public class BeautyViewHolderFactory {

    public static BaseBeautyViewHolder getBeautyViewHolder(Context context, ViewGroup viewGroup, StickerCanClickListener canClickListener) {
        BaseBeautyViewHolder beautyViewHolder;
        String ver = MHSDK.getInstance().getVer();
        if (ver == null) {
            ToastUtil.show("授权校验异常");
            ver = "-1";
        }
        switch (ver) {
            case "0":
                beautyViewHolder = new DefaultBeautyViewHolder(context, viewGroup);
                break;
            case "1":
                beautyViewHolder = new BeautyViewHolder(context, viewGroup, canClickListener);
                break;
            default:
                beautyViewHolder = new DefaultBeautyViewHolder(context, viewGroup);
                break;
        }
        return beautyViewHolder;
    }
}

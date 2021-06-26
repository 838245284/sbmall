package cn.wu1588.live.interfaces;

/**
 * Created by cxf on 2018/10/7.
 */

public interface LivePushListener {
    void onPreviewStart();

    void onPushStart();

    void onPushFailed();
}

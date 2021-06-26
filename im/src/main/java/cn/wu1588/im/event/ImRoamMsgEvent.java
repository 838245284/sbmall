package cn.wu1588.im.event;


import cn.wu1588.im.bean.ImUserBean;

/**
 * Created by cxf on 2018/7/20.
 * IM漫游消息 事件
 */

public class ImRoamMsgEvent {

    private ImUserBean mBean;

    public ImRoamMsgEvent(ImUserBean bean){
        mBean=bean;
    }

}

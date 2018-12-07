package com.houwei.guaishang.huanxin;

import android.util.Log;
import android.widget.RelativeLayout;

import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.cloud.EMHttpClient;
import com.houwei.guaishang.tools.LogUtil;

/**
 * create by lei on 2018/12/5/005
 * desc:
 */
public class HuanXinUtil {

    private static class SingleInstance{
        private static HuanXinUtil single = new HuanXinUtil();
    }

    public HuanXinUtil() {

    }

    public static HuanXinUtil g(){
        return SingleInstance.single;
    }


    public int getUnReadCount(String userName){
        EMConversation conversation = EMChatManager.getInstance().getConversation(userName);
        int unreadMsgCount = conversation.getUnreadMsgCount();
        Log.d("lei","获取消息 数量："+unreadMsgCount);
        return unreadMsgCount;
    }
}

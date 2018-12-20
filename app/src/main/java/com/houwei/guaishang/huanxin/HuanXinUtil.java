package com.houwei.guaishang.huanxin;

import android.util.Log;
import android.widget.RelativeLayout;

import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.cloud.EMHttpClient;
import com.easemob.exceptions.EaseMobException;
import com.houwei.guaishang.tools.LogUtil;

import java.util.List;

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


    public int getUnReadCount(String userName,String topicId){
        EMConversation conversation = EMChatManager.getInstance().getConversation(userName);
        int unreadMsgCount = 0;
        List<EMMessage> allMessages = conversation.getAllMessages();
        for (EMMessage msg :
                allMessages) {
            try {
                if (msg.isUnread()) {
                    String msgTopicId = msg.getStringAttribute("topicId");
                    if (topicId.equalsIgnoreCase(msgTopicId)) {
                        unreadMsgCount++;
                    }
                }
            } catch (EaseMobException e) {
                e.printStackTrace();
            }
        }
        Log.d("lei","获取消息 数量："+unreadMsgCount);
        return unreadMsgCount;
    }
}

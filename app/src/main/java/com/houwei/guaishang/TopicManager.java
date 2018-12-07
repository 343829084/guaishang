package com.houwei.guaishang;

import com.houwei.guaishang.sp.DataStorage;
import com.houwei.guaishang.sp.UserUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * create by lei on 2018/12/5/005
 * desc:topic排序管理器
 */
public class TopicManager {

    private static ArrayList<String> topicList;//需要本地排序的订单列表

    private static HashMap<String,ArrayList<String>> topicMap;

    private static class SingleInstance{
        private static TopicManager topicManager = new TopicManager();
    }

    public TopicManager() {
        topicList = new ArrayList<>();
        topicMap = new HashMap<>();
    }

    public static TopicManager g(){
        return SingleInstance.topicManager;
    }

    /**
     * 本地列表增加1个订单id
     * @param topic 订单id
     */
    public void addTopic(String topic){
        if (topicList == null){
            return;
        }
        if (topicList.contains(topic)){
            topicList.remove(topic);
        }
        topicList.add(topic);
//        topicMap.put(UserUtil.getUserInfo().getUserId(),topicList);
        saveList();
    }

    public void  remove(String topic){
        if (topicList != null && topicList.contains(topic)){
            topicList.remove(topic);
        }
        saveList();
    }

    //本地存储列表
    private void saveList(){
        DataStorage.saveComObject("TopicList",topicList);
    }

    //获取本地存储的订单列表
    public ArrayList<String> getList(){
        return (ArrayList<String>) DataStorage.getComObject("TopicList");
    }

}

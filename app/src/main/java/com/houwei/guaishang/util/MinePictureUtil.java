package com.houwei.guaishang.util;

import android.content.Context;
import android.text.TextUtils;

import com.houwei.guaishang.bean.UserBean;
import com.houwei.guaishang.manager.ITopicApplication;
import com.luck.picture.lib.entity.LocalMedia;

import java.util.HashMap;

/**
 * create by lei on 2018/11/15/015
 * desc:
 */
public class MinePictureUtil {

    private static HashMap<String,String> picture = new HashMap<>();

    public static void add(String key,String value){
        if (picture != null){
            picture.put(key,value);
        }
    }

    public static String get(String key){
        return picture.get(key);
    }



    public static void deletePic(ITopicApplication context,String path){

    }


}

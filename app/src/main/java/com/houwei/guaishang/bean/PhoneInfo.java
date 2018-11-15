package com.houwei.guaishang.bean;

import java.io.Serializable;

/**
 * Created by ** on 2018/7/31.
 */

public class PhoneInfo implements Serializable{

    private String name;
    private String mobile;

    public PhoneInfo(String name, String mobile) {
        this.name = name;
        this.mobile = mobile;
    }
}

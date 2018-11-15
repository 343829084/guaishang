package com.houwei.guaishang.bean;

import java.io.Serializable;

/**
 * Created by ** on 2018/7/31.
 */

public class SMSInfo implements Serializable{

    private String name;
    private String number;
    private String mess;

    public SMSInfo(String name, String number, String mess) {
        this.name = name;
        this.number = number;
        this.mess = mess;
    }
}

package com.houwei.guaishang.bean;

/**
 * create by lei on 2018/12/5/005
 * desc:
 */
public class ReleaseTopicResponse {
    private int code;
    private String message;
    private String data;

    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess(){
        return code == 1;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}

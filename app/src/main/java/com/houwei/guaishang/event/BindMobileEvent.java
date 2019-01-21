package com.houwei.guaishang.event;

import com.houwei.guaishang.bean.UserResponse;

public class BindMobileEvent {

    private UserResponse response;


    public BindMobileEvent() {
    }

    public UserResponse getResponse() {
        return response;
    }

    public void setResponse(UserResponse response) {
        this.response = response;
    }
}

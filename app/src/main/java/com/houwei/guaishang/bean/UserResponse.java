package com.houwei.guaishang.bean;


import java.io.Serializable;

public class UserResponse extends BaseResponse implements Serializable {

	private UserBean data;

	public UserBean getData() {
		return data;
	}

	public void setData(UserBean data) {
		this.data = data;
	}

}

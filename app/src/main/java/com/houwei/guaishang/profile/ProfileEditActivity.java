package com.houwei.guaishang.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.houwei.guaishang.R;
import com.houwei.guaishang.activity.BaseActivity;
import com.houwei.guaishang.sp.UserUtil;
import com.houwei.guaishang.tools.HttpUtil;
import com.houwei.guaishang.tools.ToastUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by lenovo on 2018/4/27.
 * 修改个人信息的页面
 */

public class ProfileEditActivity extends BaseActivity {

    public static final int NAME = 10001;//修改名字
    public static final int Phone = 10002;//修改手机
    public static final int GuDing_Phone = 10003;//修改固定电话
    public static final int Address = 10004;//修改地址
    public static final int Bank = 10005;//修改开户行
    public static final int Bank_Num = 10006;//修改开户行账号

    public static final String Parse_intent = "ParseIntent";
    public static final String Parse_extra = "extra";
    @BindView(R.id.save)
    TextView save;
    @BindView(R.id.edit_tv)
    EditText editTv;


    private int type;//1是名字  2 是手机 3 是固定电话  4 是地址 5 是开户行  6是开户行账号

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        ButterKnife.bind(this);

        parseIntent();
    }

    private void parseIntent() {
        if (getIntent() != null) {
            type = getIntent().getIntExtra(Parse_intent, 0);
            editTv.setText(getIntent().getStringExtra(Parse_extra));
        }
    }

    private void modify(String event, final String toastTv) {
        if (!TextUtils.isEmpty(editTv.getText().toString())){
            OkGo.<String>post(HttpUtil.IP + "user/modify")
                    .params("userid", UserUtil.getUserInfo().getUserId())
                    .params("event", event)
                    .params("value", editTv.getText().toString())
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            Intent intent = new Intent();
                            intent.putExtra("result",editTv.getText().toString());
                            setResult(RESULT_OK,intent);
                            finish();
                        }
                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            ToastUtils.toastForShort(ProfileEditActivity.this,toastTv);
                        }
                    });
        }else {
            ToastUtils.toastForShort(this,"请输入内容");
        }
    }


    @OnClick(R.id.save)
    public void onClick() {
        switch (type) {
            case 1:
                modify("name","修改名字失败");
                break;
            case 2:
                modify("mobile","修改电话失败");
                break;
            case 3:
                modify("guding_phone","修改固定电话失败");
                break;
            case 4:
                modify("address","修改地址失败");
                break;
            case 5:
                modify("bank","修改开户行失败");
                break;
            case 6:
                modify("bank_num","修改开户行账号失败");
                break;
        }
    }

}

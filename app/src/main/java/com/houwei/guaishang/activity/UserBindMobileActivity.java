package com.houwei.guaishang.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.houwei.guaishang.R;
import com.houwei.guaishang.bean.UserResponse;
import com.houwei.guaishang.data.DBReq;
import com.houwei.guaishang.event.BindMobileEvent;
import com.houwei.guaishang.event.LoginSuccessEvent;
import com.houwei.guaishang.sp.UserInfo;
import com.houwei.guaishang.sp.UserUtil;
import com.houwei.guaishang.tools.HttpUtil;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class UserBindMobileActivity extends BaseActivity {


    @BindView(R.id.phone_et)
    EditText phoneEt;
    @BindView(R.id.lable)
    TextView lable;
    @BindView(R.id.check_pw_et)
    EditText checkPwEt;
    @BindView(R.id.check_pw_get_btn)
    Button checkPwGetBtn;
    @BindView(R.id.next_btn)
    Button nextBtn;

    private final int DELAYTIME = 60;
    private int CURRENTDELAYTIME;

    private UserResponse mUserresponse;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bind_mobile_activity);
        ButterKnife.bind(this);
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        if (getIntent() != null){
            mUserresponse = (UserResponse) getIntent().getSerializableExtra("UserResponse");
        }
        myHandler = new MyHandler(this);
        timer = new Timer();
        initProgressDialog(false, null);

        EventHandler eh=new EventHandler(){
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
//				Log.d("CCC","e:"+event+"-re:"+result+"-data:"+data);
                smshandler.sendMessage(msg);
            }
        };
        SMSSDK.registerEventHandler(eh);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (task != null) {
            task.cancel();
            timer.cancel();
            timer = null;
            task = null;
        }
        SMSSDK.unregisterAllEventHandler();
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }

    @OnClick({R.id.check_pw_get_btn, R.id.next_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.check_pw_get_btn:
                if (phoneEt.getText().toString().equals("")) {
                    showErrorToast("请输入手机号码");
                    return;
                }
                if (checkPwGetBtn.isClickable()) {
                    checkPwGetBtn.setClickable(false);
                    checkPwGetBtn.setText(DELAYTIME+"秒后重获");
                }
                progress.show();
                SMSSDK.getVerificationCode("86",phoneEt.getText().toString().trim());
                break;
            case R.id.next_btn:
                if (phoneEt.getText().toString().equals("")) {
                    showErrorToast("请输入手机号码");
                    return;
                }
                if (mUserresponse != null && mUserresponse.getData() != null) {
                    updatePhoneNum(phoneEt.getText().toString(), mUserresponse.getData().getUserid());
                }
                break;
        }
    }

    private void updatePhoneNum(String phone,String userid) {
        OkGo.<String>post(HttpUtil.IP + "user/modify1")
                .params("userid", userid)
                .params("event", "mobile")
                .params("value", phone)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        BindMobileEvent bindMobileEvent = new BindMobileEvent();
                        mUserresponse.getData().setMobile(phoneEt.getText().toString());
                        bindMobileEvent.setResponse(mUserresponse);
                        EventBus.getDefault().post(bindMobileEvent);
                        finish();
                    }
                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                    }
                });
    }


    private MyHandler myHandler;
    public static class MyHandler extends Handler {

        private WeakReference<Context> reference;

        public MyHandler(Context context) {
            reference = new WeakReference<Context>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            final UserBindMobileActivity activity = (UserBindMobileActivity) reference.get();
            activity.progress.dismiss();
            if(activity == null){
                return;
            }
            switch (msg.what) {
                case NETWORK_OTHER: // 验证码倒计时
                    if (activity.CURRENTDELAYTIME <= 0) {
                        activity.cancelTime();
                    } else {
                        activity.CURRENTDELAYTIME--;
                        activity.checkPwGetBtn.setText(activity.CURRENTDELAYTIME + "秒后重获");
                    }
                    break;

                default:
                    activity.progress.dismiss();
                    activity.showErrorToast();
                    break;
            }
        }
    }


    private SMShandler smshandler = new SMShandler(this);
    private static class SMShandler extends Handler {

        private WeakReference<Context> reference;

        public SMShandler(Context context) {
            reference = new WeakReference<Context>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            final UserBindMobileActivity activity = (UserBindMobileActivity) reference.get();
            activity.progress.dismiss();
            if(activity == null){
                return;
            }

            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;
            if (result == SMSSDK.RESULT_COMPLETE) {
                if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE || event == SMSSDK.EVENT_GET_VOICE_VERIFICATION_CODE){
                    activity.showErrorToast("验证码已经发送");
                    activity.checkPwGetBtn.setClickable(false);
                    activity.checkPwGetBtn.setText(activity.DELAYTIME+"秒后重获");
                    activity.startTime();
                } else if (event ==SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){//返回支持发送验证码的国家列表
                    activity.showErrorToast("获取国家列表成功");
                }
            } else {
                activity.showErrorToast("验证码错误");
            }
        }
    }


    private TimerTask task;
    private Timer timer;

    private void startTime() {
        CURRENTDELAYTIME = DELAYTIME;
        task = new TimerTask() {

            @Override
            public void run() {
                myHandler.sendEmptyMessage(NETWORK_OTHER);
            }
        };
        timer.schedule(task, 0, 1000);
    }

    private void cancelTime() {
        if (task!=null) {
            task.cancel();
        }
        checkPwGetBtn.setClickable(true);
        checkPwGetBtn.setText("获取验证码");
    }


    //接收登录登出事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void on3EventMainThread(LoginSuccessEvent event){
    }
}

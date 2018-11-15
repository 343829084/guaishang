package com.houwei.guaishang.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;

import com.houwei.guaishang.MessageEvent;
import com.houwei.guaishang.R;
import com.houwei.guaishang.adapter.OrderChatAdapter;
import com.houwei.guaishang.adapter.OrderChatViewPagerAdapter;
import com.houwei.guaishang.bean.OffersBean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class OrderChatActivity extends BaseActivity implements View.OnClickListener,OrderChatAdapter.AdapterItemClickListener {

    public static final String Parse_List = "parseList";
    public static final String SID = "SID";
    public static final String OrderId = "OrderId";
    public static final String Brand = "Brand";
    public static final String ALONE = "alone";
    public static final String ShouldOffer = "ShouldOffer";
    private ViewPager mViewPager;
    private RecyclerView mRecyclerView;
    private OrderChatAdapter mAdapter;
    private  List<OffersBean.OfferBean> offerPriceList;
    private List<Fragment> fragments;

    private String sid;
    private String orderId;
    private String brand;
    private boolean alone;
    private boolean shouldOffer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_order_chat);
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        parseIntent();
        initData();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }

    private void initData() {
//        list = new ArrayList();
        fragments = new ArrayList<>();

//        Intent intent = getIntent();
//        orderId = intent.getLongExtra("orderId",0);
//        //测试数据
//        for(int i = 0; i < 20; i++) {
//            UserBean userBean = new UserBean();
//            AvatarBean bean = new AvatarBean();
//            bean.setOriginal("https://www.baidu.com/img/bd_logo1.png");
//            bean.setSmall("https://www.baidu.com/img/bd_logo1.png");
//            userBean.setAvatar(bean);
//            userBean.setName("测试数据"+i);
//            userBean.setUserid("641");
//            list.add(userBean);
//        }
    }
//                  解析的
    private void parseIntent(){
        Intent intent = getIntent();
        if (intent != null){
            offerPriceList = (ArrayList<OffersBean.OfferBean>) intent.getSerializableExtra(Parse_List);
            sid = intent.getStringExtra(SID);
            orderId = intent.getStringExtra(OrderId);
            brand = intent.getStringExtra(Brand);
            alone = intent.getBooleanExtra(ALONE,false);
            shouldOffer = intent.getBooleanExtra(ShouldOffer,false);
        }
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        LinearLayoutManager layoutmanager = new LinearLayoutManager(this);
        //设置RecyclerView 布局
        mRecyclerView.setLayoutManager(layoutmanager);
        //设置Adapter
//            报价列表
        if (offerPriceList == null) {
            offerPriceList = new ArrayList<>();
        }
        mAdapter = new OrderChatAdapter(offerPriceList);
        mAdapter.setItemOnclickListener(this);
        mRecyclerView.setAdapter(mAdapter);
//         只有一个人
        if (alone) {
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
        }

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(new OrderChatViewPagerAdapter(getSupportFragmentManager(),offerPriceList,sid,orderId,brand,alone,shouldOffer));

        findViewById(R.id.ll_back).setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                finish();
                break;
        }
    }

    @Override
    public void onItemClick(int postion, String userId) {
        //fragment聊天页面 切换
        mViewPager.setCurrentItem(postion);
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(postion);

        int size = offerPriceList.size();
        for (int i = 0; i < size; i++) {
            if (userId.equals(offerPriceList.get(i).getUserid())){
                offerPriceList.get(i).setNotify(false);
            }
        }
    }


    @Subscribe (threadMode = ThreadMode.MAIN)
    public void on3EventMainThread(MessageEvent messageEvent){
        String id = messageEvent.getId();
        int size = offerPriceList.size();
        for (int i = 0; i < size; i++) {
            if (id.equals(offerPriceList.get(i).getUserid())){
                offerPriceList.get(i).setNotify(true);
            }
        }
    }

}

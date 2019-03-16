package com.houwei.guaishang.layout;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.houwei.guaishang.R;
import com.houwei.guaishang.TopicManager;
import com.houwei.guaishang.activity.BaseActivity;
import com.houwei.guaishang.activity.Constant;
import com.houwei.guaishang.activity.OrderChatActivity;
import com.houwei.guaishang.adapter.HomeOrderGridAdapter;
import com.houwei.guaishang.adapter.OfferAdapter;
import com.houwei.guaishang.bean.AvatarBean;
import com.houwei.guaishang.bean.CommentBean;
import com.houwei.guaishang.bean.OffersBean;
import com.houwei.guaishang.bean.Payment;
import com.houwei.guaishang.bean.PraiseBean;
import com.houwei.guaishang.bean.TopicBean;
import com.houwei.guaishang.easemob.EaseConstant;
import com.houwei.guaishang.easemob.PreferenceManager;
import com.houwei.guaishang.event.LoginSuccessEvent;
import com.houwei.guaishang.huanxin.HuanXinUtil;
import com.houwei.guaishang.layout.PictureGridLayout.RedPacketClickListener;
import com.houwei.guaishang.manager.FaceManager;
import com.houwei.guaishang.sp.DataStorage;
import com.houwei.guaishang.sp.UserUtil;
import com.houwei.guaishang.tools.DealResult;
import com.houwei.guaishang.tools.HttpUtil;
import com.houwei.guaishang.tools.ToastUtils;
import com.houwei.guaishang.view.OrderBuyDialog;
import com.houwei.guaishang.view.ProgressView;
import com.houwei.guaishang.views.CircleBitmapDisplayer1;
import com.houwei.guaishang.views.SpannableTextView;
import com.houwei.guaishang.views.SpannableTextView.MemberClickListener;
import com.houwei.guaishang.widget.FloatButton;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.qiniu.android.utils.StringUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class TopicAdapter extends BaseAdapter {
    private final Drawable attention;
    private final Drawable attentionUn;
    private List<TopicBean> list;
    private LayoutInflater mInflater;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private BaseActivity mContext;
    private FaceManager faceManager;
    private String userId;
    private DisplayImageOptions options;
    private TopicBeanDeleteListener onTopicBeanDeleteListener;
    private RedPacketClickListener onRedPacketClickListener;
    private TopicBeanFollowClickListener onTopicBeanFollowClickListener;
    private TopicBeanBaojiaClickListener onTopicBeanBaojiaClickListener;
    private int face_item_size;
    private MProgressDialog dialog;
    private RxPermissions rxPermissions;

    //	设置list跳转不同详情页
    private int jumpType;

    //请求类型（0 是全部  1 是 已订单）
    private int type;
    //头像列表
    private ArrayList<String> mIconList = new ArrayList<>();
    public TopicAdapter(BaseActivity mContext, List<TopicBean> list, int jumpType,int type) {
        this.list = list;
        this.type = type;
        this.mInflater = LayoutInflater.from(mContext);
        this.mContext = mContext;
        this.userId = mContext.getITopicApplication().getMyUserBeanManager().getUserId();
        this.faceManager = mContext.getITopicApplication().getFaceManager();
        this.options = mContext.getITopicApplication().getOtherManage().getRectDisplayImageOptions();
        this.face_item_size = (int) mContext.getResources().getDimension(R.dimen.face_tiny_item_size);
        this.jumpType=jumpType;
        dialog = new MProgressDialog(mContext, true);
        rxPermissions = new RxPermissions(mContext);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            attention = mContext.getDrawable(R.mipmap.attention);
            attentionUn = mContext.getDrawable(R.mipmap.attention_un);
        } else {
            attention = mContext.getResources().getDrawable(R.mipmap.attention);
            attentionUn = mContext.getResources().getDrawable(R.mipmap.attention_un);
        }
        attention.setBounds(0, 0, attention.getIntrinsicWidth(), attention.getIntrinsicHeight());
        attentionUn.setBounds(0, 0, attentionUn.getIntrinsicWidth(), attentionUn.getIntrinsicHeight());
    }

    public int getCount() {
        return list.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_topic, null);
             View finalConvertView = convertView;
            holder.avator = (ImageView) convertView.findViewById(R.id.avator);
            holder.imageMyOrder = (ImageView) convertView.findViewById(R.id.image_myorder);
            holder.header_name = (TextView) convertView.findViewById(R.id.header_name);
//            holder.recyclerView = (LRecyclerView) convertView.findViewById(R.id.recyclerView_offer);
            holder.header_location = (TextView) convertView.findViewById(R.id.header_location);
//            holder.imgTitle = (ImageView) convertView.findViewById(R.id.img_title);
            holder.gridView = (GridView) convertView.findViewById(R.id.grid_pictures);
            holder.tvCount = (TextView) convertView.findViewById(R.id.tv_count);
            holder.content = (TextView) convertView.findViewById(R.id.content);
            holder.header_time = (TextView) convertView.findViewById(R.id.header_time);
//            holder.zan_count_btn = (PraiseTextView) convertView
//                    .findViewById(R.id.zan_count_btn);
//                 已定单
            holder.order_btn = (FloatButton)convertView.findViewById(R.id.order_btn);
            holder.orderBtn_bg = convertView.findViewById(R.id.order_btn_bg);
            holder.order_count = (TextView) convertView.findViewById(R.id.count);
            holder.ratingBar = (RatingBar) convertView.findViewById(R.id.bar);
            holder.ratingBar.setNumStars(4);
            holder.progressView = (ProgressView) convertView.findViewById(R.id.bar_status);
            holder.VProdectLayout = (LinearLayout) convertView.findViewById(R.id.product_layout);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
            holder.price = (TextView) convertView.findViewById(R.id.price);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            holder.dealAvator = convertView.findViewById(R.id.deal_avator);
            holder.dealName = convertView.findViewById(R.id.deal_name);
            holder.dealLayout = convertView.findViewById(R.id.deal_layout);
            holder.dealLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
//            holder.chat_btn = (Button) convertView.findViewById(R.id.chat_btn);
//            holder.linearLayoutForListView = (LinearLayoutForListView) convertView.findViewById(R.id.linearLayoutForListView);
//            holder.linearLayoutForListView.setDisableDivider(true);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final TopicBean bean = list.get(position);

        //处理下全部订单和已订单的ui差别
        if (type == 0){
//           全部订单
            holder.ratingBar.setVisibility(View.VISIBLE);
            holder.progressView.setVisibility(View.GONE);
            holder.VProdectLayout.setVisibility(View.GONE);
            Payment payment = bean.getPayment();
            if (payment != null
                    && !TextUtils.isEmpty(payment.getCycle())
                    && !TextUtils.isEmpty(payment.getPrice())
            ){
                holder.dealLayout.setVisibility(View.VISIBLE);
                holder.dealName.setText(payment.getName());
                ImageLoader.getInstance().displayImage(HttpUtil.IP_NOAPI+payment.getAvatar(), holder.dealAvator);
            }else {
                holder.dealLayout.setVisibility(View.GONE);
            }
//            if (Integer.valueOf(bean.getNowRob()) > 0) {
//                holder.progressBar.setVisibility(View.VISIBLE);
//            }
        }else {
//           已订单
//           付款     付款     基   获取付款
//            holder.progressBar.setVisibility(View.GONE);
            Payment payment = bean.getPayment();
            if (payment != null){
//                      打款进度                          可见
                holder.progressView.setVisibility(View.VISIBLE);
//                      周期价格框                          可见
                holder.VProdectLayout.setVisibility(View.VISIBLE);
//                      打款进度     设置打款进度  打款     获取状态
                holder.progressView.setProgress(payment.getStatus());
//                      价格                  打款     获取价格
                holder.price.setText("总价"+payment.getPrice()+"元");
//                      周期                  打款   获取周期
                holder.time.setText("周期"+payment.getCycle()+"天");




            }else {
//                       打款进度视图                    不可见
                holder.progressView.setVisibility(View.GONE);
//                       价格/周期框                        不可见
                holder.VProdectLayout.setVisibility(View.GONE);
            }
        }
//                     成员ID     基    获取成员ID
        final String memberId = bean.getMemberId();
        try {
//              最大    整数                获取最大值
            int max = Integer.valueOf(bean.getSetRob());
//                进度       整数                获取当前值
            int progress = Integer.valueOf(bean.getNowRob());
//               进度    最大
            if (max != 0) {
                holder.progressBar.setProgress(progress / max);
            }
            if(progress==max){
//                      订单 按钮     已结束状态
                holder.order_btn.setStatu(3);
//                      订单 按钮   设置简单
                holder.order_btn.setBrief("");
//                      订单  数量                      隐藏
                holder.order_count.setVisibility(View.GONE);
//                stopFlick(holder.order_btn);
//               停止电影         订单按钮背景
                stopFlick(holder.orderBtn_bg);
            }else if (TextUtils.equals(mContext.getUserID(),memberId)){
                //自己发的单
                //测试
                mIconList.clear();
                List<OffersBean.OfferBean> offerPrice = bean.getOfferPrice();
                int size = offerPrice.size();
                if (size == 0){
                    holder.order_count.setVisibility(View.GONE);
//                                      等待抢单状态
                    holder.order_btn.setStatu(5);
//                    startFlick(holder.order_btn);
                    startFlick(holder.orderBtn_bg);
                }else {
//                                       数值
                    int unReadCount = 0;
                    for (int i = 0; i < size; i++) {
//                              头像      报价     获取   获取头像
                        String avatar = offerPrice.get(i).getAvatar();
                       unReadCount = unReadCount + HuanXinUtil.g().getUnReadCount(offerPrice.get(i).getOfferId(),bean.getTopicId());
                        if (!mIconList.contains(avatar)) {
                            mIconList.add(avatar);
                        }
                    }
                    if (unReadCount > 0) {
                        holder.order_count.setVisibility(View.VISIBLE);
                        holder.order_count.setText(unReadCount + "");
                    }else {
                        holder.order_count.setVisibility(View.GONE);
                    }
//                                      自己发的单状态
                    holder.order_btn.setStatu(2);
                    if (!StringUtils.isNullOrEmpty(UserUtil.getUserInfo().getAvatar())) {
                        mIconList.add(0, UserUtil.getUserInfo().getAvatar());
                    }
                    holder.order_btn.setmAvatarList(mIconList);
//                    stopFlick(holder.order_btn);
                    stopFlick(holder.orderBtn_bg);
                }
            }else if (bean.getIsDel().equals("1")){
 //刚加
                holder.order_btn.setPublishMemberHeadUrl(bean.getMemberAvatar().getSmall());
// 刚加                                已抢状态
                holder.order_btn.setStatu(4);

//                                  结束状态
//刚注               holder.order_btn.setStatu(3);
                holder.order_btn.setBrief("");
                holder.order_count.setVisibility(View.GONE);
//                stopFlick(holder.order_btn);
                stopFlick(holder.orderBtn_bg);
            }else if (Integer.valueOf(Integer.valueOf(bean.getIsOffer())) == 1){
                int unReadCount = HuanXinUtil.g().getUnReadCount(bean.getMemberId(),bean.getTopicId());
                if (unReadCount > 0){
                    holder.order_count.setVisibility(View.VISIBLE);
                    holder.order_count.setText(unReadCount+"");
                }else {
                    holder.order_count.setVisibility(View.GONE);
                }
                holder.order_btn.setPublishMemberHeadUrl(bean.getMemberAvatar().getSmall());
//                                   已抢状态
                holder.order_btn.setStatu(4);
//                stopFlick(holder.order_btn);
                stopFlick(holder.orderBtn_bg);
            }else {
                holder.order_count.setVisibility(View.GONE);
//                                  抢单状态
                holder.order_btn.setStatu(1);
//                startFlick(holder.order_btn)
                startFlick(holder.orderBtn_bg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(TextUtils.equals(mContext.getUserID(),memberId)){
            holder.imageMyOrder.setVisibility(View.VISIBLE);
//            holder.recyclerView.setVisibility(View.VISIBLE);
//            initRecyclerView(bean,holder.recyclerView,bean.getOfferPrice());
        }else{
//            holder.recyclerView.setVisibility(View.GONE);
            holder.imageMyOrder.setVisibility(View.INVISIBLE);
        }

        //imageLoader.displayImage(bean.getMemberAvatar().findSmallUrl(), holder.avator);
//        imageLoader.displayImage(bean.getMemberAvatar().findSmallUrl(), holder.avator, mContext.getITopicApplication().getOtherManage().getCircleOptionsDisplayImageOptions());
        ImageLoader.getInstance().displayImage(bean.getMemberAvatar().findSmallUrl(), holder.avator);

        String url = bean.getCover();
//        imageLoader.displayImage(url, holder.imgTitle, mContext.getITopicApplication().getOtherManage().getRectDisplayImageOptions());
        if (bean.getPicture() != null && bean.getPicture().size() > 0) {
            holder.gridView.setVisibility(View.VISIBLE);
            List<AvatarBean> pics = bean.getPicture();
            holder.gridView.setNumColumns(pics.size() >= 3 ? 3 : pics.size());
            HomeOrderGridAdapter adapter = new HomeOrderGridAdapter(mContext, pics);
            holder.gridView.setAdapter(adapter);
        }else {
            holder.gridView.setVisibility(View.GONE);
        }
        holder.content.setText("求购： "+faceManager.
                        convertNormalStringToSpannableString(mContext, bean.getContent()),
                BufferType.SPANNABLE);
        FaceManager.extractMention2Link(holder.content);

        holder.content.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {
                // TODO Auto-generated method stub
                MenuDialog followDialog = new MenuDialog(mContext,
                        new MenuDialog.ButtonClick() {

                            @Override
                            public void onSureButtonClick() {
                                // TODO Auto-generated method stub
                                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboard.setText(bean.getContent());
                            }
                        });
                followDialog.show();
                return true;
            }
        });

//        holder.linearLayoutForListView.setAdapter(new CommentItemAdapter(
//                mContext, bean.getComments()));


        String locationTemp = bean.getDistance() != null ? bean.getDistanceString() : bean.getAddress();
        String location = "";

        if(locationTemp.contains("省")){
            location = locationTemp.substring((locationTemp.indexOf("省")+1), locationTemp.length());
        }else {
            location = locationTemp;
        }
        if(locationTemp.contains("市")){
            location = location.substring(0, (location.indexOf("市")+1));
        }
        // TODO: 2018/4/21 设置评分
        if (!TextUtils.isEmpty(bean.getJifen())) {
            Integer jifen = Integer.valueOf(bean.getJifen());
            holder.ratingBar.setRating(jifen);
        }else {
            holder.ratingBar.setRating(0);
        }
        holder.ratingBar.setIsIndicator(true);
        holder.header_location.setText(location);
        holder.header_name.setText(bean.getMemberName());
        holder.header_time.setText(bean.getTimeString());
//        holder.header_time.setText(bean.getTimeString());
//        holder.linearLayoutForListView.setVisibility((bean.getComments() == null || bean.getComments().isEmpty()) ? View.GONE : View.VISIBLE);
//        holder.linearLayoutForListView.setVisibility(View.GONE);

//        holder.zan_count_btn.setText(bean.getSumPrice());


        holder.avator.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.i("WXCH","jumpToHisInfoActivity");
                if (!TextUtils.equals(mContext.getUserID(),memberId) //不是自己发的单
                        && holder.order_btn.getStatus() == 1) {//抢单状态
                    ToastUtils.toastForLong(mContext, "您还未抢单无法查看用户详情，请先抢单");
                    return;
                }
                mContext.jumpToHisInfoActivity(bean.getMemberId(), bean.getMemberName(), bean.getMemberAvatar());
            }
        });

       /* if (userId.equals(bean.getMemberId())) {
            holder.delete_btn.setVisibility(View.VISIBLE);
            holder.chat_btn.setVisibility(View.GONE);
            holder.order_btn.setVisibility(View.GONE);
            holder.follow_btn.setVisibility(View.GONE);
        } else {
            holder.delete_btn.setVisibility(View.GONE);
            holder.chat_btn.setVisibility(View.VISIBLE);
            holder.order_btn.setVisibility(View.VISIBLE);
            holder.follow_btn.setVisibility(View.VISIBLE);
        }*/

//		holder.order_btn.setText(ValueUtil.getTopicTypeBuyButtonString(bean.getType()));
        holder.order_btn.setFloatBtnClickListener(new FloatButton.FloatBtnClickListener() {
            @Override
            public void galb() {
                TopicManager.g().addTopic(bean.getTopicId());
//                DataStorage.putCurrentTopicId(bean.getTopicId());
                orderBuyOrNextPage(bean,true);
                if (refreshImpl != null){
                    refreshImpl.refreshAdapter();
                }
            }

            @Override
            public void goChatView() {
                TopicManager.g().addTopic(bean.getTopicId());
//                DataStorage.putCurrentTopicId(bean.getTopicId());
                if (refreshImpl != null){
                    refreshImpl.refreshAdapter();
                }
                // TODO: 2018/4/21 跳转到聊天页面
                Intent intent = new Intent(mContext, OrderChatActivity.class);
                List<OffersBean.OfferBean> offerPriceList = bean.getOfferPrice();
                ArrayList<OffersBean.OfferBean> tempList = new ArrayList<OffersBean.OfferBean>();
                int size = offerPriceList.size();
                for (int i = 0; i < size; i++) {
                    OffersBean.OfferBean offerBean = offerPriceList.get(i);
                    boolean hanSameOfferBean = false;
                    for (int z = 0; z < tempList.size(); z++) {
                        if (tempList.get(z).getUserid().equals(offerBean.getUserid())) {
                            hanSameOfferBean = true;
                            break;
                        }
                    }
                    if (!hanSameOfferBean) {
                        tempList.add(offerBean);
                    }

                }
                intent.putExtra(OrderChatActivity.Parse_List,(Serializable) tempList);
                intent.putExtra(OrderChatActivity.SID,bean.getMemberId());
                intent.putExtra(OrderChatActivity.OrderId,bean.getTopicId());
                intent.putExtra(OrderChatActivity.Brand,bean.getBrand());
                intent.putExtra(OrderChatActivity.ALONE,false);
                intent.putExtra(OrderChatActivity.TopicId,bean.getTopicId());
                mContext.startActivity(intent);
            }

            @Override
            public void doNothing() {

            }

            @Override
            public void chatAlone() {
                TopicManager.g().addTopic(bean.getTopicId());
//                DataStorage.putCurrentTopicId(bean.getTopicId());
                if (refreshImpl != null){
                    refreshImpl.refreshAdapter();
                }
                Intent intent = new Intent(mContext, OrderChatActivity.class);
                ArrayList<OffersBean.OfferBean> tempList = new ArrayList<>();
                OffersBean.OfferBean  tempBean = new OffersBean.OfferBean();
                tempBean.setName(bean.getMemberName());
                tempBean.setMobile(bean.getMobile());
                tempBean.setUserid(bean.getMemberId());
                tempBean.setAvatar(bean.getMemberAvatar().findOriginalUrl());
                tempList.add(tempBean);
                intent.putExtra(OrderChatActivity.Parse_List,(Serializable) tempList);
                intent.putExtra(OrderChatActivity.SID,bean.getMemberId());
                intent.putExtra(OrderChatActivity.OrderId,bean.getTopicId());
                intent.putExtra(OrderChatActivity.Brand,bean.getBrand());
                intent.putExtra(OrderChatActivity.ALONE,true);
                intent.putExtra(OrderChatActivity.TopicId,bean.getTopicId());
                mContext.startActivity(intent);
            }
        });


//        holder.imgTitle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ArrayList<String> list = new ArrayList<String>();
//
//                if (bean.getPicture().size() > 1) {
//                    list.add(HttpUtil.IP_NOAPI+bean.getPicture().get(1).getOriginal());
//                }else {
//                    list.add(bean.getCover());
//                }
//                Intent intent = new Intent(mContext, PreviewActivity.class);
//                intent.putExtra("list",list);
//                mContext.startActivity(intent);
//            }
//        });
//        convertView.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                //Log.i("WXCH","bean:"+bean);
//                if(TextUtils.equals(mContext.getUserID(),memberId)){
//                    Intent intent=new Intent(mContext,TopicDetailMeActivity.class);
//                    intent.putExtra("TopicBean", bean);
//                    intent.putExtra("position", 0);
//                    mContext.startActivityForResult(intent, 0);
//                    return;
//                }
//                Intent i = new Intent();
//                if(jumpType==0){
//                    i.setClass(mContext, TopicDetailActivity.class);
//                }else{
//                    i.setClass(mContext, TopicDetailMeActivity.class);
//
//                }
//                i.putExtra("TopicBean", bean);
//                i.putExtra("position", 0);
//                i.putExtra("needPay", Integer.valueOf(bean.getIsOffer()));
//                mContext.startActivityForResult(i, 0);
//
//            }
//        });
        return convertView;
    }

    private void orderBuyOrNextPage(final TopicBean bean, final boolean fromBtnClick) {
        OkGo.<String>post(HttpUtil.IP+"Topic/is_rob")
                .tag(this)
                .params("user_id", userId)
                .params("topicid", bean.getTopicId())
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        String res=response.body().toString().trim();
                        //Log.i("WXCH","SSSSSS:" + res);
                        if(res.contains("1")){
//                            OrderBuyDialog.getInstance(mContext)
//                                    .setData(PreferenceManager.getInstance().getUserCoins(), bean, mContext)
//                                    .show();
                            new OrderBuyDialog(mContext, PreferenceManager.getInstance().getUserCoins(), bean, new OrderBuyDialog.FinishCallBack() {
                                @Override
                                public void call() {
                                    //抢单请求
                                    Intent intent = new Intent(mContext, OrderChatActivity.class);
                                    ArrayList<OffersBean.OfferBean> tempList = new ArrayList<>();
                                    OffersBean.OfferBean  tempBean = new OffersBean.OfferBean();
                                    tempBean.setName(bean.getMemberName());
                                    tempBean.setMobile(bean.getMobile());
                                    tempBean.setUserid(bean.getMemberId());
                                    tempBean.setAvatar(bean.getMemberAvatar().findOriginalUrl());
                                    tempList.add(tempBean);
                                    intent.putExtra(OrderChatActivity.Parse_List,(Serializable) tempList);
                                    intent.putExtra(OrderChatActivity.SID,bean.getMemberId());
                                    intent.putExtra(OrderChatActivity.OrderId,bean.getTopicId());
                                    intent.putExtra(OrderChatActivity.Brand,bean.getBrand());
                                    intent.putExtra(OrderChatActivity.ALONE,true);
                                    intent.putExtra(OrderChatActivity.ShouldOffer,true);
                                    intent.putExtra(OrderChatActivity.TopicId,bean.getTopicId());
                                    mContext.startActivity(intent);
                                }
                            }).show();
                        }else if (fromBtnClick){
                            ToastUtils.toastForShort(mContext, "此单您已抢过");
                        } else {
                            Intent i = new Intent();
                            i.putExtra("TopicBean", bean);
                            i.putExtra("position", 0);
                            mContext.jumpToChatActivityCom(bean,0,bean.getMemberId(), bean.getMemberName(), bean.getMemberAvatar(),EaseConstant.CHATTYPE_SINGLE);
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {

                        super.onError(response);
                    }
                });
    }

    private void initRecyclerView(final TopicBean topicBean, LRecyclerView recyclerViewOffer, List<OffersBean.OfferBean> beans) {
        if(beans==null||beans.isEmpty()){
            return;
        }
        LinearLayoutManager manager=new LinearLayoutManager(mContext);
        manager.setAutoMeasureEnabled(true);
        final OfferAdapter mAdapter = new OfferAdapter(mContext);
//        TopicLinearLayoutManager manager1=new TopicLinearLayoutManager(mContext,mAdapter);
//        recyclerViewOffer.setLayoutManager(manager);
        mAdapter.setDataList(beans);
        mAdapter.setTopicBean(topicBean);
        final LRecyclerViewAdapter lRecyclerViewAdapter=new LRecyclerViewAdapter(mAdapter);
        recyclerViewOffer.setAdapter(lRecyclerViewAdapter);
        recyclerViewOffer.setLoadMoreEnabled(false);
        lRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                OffersBean.OfferBean bean = mAdapter.getDataList().get(position);
                if(TextUtils.equals(mContext.getUserID(),bean.getOfferId())){
                    ToastUtils.toastForShort(mContext,"不能同自己聊天");
                    return;
                }
                AvatarBean avatarBean=new AvatarBean();
                avatarBean.setOriginal(bean.getAvatar());
                avatarBean.setSmall(bean.getAvatar());
                mContext.jumpToChatActivityCom(topicBean,0,bean.getOfferId(), bean.getName(), avatarBean, EaseConstant.CHATTYPE_SINGLE);
            }
        });
        recyclerViewOffer.setPullRefreshEnabled(false);
        recyclerViewOffer.setLoadMoreEnabled(false);
        recyclerViewOffer.refresh();
    }

    private void dealPraise(final PraiseTextView zan_count_btn, final TopicBean topicBean) {
        dialog.show();
        OkGo.<String>post(HttpUtil.IP+"topic/praise")
                .tag(this)
                .params("userid", userId)
                .params("topicid", topicBean.getTopicId())
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        dialog.dismiss();
                        PraiseBean bean=DealResult.getInstace().dealData(mContext,response,PraiseBean.class);
                        if(bean==null){return;}
                        if(bean.getCode()==Constant.SUCESS){
                            if(topicBean.isPraised()){
                                zan_count_btn.setText((topicBean.getPraiseCount()-1)+"");
                                topicBean.setPraiseCount(topicBean.getPraiseCount()-1);
                                zan_count_btn.setCompoundDrawables(null,attention,null,null);
                                topicBean.setPraised(false);
                            }else{
                                zan_count_btn.setText((topicBean.getPraiseCount()+1)+"");
                                topicBean.setPraiseCount(topicBean.getPraiseCount()+1);
                                zan_count_btn.setCompoundDrawables(null,attention,null,null);
                                topicBean.setPraised(true);
                            }
//                            topicBean.setFriendship(bean.getData());
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        dialog.dismiss();
                        super.onError(response);
                    }
                });
    }

    public static class ViewHolder {
        private TextView content, header_name,  header_location,tvCount,header_time;
        private PraiseTextView zan_count_btn;
        private ImageView avator,imgTitle,imgIndicate,imageMyOrder;
        private GridView gridView;
        private TextView  tvProgress;
        private View orderBtn_bg;
//        private LinearLayoutForListView linearLayoutForListView;
//        private LRecyclerView recyclerView;
        private ProgressView progressView;
        private LinearLayout VProdectLayout;
        private ProgressBar progressBar;
        private TextView price,time;
        private FloatButton order_btn;
        private TextView order_count;
        private RatingBar ratingBar;
        private RelativeLayout dealLayout;
        private ImageView dealAvator;
        private TextView dealName;
    }

    private DisplayImageOptions kkk(Context context) {

        return new DisplayImageOptions.Builder()
                .displayer(new CircleBitmapDisplayer1(context)).build();

    }

    public interface TopicBeanDeleteListener {
        public void onTopicBeanDeleteClick(TopicBean topicBean);
    }

    public void setOnTopicBeanDeleteListener(TopicBeanDeleteListener onTopicBeanDeleteListener) {
        this.onTopicBeanDeleteListener = onTopicBeanDeleteListener;
    }

    public TopicBeanDeleteListener getOnTopicBeanDeleteListener() {
        return onTopicBeanDeleteListener;
    }

    public interface TopicBeanFollowClickListener {
        public void onTopicBeanFollowClick(TopicBean topicBean);
    }
    public interface TopicBeanBaojiaClickListener {
        public void TopicBeanBaojiaClick(TopicBean topicBean);
    }


    public class CommentItemAdapter extends BaseAdapter {

        private BaseActivity mContext;
        private List<CommentBean> cellReviewList;
        private LayoutInflater mLayoutInflater;

        public CommentItemAdapter(BaseActivity context, List<CommentBean> cellReviewList) {
            this.mContext = context;
            this.cellReviewList = cellReviewList;
            this.mLayoutInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public int getCount() {
            return cellReviewList == null ? 0 : cellReviewList.size();
        }

        @Override
        public String getItem(int index) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.listview_review_textview, null);
            }
            final CommentBean cellBean = cellReviewList.get(position);
            SpannableTextView comment_tiny_tv = (SpannableTextView) convertView
                    .findViewById(R.id.comment_tiny_tv);


            comment_tiny_tv.setCommentItem(cellBean, new MemberClickListener() {

                @Override
                public void onMemberClick(CommentBean commentBean) {
                    // TODO Auto-generated method stub

                    mContext.jumpToHisInfoActivity(commentBean.getMemberId(),
                            commentBean.getMemberName(),
                            commentBean.getMemberAvatar());
                }
            }, new MemberClickListener() {

                @Override
                public void onMemberClick(CommentBean commentBean) {
                    // TODO Auto-generated method stub

                    mContext.jumpToHisInfoActivity(commentBean.getToMemberId(),
                            commentBean.getToMemberName(),
                            commentBean.getToMemberAvatar());

                }
            });

            comment_tiny_tv.append(faceManager.
                    convertNormalStringToSpannableString(mContext, cellBean.getContent(), face_item_size));

            return convertView;
        }
    }

    public RedPacketClickListener getOnRedPacketClickListener() {
        return onRedPacketClickListener;
    }

    public void setOnRedPacketClickListener(RedPacketClickListener onRedPacketClickListener) {
        this.onRedPacketClickListener = onRedPacketClickListener;
    }

    public TopicBeanFollowClickListener getOnTopicBeanFollowClickListener() {
        return onTopicBeanFollowClickListener;
    }

    public void setOnTopicBeanFollowClickListener(
            TopicBeanFollowClickListener onTopicBeanFollowClickListener) {
        this.onTopicBeanFollowClickListener = onTopicBeanFollowClickListener;
    }

    public TopicBeanBaojiaClickListener getTopicBeanBaojiaClickListener() {
        return onTopicBeanBaojiaClickListener;
    }

    public void setTopicBeanBaojiaClickListener(
            TopicBeanBaojiaClickListener onTopicBeanBaojiaClickListener) {
        this.onTopicBeanBaojiaClickListener = onTopicBeanBaojiaClickListener;
    }



    //按钮闪烁动画
    private void startFlick( View view ){

        if( null == view ){

            return;

        }
        view.setBackground(mContext.getResources().getDrawable(R.drawable.topic_order_animate_bg));

        Animation alphaAnimation = new AlphaAnimation( 1, 0 );

        alphaAnimation.setDuration( 1000 );

        alphaAnimation.setInterpolator( new LinearInterpolator( ) );

        alphaAnimation.setRepeatCount( Animation.INFINITE );

        alphaAnimation.setRepeatMode( Animation.REVERSE );

        view.startAnimation( alphaAnimation );

    }

    //按钮取消闪现动画
    private void stopFlick( View view ){

        if( null == view ){
            return;
        }
        view.setBackground(mContext.getResources().getDrawable(R.drawable.topic_order_bg));
        view.clearAnimation( );
    }

    private RefreshImpl refreshImpl;

    public void setRefreshImpl(RefreshImpl refreshImpl) {
        this.refreshImpl = refreshImpl;
    }

    public interface RefreshImpl{
        void refreshAdapter();
    }

}


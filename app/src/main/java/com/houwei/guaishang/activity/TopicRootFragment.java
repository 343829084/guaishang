package com.houwei.guaishang.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.houwei.guaishang.R;
import com.houwei.guaishang.bean.IndustryBean;
import com.houwei.guaishang.event.BrandSelectEvent;
import com.houwei.guaishang.layout.IndustyPopWindow;
import com.houwei.guaishang.layout.PopInter;
import com.houwei.guaishang.tools.SPUtils;
import com.houwei.guaishang.video.JCVideoPlayer;
import com.houwei.guaishang.views.ViewPagerTabButton;
import com.houwei.guaishang.views.ViewPagerTabsAdapter;
import com.houwei.guaishang.views.ViewPagerTabsView;
import com.houwei.guaishang.views.ViewPagerTabsView.PageSelectedListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
//             主题根片段                                          页面选择检同期
public class TopicRootFragment extends BaseFragment implements PageSelectedListener,PopInter {
//                     头部标题
	private String[] mTopTitles = {  "全部", "已订单" };// "原创"
//           列表   主题家片段        列表视图
	private List<TopicHomeFragment> listViews;
//   品种弹出窗      弹窗
	IndustyPopWindow pop;
//            图片视图  图片添加
	private ImageView imageAdd;
//            主题家片段        视频布局1
	private TopicHomeFragment videoLayout1;
//	private TopicFollowedFragment videoLayout2;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
//              找文件                      活动视频          容器
		return inflater.inflate(R.layout.activity_video, container, false);
	}

	@Override
//                 创建活动
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
//       事件总线  获取默认    注册
		EventBus.getDefault().register(this);
//      初始化视图
		initView();
//      初始化监听器
		initListener();
	}
//                  初始化监听器
	private void initListener() {
		// TODO Auto-generated method stub
//       获取视图                     标题右边视图
		getView().findViewById(R.id.title_right_iv).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent i = new Intent(getActivity(),
//                               搜索活动
								SearchActivity.class);
						startActivity(i);
					}
				});
//                                    图片添加
		getView().findViewById(R.id.image_add).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
//                         弹窗        弹窗  显示
						if(pop!=null&&pop.isShowing()){
//                         弹窗   驳回
							pop.dismiss();
						}else{
//                          得到行业数据
							getIndustryData();
						}
					}
				});
	}
//                 得到行业数据
	private void getIndustryData() {
//       意图                                     品牌选择活动
		Intent intent = new Intent(getActivity(),BrandSelectActivity.class);
//       启动活动结果                 品牌选择活动		   选择品牌
		startActivityForResult(intent,BrandSelectActivity.SELECT_BRAND);
//		progress.show();
//        String url = "";
//        if(checkLogined()){
//            //url = HttpUtil.IP+"topic/my_brand";
//            url = HttpUtil.IP+"topic/brand";
//            user_id = getUserID();
//        }
//		url = HttpUtil.IP+"topic/brand";
//		//IndustryBean.ItemsBean itemsBean = new IndustryBean.ItemsBean();
//
//		//List<IndustryBean.ItemsBean> itemsBeen = new ArrayList<>();
//
//
//		OkGo.<String>post(url)
//                //.params("user_id",userId)
//				.execute(new StringCallback() {
//					@Override
//					public void onSuccess(Response<String> response) {
//						progress.dismiss();
//						DealResult dr=new DealResult();
//						IndustryBean bean=dr.dealData(getActivity(),response,new TypeToken<BaseBean<IndustryBean>>(){}.getType());
//						if(bean==null){
//							return;
//						}
//						final List<IndustryBean.ItemsBean> itemsBeans =bean.getItems();
//						if(itemsBeans.isEmpty()){
//							return;
//						}
//						//Log.i("WXCH","itemsBeen:"+itemsBeans);
//						for(int i=0; i<itemsBeans.size();i++){
//							IndustryBean.ItemsBean item = itemsBeans.get(i);
//							Log.i("WXCH","ItemsBean Id:"+item.getId()+",BrandName:"+item.getBrandName());
//						}
//						showPop(itemsBeans,user_id);
//
//					}
//
//					@Override
//					public void onError(Response<String> response) {
//						super.onError(response);
//						progress.dismiss();
//					}
//				});
	}
//                 显示弹窗       行业基     项基        项基     文本   用户ID
	private void showPop(List<IndustryBean.ItemsBean> itemsBeen, String user_id) {
//       行业弹窗        弹窗     行业弹窗                      项基                  用户ID
		IndustyPopWindow pop=new IndustyPopWindow(getActivity(),itemsBeen,this,user_id);
//      弹窗  显示弹窗       图片添加
		pop.showPopupWindow(imageAdd);
	}
//                 初始化视图
	private void initView() {
//       初始化进图对话框
		initProgressDialog();
//        视图布局1            主题家片段
		 videoLayout1 = new TopicHomeFragment();
//         视图布局2           主题点赞片段
//		 videoLayout2 = new TopicFollowedFragment();
//       主题足迹片段             视频布局3         主题足迹片段
		TopicFootprintFragment videoLayout3 = new TopicFootprintFragment();

//       列表视图         数组列表   主题家片段
		listViews = new ArrayList<TopicHomeFragment>();
//      列表视图    添加  视频布局1
		listViews.add(videoLayout1);
//		listViews.add(videoLayout2);
//      列表视图   添加   视频布局3
		listViews.add(videoLayout3);
//       视图翻页  翻页      视图翻页   获取视图                   视图翻页
		ViewPager mPager = (ViewPager) getView().findViewById(R.id.viewpager);
//      图片添加                                             图片添加
		imageAdd = (ImageView) getView().findViewById(R.id.image_add);
//       翻页  设置画面翻页以外的限制 头部标题   长度
		mPager.setOffscreenPageLimit(mTopTitles.length);
//       翻页适配器   翻页适配器          例子翻页适配器        获取子片段管理
		PagerAdapter mPagerAdapter = new ExamplePagerAdapter(getChildFragmentManager());
//      翻页   设置适配器  翻页适配器
		mPager.setAdapter(mPagerAdapter);
//      翻页   设置当前项
		mPager.setCurrentItem(0);
//      翻页  设置页面边缘
		mPager.setPageMargin(1);
//     视图翻页选项卡视图   固定选项卡  视图烟叶选项卡视图  获取视图                     固定选项卡
		ViewPagerTabsView mFixedTabs = (ViewPagerTabsView) getView().findViewById(R.id.fixed_tabs);
//      固定选项卡  设置翻页选择监听器
		mFixedTabs.setOnPageSelectedListener(this);
//     视图翻页选项卡适配器	  固定选项卡适配器       固定选项卡适配器
		ViewPagerTabsAdapter mFixedTabsAdapter = new FixedTabsAdapter(getActivity());
//     固定选项卡  设置适配器  固定选项卡适配器
		mFixedTabs.setAdapter(mFixedTabsAdapter,true);
//     固定选项卡  设置视图翻页  翻页
		mFixedTabs.setViewPager(mPager,true);
//     视图布局1      刷新
		videoLayout1.refresh();

		//listViews.get(position).refresh();
	}

//                刷新列表         位置
	public void refreshList(int position){
//      视图翻页   翻页      视图翻页  获取视图                    视图翻页
		ViewPager mPager = (ViewPager) getView().findViewById(R.id.viewpager);
//      翻页    设置当前项
		mPager.setCurrentItem(0);
//      列表视图   获取 位置     刷新
		listViews.get(position).refresh();
	}
//    订阅      线程模型     线程模型   主
	@Subscribe(threadMode = ThreadMode.MAIN)
//                品牌选择    品牌选择事件
	public void brandSelect(BrandSelectEvent event) {
//           视图布局1
		if (videoLayout1 != null) {
//           视图布局1     刷新
			videoLayout1.refresh();
		}
	}

	@Override
//                翻页选中
	public void onPageSelected(int position) {
		// TODO Auto-generated method stub
//        视频播放器   	发布所有视频
		JCVideoPlayer.releaseAllVideos();
	}

	@Override
//                摧毁
	public void onDestroy() {
		// TODO Auto-generated method stub
//      事件总线  获取默认     	注销
		EventBus.getDefault().unregister(this);
//              注销
		super.onDestroy();
	}

	@Override
//                 暂停
	public void onPause() {
		super.onPause();

	}


	@Override
//                  隐藏的变化	           隐藏的
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);

	}


	@Override
//                 活动结果             请求代码          结果代码         数据
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
//      日志                           结果代码
		Log.d("CCC","fa:"+resultCode);
//                活动结果       请求代码     结果代码
		super.onActivityResult(requestCode, resultCode, data);
//       开关     结果代码
		switch (resultCode) {
//      情况下	主题发布活动	     发布成功
		case TopicReleaseActivity.RELEASE_SUCCESS:
//           刷新列表
			refreshList(0);
			break;
//      情况下  品牌选择活动		选择品牌
		case BrandSelectActivity.SELECT_BRAND:
//          文本    品牌参数    数据  获取文本夹带   品牌选择活动   	 品牌参数
			String brandParam = data.getStringExtra(BrandSelectActivity.BRAND_PARAM);
//                 文本工具  空     品牌参数
			if (!TextUtils.isEmpty(brandParam)) {
//              提交   品牌参数
				commit(brandParam);
			}
			break;
//          默认
		default:
			break;
		}
	}

	@Override
//                提交         参数
	public void commit(String params) {
//       SP工具  输入                   品牌ID     参数
		SPUtils.put(getActivity(),"brand_id",params);
//       视频活动1      刷新
		videoLayout1.refresh();
	}


	/**
	 * view pager
	 */
//                  例子翻页适配器              片段翻页适配器
	public class ExamplePagerAdapter extends FragmentPagerAdapter {

		public ExamplePagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}
//                                      上下文
		protected transient Activity mContext;
//               列表  主题家片段        视图列表
		private List<TopicHomeFragment> viewLists;

		@Override
//              片段     获取项
		public Fragment getItem(int arg0) {
			// TODO Auto-generated method stub
//                   列表视图  获取
			return listViews.get(arg0);
		}

		@Override
//                   获取数
		public int getCount() {
			// TODO Auto-generated method stub
//                  列表视图   数
			return listViews.size();
		}

	}
//                 固定选项卡适配器  实现了     视图翻页选项卡适配器
	public class FixedTabsAdapter implements ViewPagerTabsAdapter {
//                 活动    上下文
		private Activity mContext;
//              固定选项卡适配器
		public FixedTabsAdapter(Activity ctx) {
			this.mContext = ctx;
		}

		@Override
//                   获取视图      位置
		public View getView(int position) {
//          视图翻页标签按钮    标签
			ViewPagerTabButton tab;
//           布局查询视图   查询       上下文    获取布局查询
			LayoutInflater inflater = mContext.getLayoutInflater();
//          标签    视图翻页按钮        查询   查询
			tab = (ViewPagerTabButton) inflater.inflate(
//                             视图翻页标签按钮
					R.layout.viewpager_tab_button, null);
//               位置       头部标题    长度
			if (position < mTopTitles.length)
//              标签 设置文本 头部标题   位置
				tab.setText(mTopTitles[position]);
//                  标签
			return tab;
		}

	}

	@Override
//              	翻页状态发生变化
	public void onPageScrollStateChanged(int state) {

	}


}
